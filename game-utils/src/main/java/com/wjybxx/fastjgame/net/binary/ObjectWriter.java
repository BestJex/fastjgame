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

import com.google.protobuf.MessageLite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/13
 * github - https://github.com/hl845740757
 */
public interface ObjectWriter extends AutoCloseable {

    /**
     * 获取关联的{@link BinarySerializer}
     */
    BinarySerializer serializer();

    /**
     * 获取关联的{@link CodecRegistry}
     */
    CodecRegistry codecRegistry();

    void writeInt(int value) throws Exception;

    void writeLong(long value) throws Exception;

    void writeFloat(float value) throws Exception;

    void writeDouble(double value) throws Exception;

    void writeShort(short value) throws Exception;

    void writeBoolean(boolean value) throws Exception;

    void writeByte(byte value) throws Exception;

    void writeChar(char value) throws Exception;

    void writeString(@Nullable String value) throws Exception;

    /**
     * 向输出流中写入一个字节数组
     */
    void writeBytes(@Nullable byte[] value) throws Exception;

    /**
     * 向输出流中写入一个字节数组，并可以指定偏移量和长度
     */
    void writeBytes(@Nonnull byte[] bytes, int offset, int length) throws Exception;

    /**
     * 向输出流中写入一个字段，如果没有对应的简便方法，可以使用该方法
     *
     * @param value 字段的值
     */
    void writeObject(@Nullable Object value) throws Exception;

    /**
     * 写入一个需要延迟序列化的对象。
     * 如果该参数不是bytes，则会先序列化为bytes，再以bytes写入输出流。
     * 主要目的：期望减少额外的字节数组创建。
     */
    void writeLazySerializeObject(@Nullable Object value) throws Exception;

    // ---------------------------------------- 多态问题 ----------------------------------

    /**
     * 向输出流中写一个实体对象（按照超类格式写入数据，并忽略子类字段）
     *
     * @param superClass 实体对象的指定超类型
     */
    <T> void writeObject(@Nonnull T value, @Nonnull Class<? super T> superClass) throws Exception;

    // ----------------------------------------- 其它 ----------------------------------

    /**
     * 向输出流中写入一个protoBuf消息
     */
    void writeMessage(@Nullable MessageLite messageLite) throws Exception;

    /**
     * 向输出流中写入一个数组
     */
    void writeArray(@Nullable Object array) throws Exception;

    /**
     * 向输出流中写一个collection
     */
    void writeCollection(@Nullable Collection<?> collection) throws Exception;

    /**
     * 向输出流中写入一个map
     */
    void writeMap(@Nullable Map<?, ?> map) throws Exception;

    /**
     * 如果存在缓冲区，则刷新缓冲区
     */
    void flush() throws Exception;

}
