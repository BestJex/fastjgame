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

package com.wjybxx.fastjgame.net.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * 用于标识会话对方的角色类型
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:01
 * @github - https://github.com/hl845740757
 */
public enum RoleType {
    /**
     * 无效的
     */
    INVALID(-1),
    /**
     * 网关服
     */
    GATE_SERVER(1),
    /**
     * 登录服(login server)
     */
    LOGIN_SERVER(2),
    /**
     * 游戏服
     */
    GAME_SERVER(3),
    /**
     * 场景服
     */
    SCENE_SERVER(4),
    /**
     * 战区服
     */
    WARZONE_SERVER(5),
    /**
     * 玩家
     */
    PLAYER(6),
    /**
     * GM
     */
    GM(7),
    ;

    /**
     * 角色编号
     */
    public final int number;

    RoleType(int roleType) {
        this.number = roleType;
    }

    private static final Int2ObjectMap<RoleType> mapper;

    static {
        Int2ObjectOpenHashMap<RoleType> modifiableMap = new Int2ObjectOpenHashMap<>();
        for (RoleType roleType:RoleType.values()){
            modifiableMap.put(roleType.number,roleType);
        }
        mapper= Int2ObjectMaps.unmodifiable(modifiableMap);
    }

    public static RoleType forNumber(int number){
        return mapper.get(number);
    }
}
