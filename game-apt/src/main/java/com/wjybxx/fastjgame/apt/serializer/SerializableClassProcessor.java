/*
 *  Copyright 2019 wjybxx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to iBn writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wjybxx.fastjgame.apt.serializer;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wjybxx.fastjgame.apt.core.MyAbstractProcessor;
import com.wjybxx.fastjgame.apt.db.DBEntityProcessor;
import com.wjybxx.fastjgame.apt.utils.AutoUtils;
import com.wjybxx.fastjgame.apt.utils.BeanUtils;

import javax.annotation.Nullable;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * 1. 对于普通类：必须包含无参构造方法，且field注解的number必须在 0-65535之间
 * 2. 对于枚举：必须实现 NumericalEnum 接口，且提供非private的forNumber方法
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/23
 * github - https://github.com/hl845740757
 */
@AutoService(Processor.class)
public class SerializableClassProcessor extends MyAbstractProcessor {

    // 使用这种方式可以脱离对utils，net包的依赖
    private static final String SERIALIZABLE_CLASS_CANONICAL_NAME = "com.wjybxx.fastjgame.net.annotation.SerializableClass";
    private static final String SERIALIZABLE_FIELD_CANONICAL_NAME = "com.wjybxx.fastjgame.net.annotation.SerializableField";

    private static final String SERIALIZER_CANONICAL_NAME = "com.wjybxx.fastjgame.net.binary.EntitySerializer";
    private static final String ABSTRACT_SERIALIZER_CANONICAL_NAME = "com.wjybxx.fastjgame.net.binary.AbstractEntitySerializer";

    private static final String GET_ENTITY_CLASS_METHOD_NAME = "getEntityClass";
    private static final String WRITE_OBJECT_METHOD_NAME = "writeObject";
    private static final String READ_OBJECT_METHOD_NAME = "readObject";

    private static final String NEW_INSTANCE_METHOD_NAME = "newInstance";
    private static final String READ_FIELDS_METHOD_NAME = "readFields";

    private TypeMirror mapTypeMirror;
    private TypeMirror collectionTypeMirror;
    private TypeMirror stringTypeMirror;
    TypeMirror enumSetRawTypeMirror;
    TypeMirror enumMapRawTypeMirror;

    private TypeElement serializableClassElement;
    private DeclaredType serializableFieldDeclaredType;

    private TypeElement dbEntityTypeElement;
    private DeclaredType dbFieldDeclaredType;
    private DeclaredType impDeclaredType;

    private DeclaredType numericalEnumDeclaredType;
    private DeclaredType indexableEntityDeclaredType;

    TypeElement serializerTypeElement;
    // 要覆盖的方法缓存，减少大量查询
    private ExecutableElement getEntityClassMethod;
    private ExecutableElement writeObjectMethod;
    private ExecutableElement readObjectMethod;

