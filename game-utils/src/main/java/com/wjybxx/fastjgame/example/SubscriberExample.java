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

package com.wjybxx.fastjgame.example;

import com.wjybxx.fastjgame.concurrent.DefaultThreadFactory;
import com.wjybxx.fastjgame.concurrent.EventLoop;
import com.wjybxx.fastjgame.eventbus.EventBus;
import com.wjybxx.fastjgame.eventbus.Subscribe;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.concurrent.ThreadFactory;

/**
 * {@link com.wjybxx.fastjgame.eventbus.EventBus}的注册者例子。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/24
 * github - https://github.com/hl845740757
 */
public class SubscriberExample {

    public SubscriberExample(EventBus bus) {
        // 这里只是示例，最好不要在构造方法中注册，未构造完成就发布自己可能存在问题
        SubscriberExampleBusRegister.register(bus, this);
    }

    @Subscribe
    public void hello(String name) {
        System.out.println("hello-" + name);
    }

    @Subscribe
    protected void hello2(String name) {
        System.out.println("hello2-" + name);
    }

    @Subscribe
    void hello3(String name) {
        System.out.println("hello3-" + name);
    }

    @Subscribe
    public void onEvent(String name) {
        System.out.println("onEvent-" + name);
    }

    @Subscribe
    public void onEvent(Integer age) {
        System.out.println("onEvent-" + age);
    }

    @Subscribe
    public void onEvent(EventLoop name) {

    }

    @Subscribe(onlySubEvents = true, subEvents = {
            Integer.class,
            String.class,
            Boolean.class
    })
    public void onEvent(Object event) {
        System.out.println("onEvent - Object " + event);
    }

    @Subscribe(subEvents = {
            DefaultThreadFactory.class,
            InternalThreadFactory.class
    })
    public void onEvent(ThreadFactory defaultThreadFactory) {
        System.out.println("onEvent - ThreadFactory " + defaultThreadFactory);
    }

    //	@Subscribe
    public <T> void illegalMethod() {
        // 如果打开注解，编译会报错
    }

    //	@Subscribe
    public <T> void illegalMethod(T illegal) {
        // 如果打开注解，编译会报错
    }

    //	@Subscribe
    public void illegalMethod(HashSet<String> illegal) {
        // 如果打开注解，编译会报错
    }

    public static void main(String[] args) {
        final EventBus bus = new EventBus();
        new SubscriberExample(bus);

        bus.post("String");
        bus.post(250);

        bus.post(new DefaultThreadFactory("bus"));
    }

    public static class InternalThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@Nonnull Runnable r) {
            return null;
        }
    }
}
