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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.annotation.WorldSingleton;
import com.wjybxx.fastjgame.misc.DefaultRpcFunctionRegistry;
import com.wjybxx.fastjgame.misc.RpcCall;
import com.wjybxx.fastjgame.misc.VoidRpcResponseChannel;
import com.wjybxx.fastjgame.net.MessageHandler;
import com.wjybxx.fastjgame.net.RpcRequestContext;
import com.wjybxx.fastjgame.net.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * 实现TCP/Ws长链接的 [单向消息] 和 [rpc请求] 的分发。
 * 注意：不同的world有不同的消息处理器，单例级别为world级别。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/4
 * github - https://github.com/hl845740757
 */
@WorldSingleton
@NotThreadSafe
public class MessageDispatcherMrg extends DefaultRpcFunctionRegistry implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcherMrg.class);

    @Inject
    public MessageDispatcherMrg() {

    }

    @Override
    public final void onMessage(Session session, @Nullable Object message) throws Exception {
        if (null == message){
            logger.warn("{} - {} send null message", session.remoteRole(), session.remoteGuid());
            return;
        }
        if (message instanceof RpcCall) {
            dispatchRpcRequest(session, (RpcCall) message, VoidRpcResponseChannel.INSTANCE);
        } else {
            onMessage0(session, message);
        }
    }

    /**
     * 接收到一个单向消息
     * @param session 所在的会话
     * @param message 单向消息
     * @throws Exception error
     */
    protected void onMessage0(Session session, @Nonnull Object message) throws Exception {
        logger.info("unhandled {}-{} message {}", session.remoteRole(), session.remoteGuid(), message.getClass().getSimpleName());
    }

    @Override
    public final void onRpcRequest(Session session, @Nullable Object request, RpcRequestContext context) throws Exception {
        if (null == request){
            logger.warn("{} - {} send null request", session.remoteRole(), session.remoteGuid());
            return;
        }
        // 目前版本直接session创建responseChannel，后期再考虑缓存的事情
        dispatchRpcRequest(session, (RpcCall) request, session.newResponseChannel(context));
    }

}
