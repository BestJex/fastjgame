/*
 *  Copyright 2019 wjybxx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wjybxx.fastjgame.auto;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.wjybxx.fastjgame.annotation.RpcMethod;
import com.wjybxx.fastjgame.annotation.RpcMethodProxy;
import com.wjybxx.fastjgame.annotation.RpcService;
import com.wjybxx.fastjgame.annotation.RpcServiceProxy;
import com.wjybxx.fastjgame.misc.RpcCall;
import com.wjybxx.fastjgame.misc.RpcFunctionRepository;
import com.wjybxx.fastjgame.net.RpcResponse;
import com.wjybxx.fastjgame.net.RpcResponseChannel;
import com.wjybxx.fastjgame.net.Session;
import com.wjybxx.fastjgame.utils.AutoUtils;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import com.wjybxx.fastjgame.utils.MathUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

import javax.annotation.Generated;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 为RpcService的注解生成工具类。
 * 我太难了...这套api根本都不熟悉，用着真难受。。。。。
 *
 * 由于类文件结构比较文件，API都是基于访问者模式的，完全不能直接获取数据，难受的一比。
 *
 * 注意：使用class对象有限制，只可以使用JDK自带的class 和 该jar包（该项目）中所有的class (包括依赖的jar包)
 *
 * 不在本包中的类，只能使用{@link Element}) 和 {@link javax.lang.model.type.TypeMirror}。
 * 因为需要使用的类可能还没编译，也就不存在对应的class文件，加载不到。
 * 这也是注解处理器必须要打成jar包的原因。。不然对应的注解类可能都还未被编译。。。。
 *
 * {@link RoundEnvironment} 运行环境，编译器环境。（他是一个独立的编译器，无法直接调试，需要远程调试）
 * {@link Types} 编译时的类型信息（非常类似 Class，但那是运行时的东西，注意现在是编译时）
 * {@link Filer} 文件读写 util (然而，Filer 有局限性，只有 create 相关的接口)
 * {@link Elements} 代码结构信息
 *
 * 遇见的坑，先记一笔：
 * 1. {@code types.isSameType(RpcResponseChannel<String>, RpcResponseChannel)  false} 带泛型和不带泛型的不是同一个类型。
 *    {@code types.isAssignable(RpcResponseChannel<String>, RpcResponseChannel)  true}
 *    {@code types.isSubType(RpcResponseChannel<String>, RpcResponseChannel)  true}
 *
 * 2. 在注解中引用另一个类时，这个类可能还未被编译，需要通过捕获异常获取到未编译的类的{@link TypeMirror}.
 *
 * {@link DeclaredType}是变量、参数的声明类型。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/19
 * github - https://github.com/hl845740757
 */
@AutoService(Processor.class)
public class RpcServiceProcessor extends AbstractProcessor {

	private static final String repository = "repository";
	private static final String session = "session";
	private static final String methodParams = "methodParams";
	private static final String responseChannel = "responseChannel";

	// 工具类
	private Types types;
	private Elements elements;
	private Messager messager;
	/**
	 * {@link RpcCall}对应的类型
	 */
	private TypeElement rpcCallElement;
	/**
	 * {@link Void}对应的类型
	 */
	private DeclaredType voidType;
	/**
	 * {@link RpcResponseChannel}对应的类型
	 */
	private DeclaredType responseChannelType;
	/**
	 * {@link com.wjybxx.fastjgame.net.Session}对应的类型
	 */
	private DeclaredType sessionType;

	/** 所有的serviceId集合，判断重复 */
	private final ShortSet serviceIdSet = new ShortOpenHashSet(128);
	/** 所有的methodKey集合，判断重复 */
	private final IntSet methodKeySet = new IntOpenHashSet(1024);
	/** 生成信息 */
	private AnnotationSpec generatedAnnotation;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		messager = processingEnv.getMessager();