    TypeElement abstractSerializerTypeElement;
    ExecutableElement newInstanceMethod;
    ExecutableElement readFieldsMethod;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(SERIALIZABLE_CLASS_CANONICAL_NAME, DBEntityProcessor.DB_ENTITY_CANONICAL_NAME);
    }

    @Override
    protected void ensureInited() {
        if (serializableClassElement != null) {
            return;
        }

        mapTypeMirror = elementUtils.getTypeElement(Map.class.getCanonicalName()).asType();
        collectionTypeMirror = elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType();
        stringTypeMirror = elementUtils.getTypeElement(String.class.getCanonicalName()).asType();
        enumSetRawTypeMirror = typeUtils.erasure(elementUtils.getTypeElement(EnumSet.class.getCanonicalName()).asType());
        enumMapRawTypeMirror = typeUtils.erasure(elementUtils.getTypeElement(EnumMap.class.getCanonicalName()).asType());

        serializableClassElement = elementUtils.getTypeElement(SERIALIZABLE_CLASS_CANONICAL_NAME);
        serializableFieldDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(SERIALIZABLE_FIELD_CANONICAL_NAME));

        dbEntityTypeElement = elementUtils.getTypeElement(DBEntityProcessor.DB_ENTITY_CANONICAL_NAME);
        dbFieldDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(DBEntityProcessor.DB_FIELD_CANONICAL_NAME));
        impDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(DBEntityProcessor.IMPL_CANONICAL_NAME));

        numericalEnumDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(BeanUtils.NUMBER_ENUM_CANONICAL_NAME));
        indexableEntityDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(BeanUtils.INDEXABLE_ENTITY_CANONICAL_NAME));

        serializerTypeElement = elementUtils.getTypeElement(SERIALIZER_CANONICAL_NAME);
        getEntityClassMethod = AutoUtils.findMethodByName(serializerTypeElement, GET_ENTITY_CLASS_METHOD_NAME);
        writeObjectMethod = AutoUtils.findMethodByName(serializerTypeElement, WRITE_OBJECT_METHOD_NAME);
        readObjectMethod = AutoUtils.findMethodByName(serializerTypeElement, READ_OBJECT_METHOD_NAME);

        abstractSerializerTypeElement = elementUtils.getTypeElement(ABSTRACT_SERIALIZER_CANONICAL_NAME);
        newInstanceMethod = AutoUtils.findMethodByName(abstractSerializerTypeElement, NEW_INSTANCE_METHOD_NAME);
        readFieldsMethod = AutoUtils.findMethodByName(abstractSerializerTypeElement, READ_FIELDS_METHOD_NAME);

    }

    @Override
    protected boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 该注解只有类可以使用
        @SuppressWarnings("unchecked") final Set<TypeElement> allTypeElements = (Set<TypeElement>) roundEnv.getElementsAnnotatedWithAny(serializableClassElement, dbEntityTypeElement);
        final Set<TypeElement> sourceFileTypeElementSet = AutoUtils.selectSourceFile(allTypeElements, elementUtils);

        for (TypeElement typeElement : sourceFileTypeElementSet) {
            try {
                checkBase(typeElement);
                generateSerializer(typeElement);
            } catch (Throwable e) {
                messager.printMessage(Diagnostic.Kind.ERROR, AutoUtils.getStackTrace(e), typeElement);
            }
        }
        return true;
    }

    /**
     * 基础信息检查
     */
    private void checkBase(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.ENUM) {
            // 枚举需要放在最前面检查 - 因为它可能实现后面的特殊接口
            checkEnum(typeElement);
            return;
        }

        if (isNumericalEnum(typeElement)) {
            // NumericalEnum是IndexableEntity的子类，需要放在前面检查
            checkNumericalEnum(typeElement);
            return;
        }

        if (isIndexableEntity(typeElement)) {
            checkIndexableEntity(typeElement);
            return;
        }

        if (typeElement.getKind() == ElementKind.CLASS) {
            // 检查普通类
            checkClass(typeElement);
            return;
        }
        // 其它类型抛出编译错误
        messager.printMessage(Diagnostic.Kind.ERROR, "unsupported type", typeElement);
    }

    /**
     * 检查枚举 - 要序列化的枚举，必须实现 NumericalEnum 接口，否则无法序列化，或自己手写serializer。
     */
    private void checkEnum(TypeElement typeElement) {
        if (!isNumericalEnum(typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "serializable enum must implement " + numericalEnumDeclaredType.asElement().getSimpleName(),
                    typeElement);
        }
        checkNumericalEnum(typeElement);
    }

    private boolean isNumericalEnum(TypeElement typeElement) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, typeElement.asType(), numericalEnumDeclaredType);
    }

    /**
     * 检查 NumericalEnum 的子类是否有forNumber方法
     */
    private void checkNumericalEnum(TypeElement typeElement) {
        if (!isContainStaticNotPrivateForNumberMethod(typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    String.format("%s must contains a not private 'static %s forNumber(int)' method!", typeElement.getSimpleName(), typeElement.getSimpleName()),
                    typeElement);
        }
    }

    /**
     * 是否包含静态的非private的forNumber方法 - static T forNumber(int)
     * (一定有getNumber方法)
     */
    private boolean isContainStaticNotPrivateForNumberMethod(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(method -> !method.getModifiers().contains(Modifier.PRIVATE))
                .filter(method -> method.getModifiers().contains(Modifier.STATIC))
                .filter(method -> method.getSimpleName().toString().equals(BeanUtils.FOR_NUMBER_METHOD_NAME))
                .filter(method -> method.getParameters().size() == 1)
                .anyMatch(method -> method.getParameters().get(0).asType().getKind() == TypeKind.INT);
    }

    private boolean isIndexableEntity(TypeElement typeElement) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, typeElement.asType(), indexableEntityDeclaredType);
    }

    /**
     * 检查可索引的实体，检查是否有forIndex方法
     */
    private void checkIndexableEntity(TypeElement typeElement) {
        if (!containsGetIndexMethod(typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "can't find getIndex() method", typeElement);
            return;
        }

        final TypeMirror indexTypeMirror = getIndexTypeMirror(typeElement);
        if (!containsStaticNotPrivateForIndexMethod(typeElement, indexTypeMirror)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    String.format("%s must contains a not private 'static %s forIndex(%s)' method!",
                            typeElement.getSimpleName(), typeElement.getSimpleName(), indexTypeMirror.toString()),
                    typeElement);
        }
    }

    private static boolean containsGetIndexMethod(TypeElement typeElement) {
        return findGetIndexMethod(typeElement).isPresent();
    }

    private static Optional<ExecutableElement> findGetIndexMethod(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(e -> e.getParameters().size() == 0)
                .filter(e -> ((Element) e).getSimpleName().toString().equals(BeanUtils.GET_INDEX_METHOD_NAME))
                .findFirst();
    }

    private static TypeMirror getIndexTypeMirror(TypeElement typeElement) {
        final ExecutableElement getIndexMethod = findGetIndexMethod(typeElement).orElseThrow();
        return getIndexMethod.getReturnType();
    }

    /**
     * 是否包含配私有的静态forIndex方法
     *
     * @param typeElement     方法的返回值类型
     * @param indexTypeMirror 方法的参数类型
     */
    private boolean containsStaticNotPrivateForIndexMethod(final TypeElement typeElement, final TypeMirror indexTypeMirror) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(method -> !method.getModifiers().contains(Modifier.PRIVATE))
                .filter(method -> method.getModifiers().contains(Modifier.STATIC))
                .filter(method -> method.getSimpleName().toString().equals(BeanUtils.FOR_INDEX_METHOD_NAME))
                .filter(method -> method.getParameters().size() == 1)
                .filter(method -> AutoUtils.isSameTypeIgnoreTypeParameter(typeUtils, method.getParameters().get(0).asType(), indexTypeMirror))
                .anyMatch(method -> AutoUtils.isSameTypeIgnoreTypeParameter(typeUtils, method.getReturnType(), typeElement.asType()));
    }

    private void checkClass(TypeElement typeElement) {
        // 父类可能是不序列化的，但是有字段要序列化
        final List<? extends Element> allFieldsAndMethodWithInherit = BeanUtils.getAllFieldsAndMethodsWithInherit(typeElement);
        for (Element element : allFieldsAndMethodWithInherit) {
            // 非成员属性
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }

            final VariableElement variableElement = (VariableElement) element;
            // 不需要序列化
            if (!isSerializableField(variableElement)) {
                continue;
            }

            // 不能是static
            if (variableElement.getModifiers().contains(Modifier.STATIC)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "serializable field can't be static", variableElement);
                continue;
            }

            // 必须包含非private的getter方法
            if (!isContainerNotPrivateGetterMethod(variableElement, allFieldsAndMethodWithInherit)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "serializable field must contains a not private getter", variableElement);
                continue;
            }

            // map和集合类型
            if (isMapOrCollection(variableElement)) {
                checkMapAndCollectionField(variableElement);
            }
        }

        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            // 抽象类不检测无参构造方法
            return;
        }

        // 无参构造方法检测
        if (!BeanUtils.containsNoArgsConstructor(typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "SerializableClass " + typeElement.getSimpleName() + " must contains no-args constructor, private is ok!",
                    typeElement);
        }
    }

    /**
     * 用于 {@link DBEntityProcessor#DB_FIELD_CANONICAL_NAME}注解和{@link #SERIALIZABLE_FIELD_CANONICAL_NAME}注解的字段是可以序列化的
     */
    boolean isSerializableField(VariableElement variableElement) {
        final Optional<? extends AnnotationMirror> a = AutoUtils.findAnnotationWithoutInheritance(typeUtils, variableElement, serializableFieldDeclaredType);
        if (a.isPresent()) {
            return true;
        }
        final Optional<? extends AnnotationMirror> b = AutoUtils.findAnnotationWithoutInheritance(typeUtils, variableElement, dbFieldDeclaredType);
        return b.isPresent();
    }

    /**
     * 是否包含非private的getter方法
     *
     * @param allFieldsAndMethodWithInherit 所有的字段和方法，可能在父类中
     */
    private boolean isContainerNotPrivateGetterMethod(final VariableElement variableElement, final List<? extends Element> allFieldsAndMethodWithInherit) {
        return BeanUtils.isContainerNotPrivateGetterMethod(typeUtils, variableElement, allFieldsAndMethodWithInherit);
    }

    private void checkMapAndCollectionField(VariableElement variableElement) {
        final DeclaredType declaredType = AutoUtils.getDeclaredType(variableElement.asType());
        if (!declaredType.asElement().getModifiers().contains(Modifier.ABSTRACT)) {
            // 声明类型是具体类型
            return;
        }

        if (isEnumMap(declaredType) || isEnumSet(declaredType)) {
            // 声明类型是EnumMap 或 EnumSet (需要特殊处理)
            return;
        }

        // 其它抽象类型必须有imp注解
        final TypeMirror implTypeMirror = getFieldImplAnnotationValue(variableElement);
        if (implTypeMirror == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Abstract MapOrCollection must contains impl annotation " + DBEntityProcessor.IMPL_CANONICAL_NAME,
                    variableElement);
            return;
        }

        if (!AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, implTypeMirror, variableElement.asType())) {
            // 实现类型必须是声明类型的子类
            messager.printMessage(Diagnostic.Kind.ERROR, "MapOrCollectionImpl must be field sub type", variableElement);
            return;
        }

        if (isEnumMap(implTypeMirror) || isEnumSet(implTypeMirror)) {
            // 实现类型是EnumMap或EnumSet
            return;
        }

        final DeclaredType impDeclaredType = (DeclaredType) implTypeMirror;
        if (impDeclaredType.asElement().getModifiers().contains(Modifier.ABSTRACT)) {
            // 其它实现类型必须是具体类型
            messager.printMessage(Diagnostic.Kind.ERROR, "MapOrCollectionImpl must can't be abstract class", variableElement);
            return;
        }

        final ExecutableElement noArgsConstructor = getOneIntArgsConstructor((TypeElement) impDeclaredType.asElement());
        if (noArgsConstructor == null || !noArgsConstructor.getModifiers().contains(Modifier.PUBLIC)) {
            // 必须包含只有一个int参数的构造方法
            messager.printMessage(Diagnostic.Kind.ERROR, "MapOrCollectionImpl must contains public one int arg constructor", variableElement);
        }
    }

    boolean isString(TypeMirror typeMirror) {
        return typeUtils.isSameType(typeMirror, stringTypeMirror);
    }

    private boolean isMapOrCollection(VariableElement variableElement) {
        return isMap(variableElement.asType()) || isCollection(variableElement.asType());
    }

    boolean isEnumSet(TypeMirror typeMirror) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, typeMirror, enumSetRawTypeMirror);
    }

    boolean isEnumMap(TypeMirror typeMirror) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, typeMirror, enumMapRawTypeMirror);
    }

    boolean isMap(TypeMirror typeMirror) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, typeMirror, mapTypeMirror);
    }

    boolean isCollection(TypeMirror typeMirror) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, typeMirror, collectionTypeMirror);
    }

    TypeMirror getFieldImplAnnotationValue(VariableElement variableElement) {
        final AnnotationMirror impAnnotationMirror = AutoUtils
                .findAnnotationWithoutInheritance(typeUtils, variableElement, impDeclaredType)
                .orElse(null);

        if (impAnnotationMirror == null) {
            return null;
        }

        final AnnotationValue annotationValue = AutoUtils.getAnnotationValueNotDefault(impAnnotationMirror, "value");
        assert null != annotationValue;
        return AutoUtils.getAnnotationValueTypeMirror(annotationValue);
    }

    @Nullable
    private static ExecutableElement getOneIntArgsConstructor(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(e -> (ExecutableElement) e)
                .filter(e -> e.getParameters().size() == 1)
                .filter(e -> e.getParameters().get(0).asType().getKind() == TypeKind.INT)
                .findFirst()
                .orElse(null);
    }

    // ----------------------------------------------- 辅助类生成 -------------------------------------------

    private void generateSerializer(TypeElement typeElement) {
        if (isNumericalEnum(typeElement)) {
            new NumericalEntitySerializerGenerator(this, typeElement).execute();
        } else if (isIndexableEntity(typeElement)) {
            new IndexableEntitySerializerGenerator(this, typeElement).execute();
        } else {
            new DefaultSerializerGenerator(this, typeElement).execute();
        }
    }

    /**
     * 创建writeObject方法
     */
    MethodSpec.Builder newWriteMethodBuilder(DeclaredType superDeclaredType) {
        return MethodSpec.overriding(writeObjectMethod, superDeclaredType, typeUtils);
    }

    /**
     * 创建readObject方法
     */
    MethodSpec.Builder newReadObjectMethodBuilder(DeclaredType superDeclaredType) {
        return MethodSpec.overriding(readObjectMethod, superDeclaredType, typeUtils);
    }

    /**
     * 创建返回负责被序列化的类对象的方法
     */
    MethodSpec newGetEntityMethod(DeclaredType superDeclaredType) {
        return MethodSpec.overriding(getEntityClassMethod, superDeclaredType, typeUtils)
                .addStatement("return $T.class", TypeName.get(superDeclaredType.getTypeArguments().get(0)))
                .build();
    }

    /**
     * 获取class对应的序列化工具类的类名
     */
    static String getSerializerClassName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString() + "Serializer";
    }
}
