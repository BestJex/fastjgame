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

package com.wjybxx.fastjgame.utils.concurrent;

import com.wjybxx.fastjgame.utils.annotation.UnstableApi;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * 非阻塞的可监听的future。
 * 它不提供任何的阻塞式接口，只提供异步监听和非阻塞获取结果的api。
 *
 * <h3>{@link ListenableFuture}优先</h3>
 * 如果不是必须需要阻塞式的API，应当优先选择{@link ListenableFuture}。
 * 由于jdk的future一开始就提供了阻塞式的api，因此这里不能继承jdk的future。
 *
 * <h3>监听器执行时序</h3>
 * 执行环境相同的监听器，执行顺序和添加顺序相同，也就是说：
 * 1. 对于未指定{@link Executor}的监听器，执行顺序和添加顺序一致，它们会在{@link #defaultExecutor()}中有序执行。
 * 2. 对于指定了相同{@link Executor}的监听器，如果{@link Executor}是单线程的，那么该{@link Executor}关联的监听会按照添加顺序执行。
 * 主要目的是为了降低开发难度，避免用户考虑太多的时序问题！
 *
 * <h3>实现要求</h3>
 * 1. 必须满足监听器的执行时序要求。
 * 2. 要么是线程安全的，可以多线程使用的；要么能检测到冲突并防止数据被破坏。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/3/6
 */
@ThreadSafe
public interface ListenableFuture<V> {

    /**
     * 查询任务是否已完成。
     * <h3>完成状态</h3>
     * <b>正常完成</b>、<b>被取消结束</b>、<b>执行异常而结束</b>都表示完成状态。
     *
     * @return 任务已进入完成状态则返回true。
     */
    boolean isDone();

    /**
     * 查询future关联的操作是否顺利完成了。
     *
     * @return 当且仅当该future对应的task顺利完成时返回true。
     */
    boolean isSuccess();

    // ---------------------------------------- 取消相关 --------------------------------------

    /**
     * 查询任务是否被取消。
     *
     * @return 当且仅当该future关联的task由于取消进入完成状态时返回true。
     */
    boolean isCancelled();

    /**
     * 查询future关联的任务是否可以被取消。
     *
     * @return true/false 当且仅当future关联的任务可以通过{@link #cancel(boolean)}被取消时返回true。
     */
    boolean isCancellable();

    /**
     * 尝试取消future关联的任务，如果取消成功，会使得Future进入完成状态，并且{@link #cause()}将返回{@link CancellationException}。
     * 1. 如果取消成功，则返回true。
     * 2. 如果任务已经被取消，则返回true。
     * 3. 如果future关联的任务已完成，则返回false。
     *
     * @param mayInterruptIfRunning 是否允许中断工作者线程
     * @return 是否取消成功
     */
    boolean cancel(boolean mayInterruptIfRunning);

    // ------------------------------------- 非阻塞式获取计算结果 --------------------------------------

    /**
     * 非阻塞的获取当前结果：
     * 1. 如果future关联的task还未完成{@link #isDone() false}，则返回null。
     * 2. 如果任务执行成功，则返回对应的结果。
     * 2. 如果任务被取消或失败，则抛出对应的异常。
     * <p>
     * 注意：
     * 如果future关联的task没有返回值(操作完成返回null)，此时不能根据返回值做任何判断。对于这种情况，
     * 你可以使用{@link #isSuccess()},作为更好的选择。
     *
     * @return task执行结果
     * @throws CancellationException 如果任务被取消了，则抛出该异常
     * @throws CompletionException   如果在计算过程中出现了其它异常导致任务失败，则抛出该异常。
     */
    V getNow();

    /**
     * 非阻塞获取导致任务失败的原因。
     * 1. 如果future关联的task还未进入完成状态{@link #isDone() false}，则返回null。
     * 2. 当future关联的任务被取消或由于异常进入完成状态后，该方法将返回操作失败的原因。
     * 3. 如果future关联的task已正常完成，则返回null。
     *
     * @return 失败的原因
     */
    Throwable cause();

    // ------------------------------------- 监听 --------------------------------------

    /**
     * 监听器的默认执行环境，如果在添加监听器时，未指定{@link Executor}，那么一定会在该{@link EventLoop}下执行。
     * 它声明为{@link EventLoop}，表示强调它是单线程的，也就是说所有未指定{@link Executor}的监听器会按照添加顺序执行。
     * <p>
     * 将其限制为单线程的，可能会导致一定的性能损失，但是可以降低编程难度。
     * 而且如果该{@link EventLoop}就是我们的业务逻辑线程，且只有业务逻辑线程添加回调的话，那么将没有性能损失，这种情况也很常见。
     */
    EventLoop defaultExecutor();

    /**
     * 添加一个监听器。Listener将会在Future计算完成时{@link #isDone() true}被通知。
     * 如果当前Future已经计算完成，那么将立即被通知（不一定立即执行）。
     *
     * @param listener 要添加的监听器。
     * @return this
     */
    ListenableFuture<V> onComplete(@Nonnull FutureListener<? super V> listener);

    /**
     * 添加一个监听器。Listener将会在Future计算完成时{@link #isDone() true}被通知，并最终运行在指定{@link Executor}下。
     *
     * @param listener     要添加的监听器
     * @param bindExecutor 监听器的最终执行线程
     * @return this
     * @see #onComplete(FutureListener)
     */
    ListenableFuture<V> onComplete(@Nonnull FutureListener<? super V> listener, @Nonnull Executor bindExecutor);

    /**
     * 添加一个监听器，该监听器只有在成功的时候执行
     *
     * @see #onComplete(FutureListener)
     */
    ListenableFuture<V> onSuccess(@Nonnull SucceededFutureListener<? super V> listener);

    ListenableFuture<V> onSuccess(@Nonnull SucceededFutureListener<? super V> listener, @Nonnull Executor bindExecutor);

    /**
     * 添加一个监听器，该监听器只有在失败的时候执行
     *
     * @see #onComplete(FutureListener)
     */
    ListenableFuture<V> onFailure(@Nonnull FailedFutureListener<? super V> listener);

    ListenableFuture<V> onFailure(@Nonnull FailedFutureListener<? super V> listener, @Nonnull Executor bindExecutor);

    // ------------------------------------- 用于支持占位的voidFuture --------------------------------------

    /**
     * 如果该方法返回true，表示该对象仅仅用于占位。
     * 任何<b>阻塞式调用</b>和<b>添加监听器</b>都将抛出异常。
     * <p>
     * Q: 它的主要目的？
     * A: 减少开销。其实任何使用VoidFuture的地方，都可以使用正常的future，
     * 只是会有额外的开销。在某些场景使用VoidFuture将节省很多开销。
     */
    @UnstableApi
    boolean isVoid();
}
