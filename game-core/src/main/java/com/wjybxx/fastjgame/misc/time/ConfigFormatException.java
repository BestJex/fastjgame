/*
 *  Copyright 2019 wjybxx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to iBn writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wjybxx.fastjgame.misc.time;

/**
 * 配置格式异常
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/7 22:50
 * github - https://github.com/hl845740757
 */
public class ConfigFormatException extends RuntimeException {

    public ConfigFormatException() {
    }

    public ConfigFormatException(String message) {
        super(message);
    }

    public ConfigFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigFormatException(Throwable cause) {
        super(cause);
    }

    public ConfigFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
