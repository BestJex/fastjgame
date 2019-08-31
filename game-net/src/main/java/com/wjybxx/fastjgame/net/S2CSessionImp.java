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


import com.wjybxx.fastjgame.concurrent.ListenableFuture;
import com.wjybxx.fastjgame.manager.NetConfigManager;
import com.wjybxx.fastjgame.manager.NetManagerWrapper;
import com.wjybxx.fastjgame.manager.SessionManager;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.utils.EventLoopUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 连接的接收方建立的会话信息。
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/27 10:18
 * github - https://github.com/hl845740757
 */
public class S2CSessionImp extends AbstractSession implements S2CSession {

    private final NetContext netContext;
    /**
     *获取通用控制器
     */
    private final NetManagerWrapper netManagerWrapper;
    /**
     * 本地端口
     */
    private final HostAndPort localAddress;
    /**
     * 客户端唯一id，也就是sessionId
     */
    private final long clientGuid;
    /**
     * 客户端类型
     */
    private final RoleType clientType;
    /**
     * 会话在激活的时候才会创建，因此初始的时候是true
     */
    private final AtomicBoolean stateHolder = new AtomicBoolean(true);

    public S2CSessionImp(NetContext netContext, HostAndPort localAddress, NetManagerWrapper netManagerWrapper,
                         long clientGuid, RoleType clientType, SenderMode senderMode) {
        super(senderMode);
        this.netContext = netContext;
        this.netManagerWrapper = netManagerWrapper;
        this.localAddress = localAddress;
        this.clientGuid = clientGuid;
        this.clientType = clientType;
    }

    @Override
    public NetContext netContext() {
        return netContext;
    }

    @Override
    protected NetConfigManager getNetConfigManager() {
        return netManagerWrapper.getNetConfigManager();
    }

    @Override
    protected SessionManager getSessionManager() {
        return netManagerWrapper.getS2CSessionManager();
    }

    @Override
    public HostAndPort localAddress() {
        return localAddress;
    }

    @Override
    public long remoteGuid() {
        return clientGuid;
    }

    @Override
    public RoleType remoteRole() {
        return clientType;
    }

    @Override
    public boolean isActive() {
        return stateHolder.get();
    }

    public void setClosed() {
        stateHolder.set(false);
    }

    @Override
    protected ListenableFuture<?> close0() {
        // 为什么不对它做优化了？ 因为他本身调用的频率就很低，平白无故的增加复杂度不值得。
        // 设置状态
        setClosed();
        // 可能是自身发起关闭请求，因此可能在当前线程，重复调用也必须发送NetEventLoop，否则可能看似关闭，实则还未关闭
        return EventLoopUtils.submitOrRun(netContext.netEventLoop(), () -> {
            getSessionManager().removeSession(localGuid(), remoteGuid(), "close");
        });
    }

    @Override
    public String toString() {
        return "S2CSessionImp{" +
                "localGuid=" + localGuid() +
                ", localRole=" + localRole() +
                ", localAddress=" + localAddress +
                ", clientGuid=" + clientGuid +
                ", clientType=" + clientType +
                ", active=" + stateHolder.get() +
                '}';
    }
}
