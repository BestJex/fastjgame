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

import com.wjybxx.fastjgame.net.binary.LazySerializable;
import com.wjybxx.fastjgame.net.binary.PreDeserializable;
import com.wjybxx.fastjgame.net.rpc.RpcMethod;
import com.wjybxx.fastjgame.net.rpc.RpcProcessContext;
import com.wjybxx.fastjgame.net.rpc.RpcService;
import com.wjybxx.fastjgame.util.concurrent.FutureUtils;
import com.wjybxx.fastjgame.util.concurrent.ListenableFuture;

/**
 * 示例rpcService
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/26
 * github - https://github.com/hl845740757
 */
@RpcService(serviceId = 32700)
public class ExampleRpcService {

    @RpcMethod(methodId = 0)
    public void sync() {

    }

    @RpcMethod(methodId = 1)
    public void hello(String name) {
        System.out.println(name);
    }

    @RpcMethod(methodId = 2)
    public int queryId(String name) {
        return name.hashCode();
    }

    @RpcMethod(methodId = 3)
    public int inc(final int number) {
        return number + 1;
    }

    /**
     * @return 增加后的值
     */
    @RpcMethod(methodId = 4)
    public int incWithContext(RpcProcessContext context, final int number) {
        return number + 2;
    }

    /**
     * @param number 待加的数
     */
    @RpcMethod(methodId = 5)
    public ListenableFuture<Integer> incAsync(final int number) {
        return FutureUtils.newSucceedFuture(number + 3);
    }

    @RpcMethod(methodId = 6)
    public ListenableFuture<Integer> incWithContextAsync(RpcProcessContext context, final int number) {
        return FutureUtils.newSucceedFuture(number + 4);
    }

    @RpcMethod(methodId = 7)
    public void notifySuccess(long id) {
        System.out.println(id);
    }

    @RpcMethod(methodId = 8)
    public String combine(String prefix, String content) {
        return prefix + "-" + content;
    }

    @RpcMethod(methodId = 9)
    public ExampleMessages.FullMessage echo(ExampleMessages.FullMessage message) {
        return message;
    }

    /**
     * 模拟场景服务器将消息通过网关发送给玩家
     *
     * @param playerGuid 玩家标识
     * @param proto      生成的代理方法类型为Object
     */
    @RpcMethod(methodId = 10)
    public void sendToPlayer(long playerGuid, @LazySerializable byte[] proto) throws Exception {
        System.out.println("playerGuid " + playerGuid + ", " + ExampleConstants.BINARY_SERIALIZER.fromBytes(proto));
    }

    /**
     * 模拟玩家将消息通过网关发送到场景服务器
     *
     * @param playerGuid 玩家标识
     * @param msg        玩家发来的消息
     */
    @RpcMethod(methodId = 11)
    public void sendToScene(long playerGuid, @PreDeserializable String msg) {
        System.out.println("playerGuid " + playerGuid + ", " + msg);
    }

    /**
     * 合并字符串，测试变长参数
     */
    @RpcMethod(methodId = 12)
    public String join(String... params) {
        return String.join(",", params);
    }

    /**
     * 抛出异常
     */
    @RpcMethod(methodId = 13)
    public String newException(String msg) {
        throw new RuntimeException(msg);
    }
}
