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

package com.wjybxx.fastjgame.net.rpc;

import com.wjybxx.fastjgame.utils.concurrent.EventLoop;
import com.wjybxx.fastjgame.utils.concurrent.FailedFutureListener;
import com.wjybxx.fastjgame.utils.concurrent.FutureListener;
import com.wjybxx.fastjgame.utils.concurrent.SucceededFutureListener;
import com.wjybxx.fastjgame.utils.concurrent.timeout.FailedTimeoutFuture;
import com.wjybxx.fastjgame.utils.concurrent.timeout.TimeoutFutureListener;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * 已完成的Rpc调用，在它上面的任何监听都将立即执行。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/3
 * github - https://github.com/hl845740757
 */
public class FailedRpcFuture<V> extends FailedTimeoutFuture<V> implements RpcFuture<V> {

    public FailedRpcFuture(@Nonnull EventLoop notifyExecutor, @Nonnull Throwable cause) {
        super(notifyExecutor, cause);
    }

    @Override
    public final boolean isRpcException() {
        return DefaultRpcPromise.isRpcException0(cause());
    }

    @Override
    public final RpcErrorCode errorCode() {
        return DefaultRpcPromise.getErrorCode0(cause());
    }

    // ------------------------------------------------ 流式语法支持 ------------------------------------

    @Override
    public RpcFuture<V> await() {
        return this;
    }

    @Override
    public RpcFuture<V> awaitUninterruptibly() {
        return this;
    }

    @Override
    public RpcFuture<V> onComplete(@Nonnull FutureListener<? super V> listener) {
        super.onComplete(listener);
        return this;
    }

    @Override
    public RpcFuture<V> onComplete(@Nonnull FutureListener<? super V> listener, @Nonnull Executor bindExecutor) {
        super.onComplete(listener, bindExecutor);
        return this;
    }

    @Override
    public RpcFuture<V> onSuccess(@Nonnull SucceededFutureListener<? super V> listener) {
        super.onSuccess(listener);
        return this;
    }

    @Override
    public RpcFuture<V> onSuccess(@Nonnull SucceededFutureListener<? super V> listener, @Nonnull Executor bindExecutor) {
        super.onSuccess(listener, bindExecutor);
        return this;
    }

    @Override
    public RpcFuture<V> onFailure(@Nonnull FailedFutureListener<? super V> listener) {
        super.onFailure(listener);
        return this;
    }

    @Override
    public RpcFuture<V> onFailure(@Nonnull FailedFutureListener<? super V> listener, @Nonnull Executor bindExecutor) {
        super.onFailure(listener, bindExecutor);
        return this;
    }

    @Override
    public RpcFuture<V> onTimeout(@Nonnull TimeoutFutureListener<? super V> listener) {
        super.onTimeout(listener);
        return this;
    }

    @Override
    public RpcFuture<V> onTimeout(@Nonnull TimeoutFutureListener<? super V> listener, @Nonnull Executor bindExecutor) {
        super.onTimeout(listener, bindExecutor);
        return this;
    }
}