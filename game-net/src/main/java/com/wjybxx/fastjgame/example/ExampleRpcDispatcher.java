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

import com.wjybxx.fastjgame.misc.RpcCall;
import com.wjybxx.fastjgame.misc.RpcFunctionRegistry;
import com.wjybxx.fastjgame.misc.VoidRpcResponseChannel;
import com.wjybxx.fastjgame.net.ProtocolDispatcher;
import com.wjybxx.fastjgame.net.RpcRequestContext;
import com.wjybxx.fastjgame.net.Session;

import javax.annotation.Nullable;

/**
 * rpc请求分发器示例
 * @author houlei
 * @version 1.0
 * date - 2019/8/26
 */
public class ExampleRpcDispatcher implements ProtocolDispatcher {

	private final RpcFunctionRegistry registry;

	public ExampleRpcDispatcher(RpcFunctionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onRpcRequest(Session session, @Nullable Object request, RpcRequestContext context) throws Exception {
		if (request instanceof RpcCall) {
			registry.dispatchRpcRequest(session, (RpcCall) request, session.newResponseChannel(context));
		}
	}

	@Override
	public void onMessage(Session session, @Nullable Object message) throws Exception {
		if (message instanceof RpcCall) {
			registry.dispatchRpcRequest(session, (RpcCall) message, VoidRpcResponseChannel.INSTANCE);
		}
	}
}