		rpcCallElement = elements.getTypeElement(RpcCall.class.getCanonicalName());
		voidType = types.getDeclaredType(elements.getTypeElement(Void.class.getCanonicalName()));

		responseChannelType = types.getDeclaredType(elements.getTypeElement(RpcResponseChannel.class.getCanonicalName()));
		sessionType = types.getDeclaredType(elements.getTypeElement(Session.class.getCanonicalName()));

		generatedAnnotation = AnnotationSpec.builder(Generated.class)
				.addMember("value", "$S", RpcServiceProcessor.class.getCanonicalName())
				.build();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(RpcService.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;
	}

	/**
	 * 处理源自前一轮的类型元素的一组注解类型，并返回此处理器是否声明了这些注解类型。
	 * 如果返回true，则声明注释类型，并且不会要求后续处理器处理它们;
	 * 如果返回false，则注释类型无人认领，并且可能要求后续处理器处理它们。 处理器可以始终返回相同的布尔值，或者可以基于所选择的标准改变结果。
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// 该注解只有类才可以使用
		@SuppressWarnings("unchecked")
		Set<TypeElement> typeElementSet = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(RpcService.class);

		for (TypeElement typeElement:typeElementSet) {
			genProxyClass(typeElement);
		}
		return true;
	}

	private void genProxyClass(TypeElement typeElement) {
		final String pkgName = ((PackageElement)(typeElement.getEnclosingElement())).getQualifiedName().toString();
		final String className = typeElement.getSimpleName().toString();
		// 筛选rpc方法
		final List<ExecutableElement> proxyMethods = typeElement.getEnclosedElements().stream()
				.filter(element -> element.getKind() == ElementKind.METHOD)
				.map(element -> (ExecutableElement) element)
				.filter(element -> element.getAnnotation(RpcMethod.class) != null)
				.sorted(Comparator.comparingInt(e -> e.getAnnotation(RpcMethod.class).methodId()))
				.collect(Collectors.toList());
		// proxyMethods.size() == 0 也必须重新生成文件

		final short serviceId = typeElement.getAnnotation(RpcService.class).serviceId();
		if (!serviceIdSet.add(serviceId)) {
			// 打印重复serviceId
			messager.printMessage(Diagnostic.Kind.ERROR, className + " serviceId " + serviceId + " is duplicate!");
		}

		List<MethodSpec> clientMethodProxyList = new ArrayList<>(proxyMethods.size());
		List<MethodSpec> serverMethodProxyList = new ArrayList<>(proxyMethods.size());

		// 生成代理方法
		for (ExecutableElement method:proxyMethods) {
			final String methodName = method.getSimpleName().toString();
			if (method.isVarArgs()) {
				messager.printMessage(Diagnostic.Kind.ERROR,  className + " - " + methodName + " contains varArgs!", method);
				continue;
			}
			if (!method.getModifiers().contains(Modifier.PUBLIC)) {
				messager.printMessage(Diagnostic.Kind.ERROR,  className + " - " + methodName + " is not public！", method);
				continue;
			}

			// 方法id
			final short methodId = method.getAnnotation(RpcMethod.class).methodId();
			// 方法的唯一键，乘以1W比位移有更好的可读性
			final int methodKey = MathUtils.safeMultiplyShort(serviceId, (short) 10000) + methodId;
			// 重复检测
			if (!methodKeySet.add(methodKey)) {
				messager.printMessage(Diagnostic.Kind.ERROR, className + " - " + methodName + " methodKey " + methodKey + " is duplicate!");
			}
			// 生成双方的代理代码
			clientMethodProxyList.add(genClientMethodProxy(methodKey, method));
			serverMethodProxyList.add(genServerMethodProxy(methodKey, method));
		}

		// 保存serviceId
		AnnotationSpec proxyAnnotation = AnnotationSpec.builder(RpcServiceProxy.class)
				.addMember("serviceId", "$L", serviceId).build();

		// 代理类不可以继承
		TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className + "Proxy")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addAnnotation(generatedAnnotation)
				.addAnnotation(proxyAnnotation);

		// 添加客户端代理方法，在最上面
		typeBuilder.addMethods(clientMethodProxyList);
		// 添加注册方法
		typeBuilder.addMethod(genRegisterMethod(typeElement, serverMethodProxyList));
		// 添加服务器的代理方法，私有方法在最下面
		typeBuilder.addMethods(serverMethodProxyList);

		TypeSpec typeSpec = typeBuilder.build();
		JavaFile javaFile = JavaFile
				.builder(pkgName, typeSpec)
				// 不用导入java.lang包
				.skipJavaLangImports(true)
				// 4空格缩进
				.indent("    ")
				.build();
		try {
			// 输出到注解处理器配置的路径下，这样才可以在下一轮检测到并进行编译 输出到processingEnv.getFiler()会立即参与编译
			// 如果自己指定路径，可以生成源码到指定路径，但是可能无法被编译器检测到，本轮无法参与编译，需要再进行一次编译
			javaFile.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
		}
	}

	// ----------------------------------------- 为客户端生成代理方法 -------------------------------------------

	private MethodSpec genClientMethodProxy(int methodKey, ExecutableElement method) {
		// 工具方法 public static RpcCall<V>
		MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString());
		builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		// 拷贝泛型参数
		AutoUtils.copyTypeVariableNames(builder, method);

		// 解析方法参数 -- 提炼方法是为了减少当前方法的长度
		final ParseResult parseResult = parseParameters(method);
		final List<VariableElement> availableParameters = parseResult.availableParameters;
		final TypeMirror callReturnType = parseResult.callReturnType;
		final boolean allowCallback = parseResult.allowCallback;

		// 添加返回类型
		DeclaredType realReturnType = types.getDeclaredType(rpcCallElement, callReturnType);
		builder.returns(ClassName.get(realReturnType));
		// 拷贝参数列表
		AutoUtils.copyParameters(builder, availableParameters);
		// 注明方法键值
		AnnotationSpec annotationSpec = AnnotationSpec.builder(RpcMethodProxy.class)
				.addMember("methodKey", "$L", methodKey)
				.build();
		builder.addAnnotation(annotationSpec);

		// 搜集参数代码块
		if (availableParameters.size() == 0) {
			builder.addStatement("return new $T($L, $T.emptyList(), $L)", RpcCall.class, methodKey, Collections.class, allowCallback);
		} else if (availableParameters.size() == 1) {
			final String firstParameterName = availableParameters.get(0).getSimpleName().toString();
			builder.addStatement("return new $T($L, $T.singletonList($L), $L)", RpcCall.class, methodKey, Collections.class, firstParameterName, allowCallback);
		} else {
			builder.addStatement("$T<Object> $L = new $T<>($L)", List.class, methodParams, ArrayList.class, availableParameters.size());
			for (VariableElement variableElement:availableParameters) {
				builder.addStatement("$L.add($L)", methodParams, variableElement.getSimpleName());
			}
			builder.addStatement("return new $T($L, $L, $L)", RpcCall.class, methodKey, "methodParams", allowCallback);
		}
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private ParseResult parseParameters(ExecutableElement method) {
		final String className = method.getEnclosingElement().getSimpleName().toString();
		final String methodName = method.getSimpleName().toString();
		// 原始参数列表
		final List<VariableElement> originParameters = (List<VariableElement>) method.getParameters();
		// 有效参数列表
		final List<VariableElement> availableParameters = new ArrayList<>(originParameters.size());
		// 返回值类型
		TypeMirror callReturenType = null;
		// 是否允许回调--是否是单向消息，返回类型是否是void/Void
		boolean allowCallback;

		// 筛选参数
		for (int index = 0; index < originParameters.size(); index ++) {
			VariableElement variableElement = originParameters.get(index);
			// rpcResponseChannel需要从参数列表删除，并捕获泛型类型
			if (callReturenType == null && isResponseChannel(variableElement)) {
				callReturenType = getResponseChannelReturnType(className, methodName, variableElement.asType());
				continue;
			}
			// session需要从参数列表删除
			if (isSession(variableElement)) {
				continue;
			}
			availableParameters.add(variableElement);
		}
		if (null == callReturenType) {
			// 参数列表中不存在responseChannel
			callReturenType = method.getReturnType();
		} else {
			// 如果参数列表中存在responseChannel，那么返回值必须是void
			if (!isVoidType(method.getReturnType())){
				messager.printMessage(Diagnostic.Kind.ERROR, "callReturnType is not void, and parameters contains responseChannel!", method);
			}
		}
		if (isVoidType(callReturenType)) {
			allowCallback = false;
			// void转Void
			callReturenType = voidType;
		} else {
			allowCallback = true;
		}
		return new ParseResult(availableParameters, callReturenType, allowCallback);
	}

	/**
	 * 判断指定类型是否是void 或Void类型
	 */
	private boolean isVoidType(TypeMirror typeMirror) {
		return typeMirror.getKind() == TypeKind.VOID || types.isSameType(typeMirror, voidType);
	}

