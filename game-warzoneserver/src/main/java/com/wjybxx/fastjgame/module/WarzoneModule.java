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
import com.wjybxx.fastjgame.world.WarzoneWorld;
import com.wjybxx.fastjgame.world.World;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/17 15:38
 * github - https://github.com/hl845740757
 */
public class WarzoneModule extends WorldModule {

    @Override
    protected void bindWorldAndWorldInfoMgr() {
        bind(World.class).to(WarzoneWorld.class).in(Singleton.class);
        bind(WorldInfoMgr.class).to(WarzoneWorldInfoMgr.class).in(Singleton.class);
        bind(ProtocolDispatcherMgr.class).in(Singleton.class);
    }

    @Override
    protected void bindOthers() {
        // 方便子类直接使用
        bind(WarzoneWorldInfoMgr.class).in(Singleton.class);
        bind(CenterInWarzoneInfoMgr.class).in(Singleton.class);
        bind(WarzoneMongoDBMgr.class).in(Singleton.class);
    }
}
