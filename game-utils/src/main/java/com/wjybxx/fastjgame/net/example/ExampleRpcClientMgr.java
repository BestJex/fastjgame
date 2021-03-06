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

package com.wjybxx.fastjgame.net.example;

import com.wjybxx.fastjgame.net.rpc.DefaultRpcInvoker;
import com.wjybxx.fastjgame.net.rpc.RpcInvoker;
import com.wjybxx.fastjgame.net.rpc.RpcMethodSpec;
import com.wjybxx.fastjgame.net.session.Session;
import com.wjybxx.fastjgame.util.concurrent.FluentFuture;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionException;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2020/3/8
 * github - https://github.com/hl845740757
 */
public class ExampleRpcClientMgr {

    private final RpcInvoker invoker = new DefaultRpcInvoker();

    public void send(@Nonnull Session session, @Nonnull RpcMethodSpec<?> message) {
        invoker.send(session, message, false);
    }

    public void sendAndFlush(@Nonnull Session session, @Nonnull RpcMethodSpec<?> message) {
        invoker.send(session, message, true);
    }

    public <V> FluentFuture<V> call(@Nonnull Session session, @Nonnull RpcMethodSpec<V> request) {
        return invoker.call(session, request, false);
    }

    public <V> FluentFuture<V> callAndFlush(@Nonnull Session session, @Nonnull RpcMethodSpec<V> request) {
        return invoker.call(session, request, true);
    }

    public <V> V syncCall(@Nonnull Session session, @Nonnull RpcMethodSpec<V> request) throws CompletionException {
        return invoker.syncCall(session, request);
    }
}
