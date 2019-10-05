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

package com.wjybxx.fastjgame.net.ws;

import com.wjybxx.fastjgame.manager.NetEventManager;
import com.wjybxx.fastjgame.net.socket.ServerSocketCodec;
import com.wjybxx.fastjgame.net.socket.SocketPortExtraInfo;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * 使用websocket时使用
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/27 22:25
 * github - https://github.com/hl845740757
 */
public class WsServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * url路径(eg: "http://127.0.0.1:8888/ws" 中的 /ws )
     */
    private final String websocketPath;
    private final SocketPortExtraInfo portExtraInfo;
    private final NetEventManager netEventManager;

    public WsServerChannelInitializer(String websocketPath, SocketPortExtraInfo portExtraInfo, NetEventManager netEventManager) {
        this.websocketPath = websocketPath;
        this.portExtraInfo = portExtraInfo;
        this.netEventManager = netEventManager;
    }

    /**
     * 解码流程(自上而下)
     * 编码流程(自下而上)
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        appendHttpCodec(pipeline);

        appendWebsocketCodec(pipeline);

        appendCustomProtocolCodec(pipeline);
    }

    private void appendHttpCodec(ChannelPipeline pipeline) {
        // http支持 webSocket是建立在http上的
        pipeline.addLast(new HttpServerCodec());
        // http请求和响应可能被分段，利用聚合器将http请求合并为完整的Http请求
        pipeline.addLast(new HttpObjectAggregator(65535));
    }

    private void appendWebsocketCodec(ChannelPipeline pipeline) {
        // websocket 解码流程
        // websocket协议处理器(握手、心跳等)
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath));
        pipeline.addLast(new BinaryWebSocketFrameToBytesDecoder());

        // websocket 编码流程
        // Web socket clients must set this to true to mask payload.
        // Server implementations must set this to false.
        pipeline.addLast(new WebSocket13FrameEncoder(false));
        // 将ByteBuf转换为websocket二进制帧
        pipeline.addLast(new BytesToBinaryWebSocketFrameEncoder());
    }

    private void appendCustomProtocolCodec(ChannelPipeline pipeline) {
        pipeline.addLast(NetUtils.READ_TIMEOUT_HANDLER_NAME, new ReadTimeoutHandler(45));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(portExtraInfo.getSessionConfig().maxFrameLength(), 0, 4, 0, 4));
        pipeline.addLast(new ServerSocketCodec(portExtraInfo.getSessionConfig().codec(), portExtraInfo, netEventManager));
    }
}
