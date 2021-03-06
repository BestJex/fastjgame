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

package com.wjybxx.fastjgame.net.http;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2019/10/4
 * github - https://github.com/hl845740757
 */
public class HttpRequestCommitTask implements Runnable {

    private final HttpSession httpSession;
    private final String path;
    private final HttpRequestParam params;
    private final HttpRequestDispatcher dispatcher;

    public HttpRequestCommitTask(HttpSession httpSession, String path, HttpRequestParam params, HttpRequestDispatcher dispatcher) {
        this.httpSession = httpSession;
        this.dispatcher = dispatcher;
        this.path = path;
        this.params = params;
    }

    @Override
    public void run() {
        dispatcher.post(httpSession, path, params);
    }
}
