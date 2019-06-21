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

package com.wjybxx.fastjgame.net.async.event;

import com.wjybxx.fastjgame.net.async.transferobject.ConnectResponseTO;

import javax.annotation.concurrent.Immutable;

/**
 * 连接响应事件参数
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/7 13:08
 * github - https://github.com/hl845740757
 */
@Immutable
public class ConnectResponseEventParam implements NetEventParam{
    /**
     * 服务端guid
     */
    private final long serverGuid;
    /**
     * 响应参数
     */
    private final ConnectResponseTO responseTO;

    public ConnectResponseEventParam(long serverGuid, ConnectResponseTO responseTO) {
        this.serverGuid = serverGuid;
        this.responseTO = responseTO;
    }

    public ConnectResponseTO getResponseTO() {
        return responseTO;
    }

    public int getSndTokenTimes() {
        return responseTO.getSndTokenTimes();
    }

    public boolean isSuccess() {
        return responseTO.isSuccess();
    }

    public long getServerGuid() {
        return serverGuid;
    }

    public long getAck() {
        return responseTO.getAck();
    }

    public byte[] getEncrytedToken() {
        return responseTO.getEncrytedToken();
    }

    @Override
    public long sessionGuid() {
        return serverGuid;
    }
}
