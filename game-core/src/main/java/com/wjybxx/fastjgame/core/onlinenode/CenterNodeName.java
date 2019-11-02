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

import com.wjybxx.fastjgame.misc.CenterServerId;

/**
 * 中心服需要总是生成相同的名字，以使得只有一个center进程存在
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/16 0:14
 * github - https://github.com/hl845740757
 */
public class CenterNodeName {

    private final CenterServerId serverId;

    public CenterNodeName(CenterServerId serverId) {
        this.serverId = serverId;
    }

    public CenterServerId getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
        return "CenterNodeName{" +
                "serverId=" + serverId +
                '}';
    }
}
