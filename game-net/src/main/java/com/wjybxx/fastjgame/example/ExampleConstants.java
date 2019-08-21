/*
 *    Copyright 2019 wjybxx
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.wjybxx.fastjgame.example;

import com.wjybxx.fastjgame.misc.JsonBasedProtocolCodec;
import com.wjybxx.fastjgame.misc.MessageMapper;
import com.wjybxx.fastjgame.misc.ReflectBasedProtocolCodec;
import com.wjybxx.fastjgame.net.ProtocolCodec;
import com.wjybxx.fastjgame.net.RoleType;

/**
 * 测试用例的常量
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/6
 * github - https://github.com/hl845740757
 */
public final class ExampleConstants {

	/** 服务端信息 */
	public static final long serverGuid = 22222;
	public static final RoleType serverRole = RoleType.TEST_SERVER;
	/** 客户端信息 */
	public static final long clientGuid = 11111;
	public static final RoleType clientRole = RoleType.TEST_CLIENT;
	/** 客户端信息 */
	public static final long pipelineClientGuid = 33333;
	public static final RoleType pipelineClientRole = RoleType.TEST_CLIENT;

	/** 测试用例使用的codec */
	public static final MessageMapper messageMapper = MessageMapper.newInstance(new ExampleHashMappingStrategy());
	public static final JsonBasedProtocolCodec jsonBasedCodec = new JsonBasedProtocolCodec(messageMapper);
	public static final ReflectBasedProtocolCodec reflectBasedCodec = ReflectBasedProtocolCodec.newInstance(messageMapper);

	/** tcp端口 */
	public static final int tcpPort = 23333;
	/** http端口 */
	public static final int httpPort = 54321;

	private ExampleConstants() {

	}
}