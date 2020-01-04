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

package com.wjybxx.fastjgame.module;

import com.google.inject.Singleton;
import com.wjybxx.fastjgame.mgr.*;
import com.wjybxx.fastjgame.world.CenterWorld;
import com.wjybxx.fastjgame.world.World;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/15 23:07
 * github - https://github.com/hl845740757
 */
public class CenterModule extends WorldModule {

    @Override
    protected void bindWorldAndWorldInfoMgr() {
        bind(World.class).to(CenterWorld.class).in(Singleton.class);
        bind(WorldInfoMgr.class).to(CenterWorldInfoMgr.class).in(Singleton.class);
        bind(ProtocolDispatcherMgr.class).in(Singleton.class);
    }

    @Override
    protected void bindOthers() {
        bind(CenterWorldInfoMgr.class).in(Singleton.class);
        // 数据库支持
        bind(CenterMongoDBMgr.class).in(Singleton.class);

        // 节点发现
        bind(CenterDiscoverMgr.class).in(Singleton.class);
        // 服务器会话管理
        bind(CenterSceneSessionMgr.class).in(Singleton.class);
        bind(CenterWarzoneSessionMgr.class).in(Singleton.class);
        bind(CenterGateSessionMgr.class).in(Singleton.class);
        bind(CenterRouterMgr.class).in(Singleton.class);

        bind(CenterPlayerMessageDispatcherMgr.class).in(Singleton.class);
    }
}
