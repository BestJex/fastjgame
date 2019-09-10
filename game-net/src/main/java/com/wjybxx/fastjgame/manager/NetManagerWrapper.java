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
import com.wjybxx.fastjgame.eventloop.NetEventLoop;
import com.wjybxx.fastjgame.eventloop.NetEventLoopManager;

/**
 * 封装{@link NetEventLoop}可能使用到的所有管理器。
 * NetEventLoop不是依赖注入的，一个个获取实例实在有点麻烦...
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/3
 * github - https://github.com/hl845740757
 */
public class NetManagerWrapper {

    private final NetEventLoopManager netEventLoopManager;
    private final S2CSessionManager s2CSessionManager;
    private final C2SSessionManager c2SSessionManager;
    private final HttpSessionManager httpSessionManager;
    private final NetEventManager netEventManager;
    private final NettyThreadManager nettyThreadManager;
    private final NetConfigManager netConfigManager;
    private final AcceptorManager acceptorManager;
    private final HttpClientManager httpClientManager;
    private final NetTimeManager netTimeManager;
    private final NetTimerManager netTimerManager;
    private final TokenManager tokenManager;
    private final JVMC2SSessionManager jvmc2SSessionManager;
    private final JVMS2CSessionManager jvms2CSessionManager;

    @Inject
    public NetManagerWrapper(NetEventLoopManager netEventLoopManager,
                             S2CSessionManager s2CSessionManager, C2SSessionManager c2SSessionManager,
                             HttpSessionManager httpSessionManager, NetEventManager netEventManager,
                             NettyThreadManager nettyThreadManager,
                             NetConfigManager netConfigManager, AcceptorManager acceptorManager,
                             HttpClientManager httpClientManager, NetTimeManager netTimeManager,
                             NetTimerManager netTimerManager, TokenManager tokenManager, JVMC2SSessionManager jvmc2SSessionManager, JVMS2CSessionManager jvms2CSessionManager) {
        this.netEventLoopManager = netEventLoopManager;
        this.s2CSessionManager = s2CSessionManager;
        this.c2SSessionManager = c2SSessionManager;
        this.httpSessionManager = httpSessionManager;
        this.netEventManager = netEventManager;
        this.nettyThreadManager = nettyThreadManager;
        this.netConfigManager = netConfigManager;
        this.acceptorManager = acceptorManager;
        this.httpClientManager = httpClientManager;
        this.netTimeManager = netTimeManager;
        this.netTimerManager = netTimerManager;
        this.tokenManager = tokenManager;
        this.jvmc2SSessionManager = jvmc2SSessionManager;
        this.jvms2CSessionManager = jvms2CSessionManager;
    }

    public NetEventLoopManager getNetEventLoopManager() {
        return netEventLoopManager;
    }

    public S2CSessionManager getS2CSessionManager() {
        return s2CSessionManager;
    }

    public C2SSessionManager getC2SSessionManager() {
        return c2SSessionManager;
    }

    public HttpSessionManager getHttpSessionManager() {
        return httpSessionManager;
    }

    public NetEventManager getNetEventManager() {
        return netEventManager;
    }

    public NettyThreadManager getNettyThreadManager() {
        return nettyThreadManager;
    }

    public AcceptorManager getAcceptorManager() {
        return acceptorManager;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public NetTimeManager getNetTimeManager() {
        return netTimeManager;
    }

    public NetTimerManager getNetTimerManager() {
        return netTimerManager;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public NetConfigManager getNetConfigManager() {
        return netConfigManager;
    }

    public JVMC2SSessionManager getJvmc2SSessionManager() {
        return jvmc2SSessionManager;
    }

    public JVMS2CSessionManager getJvms2CSessionManager() {
        return jvms2CSessionManager;
    }
}
