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

package com.wjybxx.fastjgame.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * 协议编解码器。
 * {@link ProtocolCodec}在网络层，而{@link ProtocolDispatcher}在应用层，在用户线程。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/18
 * github - https://github.com/hl845740757
 * @apiNote 子类实现必须是线程安全的！
 */
@ThreadSafe
public interface ProtocolCodec {

    // ---------------------------------------- RPC请求 ---------------------------------

    /**
     * 编码rpc请求
     *
     * @param bufAllocator buf分配器
     * @param request      rpc请求内容
     * @return rpc请求对应的字节数组，为甚使用{@link ByteBuf}？ 减少中间数组对象，减少垃圾回收。
     */
    ByteBuf encodeRpcRequest(ByteBufAllocator bufAllocator, @Nonnull Object request) throws IOException;

    /**
     * 解码rpc请求
     *
     * @param data byteBuf数据
     * @return rpc请求内容
     */
    Object decodeRpcRequest(ByteBuf data) throws IOException;

    /**
     * 将一个对象序列化再反序列化，获取一个拷贝的新对象。
     *
     * @param request rpc请求内容
     * @return newInstance
     */
    Object cloneRpcRequest(@Nonnull Object request) throws IOException;

    // ----------------------------------------- RPC响应 -------------------------------------

    /**
     * 编码rpc响应结果
     * 当且仅当{@link RpcResponse#getBody() != null}时才会调用。
     *
     * @param bufAllocator buf分配器
     * @param body         rpc响应内容
     * @return rpc响应对应的字节数组
     */
    ByteBuf encodeRpcResponse(ByteBufAllocator bufAllocator, @Nonnull Object body) throws IOException;

    /**
     * 解码rpc响应内容。
     * 当且仅当{@link ByteBuf#readableBytes() > 0}时才会调用。
     *
     * @param data byteBuf数据
     * @return rpc响应内容
     */
    Object decodeRpcResponse(ByteBuf data) throws IOException;

    /**
     * 将一个rpc响应结果对象序列化再反序列化，获取一个拷贝的新对象。
     *
     * @param body 响应内容。
     * @return newInstance
     */
    Object cloneRpcResponse(@Nonnull Object body) throws IOException;
    // ----------------------------------------- 单向消息 -------------------------------------

    /**
     * 编码一个单向消息
     *
     * @param bufAllocator buf分配器
     * @param message      消息内容
     * @return 消息对应的字节数组
     */
    ByteBuf encodeMessage(ByteBufAllocator bufAllocator, Object message) throws IOException;

    /**
     * 解码一个单向消息
     *
     * @param data byteBuf数据
     * @return 消息内容
     */
    Object decodeMessage(ByteBuf data) throws IOException;

    /**
     * 将一个单向消息序列化再反序列化，获取一个拷贝的新对象。
     *
     * @param message 消息内容
     * @return newInstance
     */
    Object cloneMessage(@Nonnull Object message) throws IOException;
}