	/**
	 * 是否是 {@link RpcResponseChannel} 类型。
	 */
	private boolean isResponseChannel(VariableElement variableElement) {
		return isTargetType(variableElement, declaredType -> types.isAssignable(declaredType, responseChannelType));
	}

	/**
	 * 是否是 {@link Session}类型
	 */
	private boolean isSession(VariableElement variableElement) {
		return isTargetType(variableElement, declaredType -> types.isSubtype(declaredType, sessionType));
	}

	/**
	 * 不能访问{@link VariableElement}，会死循环，必须转换成typeMirror访问，否则无法访问到详细信息。
	 */
	private boolean isTargetType(final VariableElement variableElement, final Predicate<DeclaredType> matcher) {
		return variableElement.asType().accept(new SimpleTypeVisitor8<Boolean, Void>(){

			@Override
			public Boolean visitDeclared(DeclaredType t, Void aVoid) {
				// 访问声明的类型
				return matcher.test(t);
			}

			@Override
			protected Boolean defaultAction(TypeMirror e, Void aVoid) {
				return false;
			}

		}, null);
	}

	/**
	 * 获取RpcResponseChannel的泛型参数
	 */
	private TypeMirror getResponseChannelReturnType(String className, String methodName, TypeMirror typeMirror) {
		return typeMirror.accept(new SimpleTypeVisitor8<TypeMirror, Void>(){
			@Override
			public TypeMirror visitDeclared(DeclaredType t, Void aVoid) {
				if (t.getTypeArguments().size() > 0) {
					// 第一个参数就是返回值类型
					return t.getTypeArguments().get(0);
				} else {
					// 声明类型木有泛型参数，返回Object类型，并打印一个警告
					messager.printMessage(Diagnostic.Kind.WARNING, className + "-" + methodName + "RpcResponseChannel missing  type parameter.");
					return elements.getTypeElement(Object.class.getCanonicalName()).asType();
				}
			}
		}, null);
	}

