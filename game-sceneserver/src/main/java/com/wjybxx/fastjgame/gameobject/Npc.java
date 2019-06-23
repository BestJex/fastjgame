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

package com.wjybxx.fastjgame.gameobject;

import com.wjybxx.fastjgame.config.NpcConfig;
import com.wjybxx.fastjgame.scene.gameobjectdata.GameObjectType;

import javax.annotation.Nonnull;

/**
 * npc
 * @author wjybxx
 * @version 1.0
 * date - 2019/6/4 17:00
 * github - https://github.com/hl845740757
 */
public class Npc extends GameObject<NpcData>{

    private final NpcData npcData;

    public Npc(NpcData npcData) {
        this.npcData = npcData;
    }

    @Nonnull
    @Override
    public NpcData getData() {
        return npcData;
    }

    public NpcConfig getNpcConfig() {
        return npcData.getConfig();
    }

    public int getNpcId(){
        return npcData.getNpcId();
    }

}
