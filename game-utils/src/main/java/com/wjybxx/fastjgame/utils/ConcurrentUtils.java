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

package com.wjybxx.fastjgame.utils;

import com.wjybxx.fastjgame.function.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;

/**
 * 并发工具包，常用并发工具方法。
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/14 1:02
 * github - https://github.com/hl845740757
 */
public class ConcurrentUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentUtils.class);

    /** 什么都不做的任务 */
    public static final Runnable NO_OP_TASK = () -> {};

    /** 用于唤醒的任务 */
    public static final Runnable WEAK_UP_TASK = () -> {};

    private ConcurrentUtils() {

    }

    /**
     * 在{@link CountDownLatch#await()}上等待，等待期间不响应中断
     * @param countDownLatch 闭锁
     */
    public static void awaitUninterruptibly(@Nonnull CountDownLatch countDownLatch){
        awaitUninterruptibly(countDownLatch::await);
    }

    /**
     * 在{@link Semaphore#acquire()}上等待，等待期间不响应中断
     * @param semaphore 信号量
     */
    public static void awaitUninterruptibly(Semaphore semaphore){
        awaitUninterruptibly(semaphore::acquire);
    }

    /**
     * 在等待期间不响应中断
     * @param acquireFun 如果在资源上申请资源
     */
    public static void awaitUninterruptibly(AcquireFun acquireFun){
        // 清除中断状态，避免无谓的中断
        boolean interrupted = Thread.interrupted();
        try {
            while (true){
                try {
                    acquireFun.acquire();
                    break;
                }catch (InterruptedException e){
                    interrupted = true;
                }
            }
        }finally {
            // 恢复中断状态
            recoveryInterrupted(interrupted);
        }
    }

    /**
     * 使用重试的方式等待闭锁打开
     * @param countDownLatch 闭锁
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static void awaitWithRetry(CountDownLatch countDownLatch, long heartbeat, TimeUnit timeUnit){
        awaitWithRetry(countDownLatch::await, heartbeat, timeUnit);
    }

    /**
     * 使用重试的方式申请信号量
     * @param semaphore 信号量
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static void awaitWithRetry(Semaphore semaphore, long heartbeat, TimeUnit timeUnit){
        awaitWithRetry(semaphore::tryAcquire, heartbeat, timeUnit);
    }

     /**
     * 使用重试的方式申请资源(可以保持线程的活性)
     * @param tryAcquireFun 申请资源的方法
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static void awaitWithRetry(TryAcquireFun tryAcquireFun, long heartbeat, TimeUnit timeUnit){
        boolean interrupted = Thread.interrupted();
        try {
            while (true){
                try {
                    if (tryAcquireFun.tryAcquire(heartbeat,timeUnit)){
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted=true;
                }
            }
        }finally {
            recoveryInterrupted(interrupted);
        }
    }

    /**
     * 使用重试的方式申请资源，申请失败则睡眠一定时间。
     * @param tryAcquireFun 资源申请函数
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     * @param <T> 资源的类型
     */
    public static <T> void awaitWithSleepingRetry(TryAcquireFun2 tryAcquireFun, long heartbeat, TimeUnit timeUnit){
        awaitWithRetry(toAcquireFunWithSleep(tryAcquireFun), heartbeat, timeUnit);
    }

    // 远程资源申请
    /**
     * 在等待远程资源期间不响应中断
     * @param acquireFun 如果在资源上申请资源
     * @throws Exception error
     */
    public static <T> void awaitRemoteUninterruptibly(RemoteAcquireFun acquireFun) throws Exception {
        boolean interrupted = Thread.interrupted();
        try {
            while (true){
                try {
                    acquireFun.acquire();
                    break;
                }catch (InterruptedException e){
                    interrupted = true;
                }
            }
        }finally {
            recoveryInterrupted(interrupted);
        }
    }

    /**
     * 使用重试的方式申请远程资源
     * @param <T> 资源类型
     * @param tryAcquireFun 尝试申请
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     * @throws Exception error
     */
    public static <T> void awaitRemoteWithRetry(RemoteTryAcquireFun tryAcquireFun, long heartbeat, TimeUnit timeUnit) throws Exception {
        // 虽然是重复代码，但是不好消除
        boolean interrupted = Thread.interrupted();
        try {
            while (true){
                try {
                    if (tryAcquireFun.tryAcquire(heartbeat, timeUnit)){
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted=true;
                }
            }
        }finally {
            recoveryInterrupted(interrupted);
        }
    }

    /**
     * 使用重试的方式申请远程资源，申请失败则睡眠一定时间
     * @param tryAcquireFun 资源申请函数
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static <T> void awaitRemoteWithSleepingRetry(RemoteTryAcquireFun2 tryAcquireFun, long heartbeat, TimeUnit timeUnit) throws Exception {
        awaitRemoteWithRetry(toAcquireRemoteFunWithSleep(tryAcquireFun), heartbeat, timeUnit);
    }

    // region 私有实现(辅助方法)
    /**
     * 使用sleep转换为{@link TryAcquireFun}类型。
     * @param tryAcquireFun2 CAS的尝试函数
     * @param <T> 资源类型
     * @return
     */
    private static <T> TryAcquireFun toAcquireFunWithSleep(TryAcquireFun2 tryAcquireFun2){
        return (timeout, timeUnit) -> {
            if (tryAcquireFun2.tryAcquire()) {
                return true;
            } else {
                Thread.sleep(timeUnit.toMillis(timeout));
                return false;
            }
        };
    }

    /**
     * 使用sleep转换为{@link RemoteTryAcquireFun}类型。
     * @param tryAcquireFun2 CAS的尝试函数
     * @return
     */
    private static <T> RemoteTryAcquireFun toAcquireRemoteFunWithSleep(RemoteTryAcquireFun2 tryAcquireFun2){
        return (timeout, timeUnit) -> {
            if (tryAcquireFun2.tryAcquire()){
                return true;
            }else {
                Thread.sleep(timeUnit.toMillis(timeout));
                return false;
            }
        };
    }

    // endregion

    // ---------------------------------------- 中断处理 ---------------------------

    /**
     * 恢复中断
     * @param interrupted 是否出现了中断
     */
    public static void recoveryInterrupted(boolean interrupted) {
        if (interrupted) {
            try {
                Thread.currentThread().interrupt();
            } catch (SecurityException ignore) {
            }
        }
    }

    /**
     * 恢复中断。
     * 如果是中断异常，则恢复线程中断状态。
     * @param e 异常
     * @return ture if is InterruptedException
     */
    public static boolean recoveryInterrupted(Exception e) {
        if (isInterrupted(e)) {
            try {
                Thread.currentThread().interrupt();
            } catch (SecurityException ignore) {
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查线程中断状态。
     * @throws InterruptedException 如果线程被中断，则抛出中断异常
     */
    public static void checkInterrupted() throws InterruptedException{
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * 是否是中断异常
     * @param e exception
     * @return true/false
     */
    public static boolean isInterrupted(Throwable e) {
        return e instanceof InterruptedException;
    }

    /**
     * 将一个可能抛出异常的任务包装为一个不抛出受检异常的runnable。
     * @param r 可能抛出异常的任务
     * @param exceptionHandler 异常处理器
     * @return Runnable
     */
    public static Runnable safeRunnable(Runnable r, ExceptionHandler exceptionHandler){
        return new SafeRunnable(r, exceptionHandler);
    }

    /**
     * 将一个可能抛出异常的任务包装为一个不抛出受检异常的runnable。
     * @param r 可能抛出异常的任务
     * @param exceptionHandler 异常处理器
     * @return Runnable
     */
    public static Runnable safeRunnable(AnyRunnable r, ExceptionHandler exceptionHandler){
        return new SafeRunnable2(r, exceptionHandler);
    }

    /**
     * 安全的执行一个任务，只是将错误打印到日志，不抛出异常。
     * 对于不甚频繁的方法调用可以进行封装，如果大量的调用可能会对性能有所影响；
     *
     * @param task 要执行的任务，可以将要执行的方法封装为 ()-> safeExecute()
     * @return true if is interrupted
     */
    public static boolean safeExecute(Runnable task){
        boolean interrupted = false;
        try {
            task.run();
        } catch (Throwable e) {
            interrupted = isInterrupted(e);
            logger.warn("A task raised an exception. Task: {}", task, e);
        }
        return interrupted;
    }

    /**
     * 安全的执行一个任务，只是将错误打印到日志，不抛出异常。
     * 对于不甚频繁的方法调用可以进行封装，如果大量的调用可能会对性能有所影响；
     *
     * @param task 要执行的任务，可以将要执行的方法封装为 ()-> safeExecute()
     * @return true if is interrupted
     */
    public static boolean safeExecute(AnyRunnable task) {
        boolean interrupted = false;
        try {
            task.run();
        } catch (Throwable e) {
            interrupted = isInterrupted(e);
            logger.warn("A task raised an exception. Task: {}", task, e);
        }
        return interrupted;
    }

    /**
     * 尝试提交任务到指定executor
     * @param executor 任务提交的目的地
     * @param task 待提交的任务
     * @return 如果提交成功则返回true，否则返回false。
     */
    public static boolean tryCommit(@Nonnull Executor executor, @Nonnull Runnable task){
        try {
            executor.execute(task);
            return true;
        } catch (Exception e){
            // may reject
            if (e instanceof RejectedExecutionException) {
                logger.info("Target executor may shutdown, task is rejected.");
            } else {
                logger.warn("execute caught exception!", e);
            }
        }
        return false;
    }

    /**
     * 重新抛出失败异常。如果cause为null，则什么也不做，否则抛出对应的异常
     *
     * @param cause 任务失败的原因
     * @throws CancellationException 被取消
     * @throws ExecutionException 执行中出现其它异常
     */
    public static void rethrowIfFailed(@Nullable Throwable cause) throws CancellationException, ExecutionException{
        if (cause == null) {
            return;
        }
        if (cause instanceof CancellationException) {
            throw (CancellationException) cause;
        }
        throw new ExecutionException(cause);
    }

    /**
     * 重新抛出异常，绕过编译时检查。
     *
     * @param ex 受检异常
     * @param <E> 异常类型
     * @throws E 绕过编译时检查
     */
    @SuppressWarnings("unchecked")
    public static <E extends Exception> void rethrow(@Nonnull Exception ex) throws E{
        throw (E)ex;
    }

    //  ------------------------------------ 内部的一些封装，指不定什么时候可能就换成lambda表达式 --------------------

    private static class AnyRunnableAdapter implements AnyRunnable{

        private final Runnable task;

        public AnyRunnableAdapter(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() throws Exception {
            task.run();;
        }
    }

    private static class SafeRunnable implements Runnable {

        private final Runnable task;
        private final ExceptionHandler exceptionHandler;

        private SafeRunnable(Runnable task, ExceptionHandler exceptionHandler) {
            this.task = task;
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e){
                exceptionHandler.handleException(e);
            }
        }
    }

    private static class SafeRunnable2 implements Runnable {

        private final AnyRunnable task;
        private final ExceptionHandler exceptionHandler;

        private SafeRunnable2(AnyRunnable task, ExceptionHandler exceptionHandler) {
            this.task = task;
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e){
                exceptionHandler.handleException(e);
            }
        }
    }
}