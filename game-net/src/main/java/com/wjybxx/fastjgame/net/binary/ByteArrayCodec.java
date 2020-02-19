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

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/17
 */
class ByteArrayCodec implements BinaryCodec<byte[]> {
    @Override
    public boolean isSupport(Class<?> runtimeType) {
        return runtimeType == byte[].class;
    }

    @Override
    public void writeData(CodedOutputStream outputStream, @Nonnull byte[] instance) throws Exception {
        outputStream.writeUInt32NoTag(instance.length);
        if (instance.length > 0) {
            outputStream.writeRawBytes(instance);
        }
    }

    @Nonnull
    @Override
    public byte[] readData(CodedInputStream inputStream) throws Exception {
        final int length = inputStream.readUInt32();
        if (length == 0) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        return inputStream.readRawBytes(length);
    }

    @Override
    public byte getWireType() {
        return WireType.BYTE_ARRAY;
    }

}