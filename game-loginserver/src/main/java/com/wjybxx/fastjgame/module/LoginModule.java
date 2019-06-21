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
import com.wjybxx.fastjgame.mrg.CenterInLoginInfoMrg;
import com.wjybxx.fastjgame.mrg.LoginDiscoverMrg;
import com.wjybxx.fastjgame.mrg.LoginWorldInfoMrg;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;
import com.wjybxx.fastjgame.world.LoginWorld;
import com.wjybxx.fastjgame.world.World;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/17 20:09
 * github - https://github.com/hl845740757
 */
public class LoginModule extends CoreModule{

    @Override
    protected void bindWorldAndWorldInfoMrg() {
        bind(World.class).to(LoginWorld.class).in(Singleton.class);
        bind(WorldInfoMrg.class).to(LoginWorldInfoMrg.class).in(Singleton.class);
    }

    @Override
    protected void bindOthers() {
        bind(LoginWorldInfoMrg.class).in(Singleton.class);
        bind(LoginDiscoverMrg.class).in(Singleton.class);
        bind(CenterInLoginInfoMrg.class).in(Singleton.class);
    }
}
