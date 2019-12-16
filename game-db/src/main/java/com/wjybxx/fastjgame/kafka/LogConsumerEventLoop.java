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

package com.wjybxx.fastjgame.kafka;

import com.wjybxx.fastjgame.concurrent.RejectedExecutionHandler;
import com.wjybxx.fastjgame.concurrent.disruptor.DisruptorEventLoop;
import com.wjybxx.fastjgame.concurrent.disruptor.TimeoutWaitStrategyFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * kafka消费者事件循环
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/11/28
 * github - https://github.com/hl845740757
 */
public class LogConsumerEventLoop extends DisruptorEventLoop {

    private static final Logger logger = LoggerFactory.getLogger(LogConsumerEventLoop.class);

    /**
     * 日志线程任务缓冲区大小
     * 消费者需要响应的事件不多，因此较小
     */
    private static final int CONSUMER_RING_BUFFER_SIZE = 8192;
    private static final int CONSUMER_BATCH_EVENT_SIZE = 1024;

    /**
     * 无事件消费时阻塞等待时间
     */
    private static final int CONSUMER_BLOCK_TIME_MS = 50;

    /**
     * 消费者拉取数据最长阻塞时间
     */
    private static final Duration CONSUMER_POLL_DURATION = Duration.ofMillis(100);

    private final KafkaConsumer<String, String> consumer;
    private final Set<String> subscribedTopics;
    private final LogConsumer logConsumer;

    public LogConsumerEventLoop(@Nonnull ThreadFactory threadFactory,
                                @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                                @Nonnull String brokerList,
                                @Nonnull Set<String> subscribedTopics,
                                @Nonnull String groupId,
                                @Nonnull LogConsumer logConsumer) {
        super(null, threadFactory, rejectedExecutionHandler, CONSUMER_RING_BUFFER_SIZE, CONSUMER_BATCH_EVENT_SIZE, newWaitStrategyFactory());
        consumer = new KafkaConsumer<>(newConfig(brokerList, groupId), new StringDeserializer(), new StringDeserializer());
        this.subscribedTopics = subscribedTopics;
        this.logConsumer = logConsumer;
    }

    @Nonnull
    private static TimeoutWaitStrategyFactory newWaitStrategyFactory() {
        return new TimeoutWaitStrategyFactory(CONSUMER_BLOCK_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void init() throws Exception {
        consumer.subscribe(subscribedTopics);
    }

    @Override
    protected void loopOnce() throws Exception {
        final ConsumerRecords<String, String> records = consumer.poll(CONSUMER_POLL_DURATION);
        if (records.isEmpty()) {
            return;
        }

        try {
            for (ConsumerRecord<String, String> record : records) {
                consumeSafely(record);
            }
        } finally {
            // 提交消费记录 - 如果使用自动提交，参数设置不当时，容易导致重复消费。
            consumer.commitSync();
        }
    }

    @Override
    protected void clean() throws Exception {
        consumer.close();
    }

    private void consumeSafely(ConsumerRecord consumerRecord) {
        try {
            logConsumer.consume(consumerRecord);
        } catch (Throwable e) {
            logger.warn("logConsumer.consume caught exception", e);
        }
    }

    private static Properties newConfig(final String brokerList, final String groupId) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }
}