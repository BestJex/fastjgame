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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.wjybxx.fastjgame.manager.*;

/**
 * NetEventLoop依赖的模块，EventLoop级别的单例。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/2
 * github - https://github.com/hl845740757
 */
public class NetEventLoopModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();

        bind(NetEventLoopManager.class).in(Singleton.class);

        bind(AcceptorManager.class).in(Singleton.class);
        bind(ConnectorManager.class).in(Singleton.class);
        bind(HttpSessionManager.class).in(Singleton.class);

        bind(NetTimeManager.class).in(Singleton.class);
        bind(NetTimerManager.class).in(Singleton.class);

        bind(NetManagerWrapper.class).in(Singleton.class);
    }
}