	// --------------------------------------------- 为服务端生成代理方法 ---------------------------------------

	/**
	 * 生成注册方法
	 * {@code
	 * 		public static void register(RpcFunctionRepository repository, T className) {
	 * 		 	registerGetMethod1(repository, t);
	 * 		 	registerGetMethod2(repository, t);
	 * 		}
	 * }
	 * @param typeElement 类信息
	 * @param serverProxyMethodList 被代理的服务器方法
	 */
	private MethodSpec genRegisterMethod(TypeElement typeElement, List<MethodSpec> serverProxyMethodList) {
		final String className = typeElement.getSimpleName().toString();
		final String classParamName = AutoUtils.firstCharToLowerCase(className);
		MethodSpec.Builder builder = MethodSpec.methodBuilder("register")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(TypeName.VOID)
				.addParameter(RpcFunctionRepository.class, repository)
				.addParameter(TypeName.get(typeElement.asType()), classParamName);

		// 添加调用
		for (MethodSpec method:serverProxyMethodList) {
			builder.addStatement("$L($L, $L)", method.name, repository, classParamName);
		}
		return builder.build();
	}

	/**
	 * 为某个具体方法生成注册方法
	 * {@code
	 * 		private static void registerGetMethod1(RpcFunctionRepository repository, T className) {
	 * 		    repository.register(10001, (session, methodParams, responseChannel) -> {
	 * 		       // code-
	 * 		       className.method1(method.get(0), method.get(1), responseChannel);
	 * 		    });
	 * 		}
	 * }
	 * @param executableElement rpcMethod
	 * @return methodName
	 */
	private MethodSpec genServerMethodProxy(int methodKey, ExecutableElement executableElement) {
		final TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
		final String classParamName = AutoUtils.firstCharToLowerCase(typeElement.getSimpleName().toString());

		// 加上methodKey防止重复
		final String methodName = "_register" + AutoUtils.firstCharToUpperCase(executableElement.getSimpleName().toString()) + "_" + methodKey;
		MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
				.returns(TypeName.VOID)
				.addParameter(RpcFunctionRepository.class, repository)
				.addParameter(TypeName.get(typeElement.asType()), classParamName);

		builder.addCode("$L.register($L, ($L, $L, $L) -> {\n", repository, methodKey,
				session, methodParams, responseChannel);

		if (!isVoidType(executableElement.getReturnType())) {
			// 同步返回结果
			builder.addCode("    try {\n");
			builder.addCode("        $L.writeSuccess(", responseChannel);
			builder.addCode(genInvokeStatement(classParamName, executableElement) + ");\n");
			builder.addCode("    } catch (Exception e) {\n");
			builder.addCode("        // 失败立即返回结果，防止对方超时\n");
			builder.addStatement("        $L.write($T.ERROR)", responseChannel, RpcResponse.class);
			builder.addStatement("        $T.rethrow(e)", ConcurrentUtils.class);
			builder.addCode("    }");
		} else  {
			// 异步返回结果 或 没有结果
			builder.addStatement("    " + genInvokeStatement(classParamName, executableElement));
		}
		builder.addStatement("})");
		return builder.build();
	}

