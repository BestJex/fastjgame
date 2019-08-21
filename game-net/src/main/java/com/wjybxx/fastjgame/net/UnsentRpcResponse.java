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

package com.wjybxx.fastjgame.net;

/**
 * 发送缓冲区中尚未发送的rpc结果。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/3
 * github - https://github.com/hl845740757
 */
public class UnsentRpcResponse implements UnsentMessage{

	/** rpc请求id */
	private long requestGuid;
	/** 对应的结果 */
	private RpcResponse rpcResponse;

	public UnsentRpcResponse(long requestGuid, RpcResponse rpcResponse) {
		this.requestGuid = requestGuid;
		this.rpcResponse = rpcResponse;
	}

	@Override
	public SentMessage build(long sequence, MessageQueue messageQueue) {
		return new SentRpcResponse(sequence, requestGuid, rpcResponse);
	}
}
