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

package com.wjybxx.fastjgame.manager;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.concurrent.DefaultThreadFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Netty线程管理器。
 * 最终决定还是每一个NetEventLoop一个，因为资源分配各有缺陷。
 * why?
 * 如果以NetEventLoopGroup进行分配，在用户不清楚的情况下，用户可能认为调整NetEventLoopGroup的线程数就可以提高网络性能，但实际上不行。
 * 如果没有调整Netty的线程数，它不能做到随着NetEventLoop线程的增加使性能也增加的目的。
 * <p>
 * 以NetEventLoop为单位分配资源也有坏处，最明显的坏处就是线程数很多。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/29 20:02
 * github - https://github.com/hl845740757
 */
@ThreadSafe
public class NettyThreadManager {

    private static final Logger logger = LoggerFactory.getLogger(NettyThreadManager.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Inject
    public NettyThreadManager() {

    }

    public void start(int nettyIOThreadNum) {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("ACCEPTOR_THREAD"));
        workerGroup = new NioEventLoopGroup(nettyIOThreadNum, new DefaultThreadFactory("IO_THREAD"));
        logger.info("NettyThreadManager start success");
    }

    /**
     * 关闭Netty的线程
     */
    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("NettyThreadManager shutdown success");

    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

}
