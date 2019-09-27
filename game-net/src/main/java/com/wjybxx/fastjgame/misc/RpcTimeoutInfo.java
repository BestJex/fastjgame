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

package com.wjybxx.fastjgame.misc;

import com.wjybxx.fastjgame.concurrent.Promise;
import com.wjybxx.fastjgame.net.RpcCallback;
import com.wjybxx.fastjgame.net.RpcResponse;

import javax.annotation.Nonnull;

/**
 * rpc请求超时信息
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/3
 * github - https://github.com/hl845740757
 */
public class RpcTimeoutInfo {

    // promise与callback二者存一
    /**
     * promise
     */
    public Promise<RpcResponse> rpcPromise;
    /**
     * 回调
     */
    public RpcCallback rpcCallback;

    /**
     * rpc超时时间
     */
    public long deadline;

    private RpcTimeoutInfo(Promise<RpcResponse> rpcPromise, RpcCallback rpcCallback, long deadline) {
        this.rpcPromise = rpcPromise;
        this.rpcCallback = rpcCallback;
        this.deadline = deadline;
    }

    public static RpcTimeoutInfo newInstance(@Nonnull Promise<RpcResponse> rpcPromise, long deadline) {
        return new RpcTimeoutInfo(rpcPromise, null, deadline);
    }

    public static RpcTimeoutInfo newInstance(@Nonnull RpcCallback rpcCallback, long deadline) {
        return new RpcTimeoutInfo(null, rpcCallback, deadline);
    }
}
