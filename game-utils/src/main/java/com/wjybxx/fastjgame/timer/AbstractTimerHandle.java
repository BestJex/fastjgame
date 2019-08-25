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

package com.wjybxx.fastjgame.timer;

import com.wjybxx.fastjgame.misc.LongSequencer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * 抽象的TimerHandle实现
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/14
 * github - https://github.com/hl845740757
 */
public abstract class AbstractTimerHandle implements TimerHandle{

	private static final LongSequencer timerIdSequencer = new LongSequencer(0);

	/**
	 * 执行时间越小越靠前，执行时间相同的，timerId越小越靠前(越先添加timerId越小)
	 */
	public static final Comparator<AbstractTimerHandle> timerComparator = Comparator.comparingLong(AbstractTimerHandle::getNextExecuteTimeMs)
			.thenComparingLong(AbstractTimerHandle::getTimerId);

	/** 定时器id，先添加的必定更小... */
	private final long timerId;
	/** 绑定的timer系统 */
	private final TimerSystem timerSystem;
	/** 该handle关联的timerTask */
	private final TimerTask timerTask;
	/** timer的创建时间 */
	private final long createTimeMs;
	/** 上下文/附加属性 */
	private Object attachment;

	/** 下次的执行时间 */
	private long nextExecuteTimeMs;
	/** 是否已终止 */
	private boolean terminated = false;

	protected AbstractTimerHandle(TimerSystem timerSystem, TimerTask timerTask, long createTimeMs) {
		this.timerId = timerIdSequencer.incAndGet();
		this.timerSystem = timerSystem;
		this.createTimeMs = createTimeMs;
		this.timerTask = timerTask;
	}

	@Nonnull
	@Override
	public TimerSystem timerSystem() {
		return timerSystem;
	}

	@Nonnull
	@Override
	public TimerTask timerTask() {
		return timerTask;
	}

	@Override
	public long createTimeMs() {
		return createTimeMs;
	}

	@Override
	public long executeDelay() {
		if (terminated) {
			return -1;
		}
		return Math.max(0, nextExecuteTimeMs - timeProvider().getSystemMillTime());
	}

	@Override
	public final <T> T attach(@Nullable Object newData) {
		@SuppressWarnings("unchecked")
		T pre = (T) attachment;
		this.attachment = newData;
		return pre;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T> T attachment() {
		return (T) attachment;
	}

	@Override
	public final void cancel() {
		if (!terminated) {
			doCancel();
			terminated = true;
		}
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	protected SystemTimeProvider timeProvider() {
		return timerSystem.timeProvider();
	}

	@SuppressWarnings("WeakerAccess")
	public final long getTimerId() {
		return timerId;
	}

	void setNextExecuteTimeMs(long nextExecuteTimeMs) {
		this.nextExecuteTimeMs = nextExecuteTimeMs;
	}

	long getNextExecuteTimeMs() {
		return nextExecuteTimeMs;
	}

	final void setTerminated() {
		this.terminated = true;
	}

	/**
	 * timer创建时进行初始化。
	 * @param curTimeMs 当前系统时间
	 */
	protected abstract void init(long curTimeMs);

	/**
	 * 任务执行一次之后，更新状态
	 * @param curTimeMs 当前系统时间
	 */
	protected abstract void afterExecute(long curTimeMs);

	/**
	 * 执行取消操作
	 */
	protected abstract void doCancel();

}
