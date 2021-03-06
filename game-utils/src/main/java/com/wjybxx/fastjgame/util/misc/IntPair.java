/*
 * Copyright 2019 wjybxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wjybxx.fastjgame.util.misc;

import com.wjybxx.fastjgame.net.binary.ObjectReader;
import com.wjybxx.fastjgame.net.binary.ObjectWriter;
import com.wjybxx.fastjgame.net.binary.PojoCodecImpl;
import com.wjybxx.fastjgame.util.MathUtils;

/**
 * int值对
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/11/2
 * github - https://github.com/hl845740757
 */
public class IntPair {

    private final int first;
    private final int second;

    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public long composeToLong() {
        return MathUtils.composeIntToLong(first, second);
    }

    @Override
    public String toString() {
        return "IntPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @SuppressWarnings("unused")
    private static class Codec implements PojoCodecImpl<IntPair> {

        @Override
        public Class<IntPair> getEncoderClass() {
            return IntPair.class;
        }

        @Override
        public IntPair readObject(ObjectReader reader) throws Exception {
            return new IntPair(reader.readInt(), reader.readInt());
        }

        @Override
        public void writeObject(IntPair instance, ObjectWriter writer) throws Exception {
            writer.writeInt(instance.first);
            writer.writeInt(instance.second);
        }
    }
}
