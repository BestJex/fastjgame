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

/**
 * 抽象的固定延迟的TimerHandle实现
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/14
 * github - https://github.com/hl845740757
 */
public abstract class AbstractFixedDelayHandle extends AbstractTimerHandle implements FixedDelayHandle {

	private final long initialDelay;

	private long delay;

	/** 理论上的上次执行时间 */
	private long logicLastExecuteTimeMs;

	protected AbstractFixedDelayHandle(TimerSystem timerSystem, long createTimeMs, TimerTask timerTask,
						long initialDelay, long delay) {
		super(timerSystem, timerTask, createTimeMs);
		this.initialDelay = initialDelay;
		this.delay = delay;
	}

	@Override
	public long initialDelay() {
		return initialDelay;
	}

	@Override
	public long delay() {
		return delay;
	}

	@Override
	public final boolean setDelay(long delay) {
		ensureDelay(delay);
		if (isTerminated()) {
			return false;
		} else {
			this.delay = delay;
			return true;
		}
	}

	@Override
	public boolean setDelayImmediately(long delay) {
		if (setDelay(delay)) {
			adjust();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 当修改完delay的时候，进行必要的调整，此时还未修改下次执行时间。
	 */
	protected abstract void adjust();

	@Override
	protected final void init(long curTimeMs) {
		logicLastExecuteTimeMs = curTimeMs + initialDelay;
		updateNextExecuteTime();
	}

	@Override
	protected final void afterExecute(long curTimeMs) {
		// 上次执行时间为真实时间
		logicLastExecuteTimeMs = curTimeMs;
		updateNextExecuteTime();
	}

	/** 更新下一次的执行时间 */
	protected final void updateNextExecuteTime() {
		setNextExecuteTimeMs(logicLastExecuteTimeMs + delay);
	}

	static void ensureDelay(long delay) {
		if (delay <= 0) {
			throw new IllegalArgumentException("delay " + delay);
		}
	}
}