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

package com.wjybxx.fastjgame.example;

import com.wjybxx.fastjgame.misc.JsonBasedProtocolCodec;
import com.wjybxx.fastjgame.misc.ReflectBasedProtocolCodec;
import com.wjybxx.fastjgame.net.common.ProtocolCodec;

import java.io.IOException;

/**
 * 序列化性能测试
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/10/10
 * github - https://github.com/hl845740757
 */
public class SerializePerformanceTesting {

    public static void main(String[] args) throws IOException {
        ExampleMessages.FullMessage fullMessage = ReflectBasedProtoCodecTest.newFullMessage();

        JsonBasedProtocolCodec jsonBasedCodec = ExampleConstants.jsonBasedCodec;
        ReflectBasedProtocolCodec reflectBasedCodec = ExampleConstants.reflectBasedCodec;

        // equals测试，正确性必须要保证
        equalsTest(jsonBasedCodec, fullMessage);
        System.out.println();

        equalsTest(reflectBasedCodec, fullMessage);
        System.out.println();

        // 预热
        codecTest(jsonBasedCodec, fullMessage, 1000);
        codecTest(reflectBasedCodec, fullMessage, 1000);
        System.out.println();

        // 开搞
        codecTest(jsonBasedCodec, fullMessage, 10_0000);
        codecTest(reflectBasedCodec, fullMessage, 10_0000);
    }

    private static void equalsTest(ProtocolCodec codec, ExampleMessages.FullMessage fullMessage) throws IOException {
        final String name = codec.getClass().getSimpleName();
        byte[] byteBuf = codec.serializeToBytes(fullMessage);
        System.out.println(name + " encode result bytes = " + byteBuf.length);

        Object decodeMessage = codec.deserializeFromBytes(byteBuf);
        System.out.println(name + " codec equals result = " + fullMessage.equals(decodeMessage));
    }

    private static void codecTest(ProtocolCodec codec, ExampleMessages.FullMessage fullMessage, int loopTimes) throws IOException {
        final String name = codec.getClass().getSimpleName();
        long start = System.currentTimeMillis();
        for (int index = 0; index < loopTimes; index++) {
            byte[] byteBuf = codec.serializeToBytes(fullMessage);
            Object decodeMessage = codec.deserializeFromBytes(byteBuf);
        }
        System.out.println(name + " codec " + loopTimes + " times cost timeMs " + (System.currentTimeMillis() - start));
    }
}