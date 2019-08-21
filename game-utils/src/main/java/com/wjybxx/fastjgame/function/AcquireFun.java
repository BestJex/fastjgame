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
package com.wjybxx.fastjgame.function;

/**
 * 资源申请函数
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/14 10:58
 * github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface AcquireFun {

    /**
     * 申请资源，直到成功或中断
     * @throws InterruptedException 如果在申请资源期间被中断，则可能抛出该异常
     */
    void acquire() throws InterruptedException;

}
