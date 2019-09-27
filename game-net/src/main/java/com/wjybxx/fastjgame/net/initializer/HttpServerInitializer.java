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

package com.wjybxx.fastjgame.net.initializer;

import com.wjybxx.fastjgame.manager.NetEventManager;
import com.wjybxx.fastjgame.net.http.HttpRequestDispatcher;
import com.wjybxx.fastjgame.net.http.HttpRequestParamDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 作为http服务器时的channel初始化器
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/28 22:13
 * github - https://github.com/hl845740757
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 是哪一个用户在监听
     */
    private final long localGuid;
    private final HttpRequestDispatcher httpRequestDispatcher;
    private final NetEventManager netEventManager;

    public HttpServerInitializer(long localGuid, HttpRequestDispatcher httpRequestDispatcher, NetEventManager netEventManager) {
        this.localGuid = localGuid;
        this.httpRequestDispatcher = httpRequestDispatcher;
        this.netEventManager = netEventManager;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        // http请求和响应可能被分段，利用聚合器将http请求合并为完整的Http请求
        pipeline.addLast(new HttpObjectAggregator(65535));
        pipeline.addLast(new HttpRequestParamDecoder(localGuid, httpRequestDispatcher, netEventManager));
    }
}
