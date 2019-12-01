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

package com.wjybxx.fastjgame.core.onlinenode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * zookeeper上在线CenterServer节点信息
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/15 17:21
 * github - https://github.com/hl845740757
 */
public class CenterNodeData extends TcpServerNodeData {

    /**
     * world唯一标识。
     * 因为center节点需要互斥存在，因此guid不在名字里，而是在这里。
     */
    private final long worldGuid;

    @JsonCreator
    public CenterNodeData(@JsonProperty("innerHttpAddres") String innerHttpAddress,
                          @JsonProperty("innerTcpAddress") String innerTcpAddress,
                          @JsonProperty("worldGuid") long worldGuid) {
        super(innerHttpAddress, innerTcpAddress);
        this.worldGuid = worldGuid;
    }

    public long getWorldGuid() {
        return worldGuid;
    }
}
