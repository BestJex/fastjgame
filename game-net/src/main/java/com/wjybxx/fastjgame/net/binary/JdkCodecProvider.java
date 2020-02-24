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

import com.wjybxx.fastjgame.utils.EnumUtils;
import com.wjybxx.fastjgame.utils.entity.NumericalEntityMapper;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/24
 */
public class JdkCodecProvider implements CodecProvider {

    public static final JdkCodecProvider INSTANCE = new JdkCodecProvider();

    private final Map<Class<?>, JDKObjectCodec<?>> type2CodecMap = new IdentityHashMap<>(16);
    private final NumericalEntityMapper<JDKObjectCodec<?>> classId2CodecMap;

    private JdkCodecProvider() {
        addCodec(new ByteCodec(1));
        addCodec(new CharCodec(2));
        addCodec(new ShortCodec(3));
        addCodec(new IntegerCodec(4));
        addCodec(new LongCodec(5));
        addCodec(new FloatCodec(6));
        addCodec(new DoubleCodec(7));
        addCodec(new BooleanCodec(8));

        addCodec(new StringCodec(9));
        addCodec(new ClassCodec(10));

        classId2CodecMap = EnumUtils.mapping(type2CodecMap.values().toArray(JDKObjectCodec<?>[]::new), true);
    }

    private void addCodec(JDKObjectCodec<?> codec) {
        type2CodecMap.put(codec.getEncoderClass(), codec);
    }

    @Override
    public int getProviderId() {
        return CodecProviderConst.JDK_PROVIDER_ID;
    }

    @Nullable
    @Override
    public Codec<?> getCodec(int classId) {
        return classId2CodecMap.forNumber(classId);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> Codec<T> getCodec(Class<T> clazz) {
        return (Codec<T>) type2CodecMap.get(clazz);
    }
}
