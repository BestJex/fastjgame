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

package com.wjybxx.fastjgame.util.eventbus;

import com.wjybxx.fastjgame.util.function.FunctionUtils;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 一个特殊的EventBus实现，它适用于这样的场合：
 * 如果确保出现在某个泛型事件中的child不会在其它任何事件中出现，也就是说泛型事件自身的类型在分发过程是可以忽略的情况下，可以使用该实现。
 * <p>
 * 如果{@link #post(Object)}抛出的事件为 {@link GenericEvent}，则以{@link GenericEvent#child()}的类型进行分发。
 * 如果{@link #post(Object)}抛出的事件非 {@link GenericEvent}，则以{@code event}的类型进行分发。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/21
 * github - https://github.com/hl845740757
 */
public class IdentityEventBus implements EventBus {

    /**
     * eventKey -> handler
     * eventKey：{@link Class}
     */
    private final Map<Class<?>, EventHandler<?>> handlerMap;
    private final Predicate<Class<?>> filter;
    private final Predicate<Class<? extends GenericEvent<?>>> genericFilter;

    public IdentityEventBus() {
        this(EventBusUtils.DEFAULT_EXPECTED_SIZE);
    }

    public IdentityEventBus(int expectedSize) {
        this(expectedSize, FunctionUtils.alwaysTrue(), FunctionUtils.alwaysTrue());
    }

    public IdentityEventBus(int expectedSize, Predicate<Class<?>> filter, Predicate<Class<? extends GenericEvent<?>>> genericFilter) {
        this.handlerMap = new IdentityHashMap<>(expectedSize);
        this.filter = filter;
        this.genericFilter = genericFilter;
    }

    @Override
    public final void post(@Nonnull Object event) {
        if (event instanceof GenericEvent) {
            final GenericEvent<?> g = (GenericEvent<?>) event;
            postImp(event, g.child().getClass());
        } else {
            postImp(event, event.getClass());
        }
    }

    private void postImp(final @Nonnull Object event, final Class<?> eventKey) {
        EventBusUtils.postEventImp(handlerMap, event, eventKey);
    }

    @Override
    public final <T> void register(@Nonnull Class<T> eventType, @Nonnull EventHandler<? super T> handler) {
        if (accept(eventType)) {
            addHandlerImp(eventType, handler);
        }
    }

    /**
     * 是否支持该类型的普通事件
     *
     * @return 如果返回false，则会忽略该对应的handler注册
     */
    private boolean accept(@Nonnull Class<?> eventType) {
        return filter.test(eventType);
    }

    @Override
    public final <T extends GenericEvent<?>> void register(@Nonnull Class<T> genericType, @Nonnull Class<?> childType, @Nonnull EventHandler<? super T> handler) {
        if (acceptGeneric(genericType)) {
            addHandlerImp(childType, handler);
        }
    }

    /**
     * 是否支持该类型的泛型事件
     *
     * @return 如果返回false，则会忽略该对应的handler注册
     */
    private boolean acceptGeneric(Class<? extends GenericEvent<?>> genericType) {
        return genericFilter.test(genericType);
    }

    private void addHandlerImp(Class<?> keyClass, EventHandler<?> handler) {
        EventBusUtils.addHandlerImp(handlerMap, keyClass, handler);
    }

    @Override
    public final void release() {
        handlerMap.clear();
    }
}
