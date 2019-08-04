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
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/17 21:28
 * github - https://github.com/hl845740757
 */
public class LoginNodeData {

    /**
     * 用于GM等工具而绑定的http端口信息
     */
    private final String innerHttpAddress;
    /**
     * 为玩家服务的http端口
     */
    private final String outerHttpAddress;

    @JsonCreator
    public LoginNodeData(@JsonProperty("innerHttpAddress") String innerHttpAddress,
                         @JsonProperty("outerHttpAddress") String outerHttpAddress) {
        this.innerHttpAddress = innerHttpAddress;
        this.outerHttpAddress = outerHttpAddress;
    }

    public String getInnerHttpAddress() {
        return innerHttpAddress;
    }

    public String getOuterHttpAddress() {
        return outerHttpAddress;
    }
}
