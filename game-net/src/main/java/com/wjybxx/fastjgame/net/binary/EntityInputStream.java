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

package com.wjybxx.fastjgame.net.binary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * 普通JavaBean对象输入流
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/13
 * github - https://github.com/hl845740757
 */
public interface EntityInputStream {

    /**
     * 从输入流中读取一个字段
     * (方便手写实现)
     */
    default <T> T readField() throws IOException {
        return readField(WireType.RUN_TIME);
    }

    /**
     * 从输入流中读取一个字段
     * (给生成代码使用的)。
     *
     * @param wireType 期望的数据类型，主要用于校验。如果该值不为{@link WireType#RUN_TIME}，则需要和读取到的tag进行比较。
     * @return data
     * @throws IOException error
     */
    @Nullable
    <T> T readField(byte wireType) throws IOException;

    /**
     * 从输入流中读取数据到map中
     *
     * @param mapFactory 创建map的工厂 - 参数为元素个数，可能为0
     */
    @Nullable
    <M extends Map<K, V>, K, V> M readMap(@Nonnull IntFunction<M> mapFactory) throws IOException;

    /**
     * 从输入流中读取数据到collection中
     *
     * @param collectionFactory 创建集合的工厂 - 参数为元素个数，可能为0
     */
    @Nullable
    <C extends Collection<E>, E> C readCollection(@Nonnull IntFunction<C> collectionFactory) throws IOException;

}
