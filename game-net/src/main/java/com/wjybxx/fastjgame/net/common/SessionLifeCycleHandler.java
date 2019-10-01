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

package com.wjybxx.fastjgame.net.common;

import com.wjybxx.fastjgame.manager.NetTimeManager;
import com.wjybxx.fastjgame.misc.ConnectAwareTask;
import com.wjybxx.fastjgame.misc.DisconnectAwareTask;
import com.wjybxx.fastjgame.net.session.SessionHandlerContext;
import com.wjybxx.fastjgame.net.session.SessionInboundHandlerAdapter;

/**
 * session生命周期处理器
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/28
 * github - https://github.com/hl845740757
 */
public class SessionLifeCycleHandler extends SessionInboundHandlerAdapter {

    private long lastReadTime;
    private NetTimeManager timeManager;
    private long sessionTimeoutMs;

    @Override
    public void init(SessionHandlerContext ctx) throws Exception {
        // 缓存减少堆栈深度
        timeManager = ctx.managerWrapper().getNetTimeManager();
        sessionTimeoutMs = ctx.session().config().getSessionTimeoutMs();
        lastReadTime = timeManager.getSystemMillTime();
    }

    @Override
    public void tick(SessionHandlerContext ctx) {
        // 超时时间内，没读取到消息了
        if (timeManager.getSystemMillTime() - lastReadTime > sessionTimeoutMs) {
            // tick的时候不能直接删除session
            ctx.managerWrapper().getNetTimerManager().nextTick(handle -> {
                ctx.session().close();
            });
        }
    }

    @Override
    public void onSessionActive(SessionHandlerContext ctx) throws Exception {
        ctx.localEventLoop().execute(new ConnectAwareTask(ctx.session()));
    }

    @Override
    public void onSessionInactive(SessionHandlerContext ctx) throws Exception {
        ctx.localEventLoop().execute(new DisconnectAwareTask(ctx.session()));
    }

    @Override
    public void read(SessionHandlerContext ctx, Object msg) {
        lastReadTime = timeManager.getSystemMillTime();
        if (msg == PingPongMessage.INSTANCE) {
            // 读取到一个ping，立即返回一个消息
            ctx.session().fireWrite(PingPongMessage.INSTANCE);
        } else {
            ctx.fireRead(msg);
        }
    }
}
