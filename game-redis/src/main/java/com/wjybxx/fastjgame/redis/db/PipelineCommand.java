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

package com.wjybxx.fastjgame.redis.db;

import com.wjybxx.fastjgame.util.misc.MethodSpec;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * 基于管道的命令，它支持大部分命令。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/12/12
 * github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface PipelineCommand<V> extends MethodSpec<V> {

    Response<V> execute(Pipeline pipeline);

}