	/**
	 * 生成方法调用代码，没有分号和换行符。
	 */
	private CodeBlock genInvokeStatement(String classParamName, ExecutableElement executableElement) {
		final CodeBlock.Builder builder = CodeBlock.builder();
		builder.add("$L.$L(", classParamName, executableElement.getSimpleName());

		boolean needDelimiter = false;
		int index = 0;
		for (VariableElement variableElement:executableElement.getParameters()) {
			if (needDelimiter) {
				builder.add(", ");
			} else {
				needDelimiter = true;
			}

			if (isResponseChannel(variableElement)) {
				builder.add(responseChannel);
			} else if (isSession(variableElement)){
				builder.add(session);
			} else {
				TypeName typeName = ParameterizedTypeName.get(variableElement.asType());
				if (typeName.isPrimitive()) {
					// 基本类型需要转换为包装类型
					typeName = typeName.box();
				}
				// 这里的T不导包有点坑爹
				builder.add("($T)($L.get($L))", typeName, methodParams, index);
				index++;
			}
		}
		builder.add(")");
		return builder.build();
	}

	private static class ParseResult {
		// 除去特殊参数余下的参数
		private final List<VariableElement> availableParameters;
		// 远程调用的返回值类型
		private final TypeMirror callReturnType;
		// 是否允许回调
		private final boolean allowCallback;

		ParseResult(List<VariableElement> availableParameters, TypeMirror callReturnType, boolean allowCallback) {
			this.availableParameters = availableParameters;
			this.callReturnType = callReturnType;
			this.allowCallback = allowCallback;
		}
	}
}