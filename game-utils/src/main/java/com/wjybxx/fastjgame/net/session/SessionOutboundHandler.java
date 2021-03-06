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

package com.wjybxx.fastjgame.net.session;

/**
 * {@link SessionPipeline} 出站事件处理器。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/25
 * github - https://github.com/hl845740757
 */
public interface SessionOutboundHandler extends SessionHandler {

    /**
     * 发送一个消息。
     * 为了扩展性变成了这样，导致丧失了部分可读性和性能。
     *
     * @param msg 消息内容
     */
    void write(SessionHandlerContext ctx, Object msg) throws Exception;

    /**
     * 刷新缓冲区，如果有缓存的话。
     *
     * @param ctx handler所属的上下文
     */
    void flush(SessionHandlerContext ctx) throws Exception;

    /**
     * 请求关闭session。
     * 注意：不要轻易重写该方法。
     *
     * @param ctx handler所属的上下文
     */
    void close(SessionHandlerContext ctx) throws Exception;

}
