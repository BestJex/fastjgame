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

package com.wjybxx.fastjgame.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * EventBus的一个简单实现。
 * 1. 它并不是一个线程安全的对象
 * 2. 它也不是一个标准的EventBus实现，比如就没有取消注册的接口，也没有单独的dispatcher、Registry
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/23
 * github - https://github.com/hl845740757
 */
@NotThreadSafe
public class EventBus implements EventHandlerRegistry,EventDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
	/**
	 * 事件类型到处理器的映射
	 */
	private final Map<Class<?>, EventHandler<?>> handlerMap = new IdentityHashMap<>(512);

	@SuppressWarnings("unchecked")
	@Override
	public <T> void post(@Nonnull T event) {
		final EventHandler<T> eventHandler = (EventHandler<T>) handlerMap.get(event.getClass());
		if (null == eventHandler) {
			// 对应的事件处理器可能忘记了注册
			logger.warn("{}'s listeners may forgot register!", event.getClass().getName());
			return;
		}
		try {
			eventHandler.onEvent(event);
		} catch (Exception e){
			// 不能因为某个异常导致其它监听器接收不到事件
			logger.warn("onEvent caught exception! EventInfo {}, handler info {}",
					event.getClass().getName(), eventHandler.getClass().getName(), e);
		}
	}

	@Override
	public <T> void register(@Nonnull Class<T> eventType, @Nonnull EventHandler<T> handler) {
		@SuppressWarnings("unchecked")
		final EventHandler<T> existHandler = (EventHandler<T>) handlerMap.get(eventType);
		if (null == existHandler) {
			handlerMap.put(eventType, handler);
		} else {
			if (existHandler instanceof CompositeEventHandler) {
				((CompositeEventHandler<T>) existHandler).addHandler(handler);
			} else {
				handlerMap.put(eventType, new CompositeEventHandler<>(existHandler, handler));
			}
		}
	}
}