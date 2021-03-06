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

package com.wjybxx.fastjgame.util.concurrent;


import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * EventLoop选择器，用于负载均衡算法。实现类必须是线程安全的，因为{@link #next()} 可能被多线程同时调用。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/14
 * github - https://github.com/hl845740757
 */
@ThreadSafe
public interface EventLoopChooser {

    /**
     * 获取下一个执行用的EventLoop。
     *
     * @return EventLoop
     * @apiNote 子类实现需要是线程安全的。
     */
    @Nonnull
    EventLoop next();

    /**
     * 给定一个键，分配一个{@link EventLoop}。
     * 目的：这样用户总是可以通过key指定选中某一个线程，消除不必要的同步。
     *
     * @return EventLoop
     * @apiNote 子类实现需要是线程安全的。且同一个key的选择结果必须是相同的
     */
    EventLoop select(int key);
}
