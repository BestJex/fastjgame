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
import javax.annotation.Nullable;

/**
 * 事件循环。
 *
 * <h3>多生产者单消费者模型</h3>
 * 1. 它是单线程的: 它保证任务不会并发执行，且任务的执行顺序和提交顺序一致。
 * 2. 它会阻止其它线程消费数据。
 * 3. 由于{@link EventLoop}都是单线程的，如果两个{@link EventLoop}存在直接交互，
 * 且某一个{@link EventLoop}使用的是有界队列，则可能导致大量的任务拒绝或死锁！
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/14
 * github - https://github.com/hl845740757
 */
public interface EventLoop extends FixedEventLoopGroup {

    /**
     * @return this - 由于{@link EventLoop}表示单个线程，因此总是分配自己。
     */
    @Nonnull
    @Override
    EventLoop next();

    /**
     * @param key 计算索引的键
     * @return this - 由于{@link EventLoop}表示单个线程，因此总是选中自己
     */
    @Nonnull
    @Override
    EventLoop select(int key);

    /**
     * 返回该EventLoop线程所在的线程组（管理该EventLoop的容器）。
     * 如果没有父节点，返回null。
     */
    @Nullable
    EventLoopGroup parent();

    /**
     * 当前线程是否是{@link EventLoop}所在线程。
     * 主要作用:
     * 1. 它暗示着：如果当前线程是{@link EventLoop}线程，那么可以访问一些线程封闭的数据。
     * 2. 防止死锁。
     *
     * <h3>时序问题</h3>
     * 以下代码可能产生时序问题:
     * <pre>
     * {@code
     * 		if(eventExecutor.inEventLoop()) {
     * 	    	doSomething();
     *        } else{
     * 			eventLoop.execute(() -> doSomething());
     *        }
     * }
     * </pre>
     * Q: 产生的原因？
     * A: 单看任意一个线程，该线程的所有操作之间都是有序的，这个应该能理解。
     * 但是出现多个线程执行该代码块时：
     * 1. 所有的非EventLoop线程的操作会进入同一个队列，因此所有的非EventLoop线程之间的操作是有序的！
     * 2. 但是EventLoop线程是直接执行的，并没有进入队列，因此EventLoop线程 和 任意非EventLoop线程之间都没有顺序保证。
     * <p>
     * 举个例子：去营业厅办理业务时，一般是先拿号，再排队办理业务。但是如果有人拿号之后可以立即办理业务，不需要排队，那么号码可能就失去意义了！
     * <p>
     * 该方法一定要慎用，它有时候是无害的，有时候则是有害的，因此必须想明白是否需要提供全局时序保证！
     *
     * @return true/false
     */
    boolean inEventLoop();

}
