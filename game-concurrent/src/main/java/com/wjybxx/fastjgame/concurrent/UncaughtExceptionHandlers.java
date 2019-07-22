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

package com.wjybxx.fastjgame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * 常用的线程未处理异常处理器
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/22
 * github - https://github.com/hl845740757
 */
public class UncaughtExceptionHandlers {

    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionHandlers.class);

    private UncaughtExceptionHandlers() {

    }

    /**
     * 如果线程缺少 {@link java.lang.Thread.UncaughtExceptionHandler},则将未捕获的异常计入日志。
     * @param thread 待检测的线程
     */
    public static void logIfAbsent(Thread thread) {
        logIfAbsent(thread, logger);
    }

    /**
     * 如果线程缺少 {@link java.lang.Thread.UncaughtExceptionHandler},则将未捕获的异常计入日志。
     *
     * @param thread 待检测的线程
     * @param logger 指定的用于记录日志的logger
     */
    public static void logIfAbsent(Thread thread, Logger logger) {
        if (thread.getUncaughtExceptionHandler() == null){
            thread.setUncaughtExceptionHandler(justLog(logger));
        }
    }

    /**
     * 仅仅记录日志的UncaughtExceptionHandler
     * @return UncaughtExceptionHandler
     */
    @Nonnull
    public static Thread.UncaughtExceptionHandler justLog() {
        return justLog(logger);
    }

    /**
     * 仅仅记录日志的UncaughtExceptionHandler (使用指定的logger)
     * @param logger 指定的用于记录日志的logger
     * @return UncaughtExceptionHandler
     */
    @Nonnull
    public static Thread.UncaughtExceptionHandler justLog(Logger logger) {
        return (Thread t, Throwable e) -> {
            logger.error("thread {} exit due to uncaughtException.", t.toString(), e);
        };
    }
}
