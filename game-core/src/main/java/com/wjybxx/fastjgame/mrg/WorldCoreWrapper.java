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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;

/**
 * WorldCore依赖的所有控制器的包装类
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:41
 * @github - https://github.com/hl845740757
 */
public class WorldCoreWrapper {

    private final CuratorMrg curatorMrg;
    private final GuidMrg guidMrg;
    private final GameConfigMrg gameConfigMrg;
    private final InnerAcceptorMrg innerAcceptorMrg;
    private final MongoDBMrg mongoDBMrg;

    @Inject
    public WorldCoreWrapper(CuratorMrg curatorMrg, GuidMrg guidMrg, GameConfigMrg gameConfigMrg,
                            InnerAcceptorMrg innerAcceptorMrg, MongoDBMrg mongoDBMrg) {
        this.curatorMrg = curatorMrg;
        this.guidMrg = guidMrg;
        this.gameConfigMrg = gameConfigMrg;
        this.innerAcceptorMrg = innerAcceptorMrg;
        this.mongoDBMrg = mongoDBMrg;
    }

    public CuratorMrg getCuratorMrg() {
        return curatorMrg;
    }

    public GuidMrg getGuidMrg() {
        return guidMrg;
    }

    public GameConfigMrg getGameConfigMrg() {
        return gameConfigMrg;
    }

    public InnerAcceptorMrg getInnerAcceptorMrg() {
        return innerAcceptorMrg;
    }

    public MongoDBMrg getMongoDBMrg() {
        return mongoDBMrg;
    }
}
