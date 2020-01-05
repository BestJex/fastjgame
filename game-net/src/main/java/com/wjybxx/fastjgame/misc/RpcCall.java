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

package com.wjybxx.fastjgame.misc;


import com.wjybxx.fastjgame.annotation.SerializableClass;
import com.wjybxx.fastjgame.annotation.SerializableField;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

/**
 * 较为标准的rpc调用。
 * 推荐使用该方式，但并不限制rpc调用的形式！
 * 注意：如果使用该形式的rpc调用，请保证{@link RpcCall}在{@link MessageMapper}中存在。
 * 警告：不要修改对象的内容，否则可能引发bug(并发错误)。
 *
 * @param <V> the type of return type
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/19
 * github - https://github.com/hl845740757
 */
@SuppressWarnings("unused")
@NotThreadSafe
@SerializableClass
public class RpcCall<V> {

    /**
     * 调用的远程方法，用于确定一个唯一的方法。不使用 服务名 + 方法名 + 方法具体参数信息，传输的内容量过于庞大，性能不好。
     */
    @SerializableField(number = 1)
    private final int methodKey;

    /**
     * 方法参数列表
     */
    @SerializableField(number = 2)
    private final List<Object> methodParams;

    /**
     * 需要延迟到网络层序列化为byte[]的参数位置信息。
     * <p>
     * 2020年1月4日修改为需要序列化，原因：我们希望可以转发rpcCall对象，中间的代理需要有原始的rpcCall信息。
     */
    @SerializableField(number = 3)
    private final int lazyIndexes;
    /**
     * 需要网络层提前反序列化的参数位置信息 - 需要序列化到接收方。
     */
    @SerializableField(number = 4)
    private final int preIndexes;

    // 反射创建对象
    private RpcCall() {
        methodKey = -1;
        methodParams = null;
        lazyIndexes = 0;
        preIndexes = 0;
    }

    public RpcCall(int methodKey, List<Object> methodParams, int lazyIndexes, int preIndexes) {
        this.methodKey = methodKey;
        this.methodParams = methodParams;
        this.lazyIndexes = lazyIndexes;
        this.preIndexes = preIndexes;
    }

    public int getMethodKey() {
        return methodKey;
    }

    public List<Object> getMethodParams() {
        return methodParams;
    }

    public int getLazyIndexes() {
        return lazyIndexes;
    }

    public int getPreIndexes() {
        return preIndexes;
    }
}
