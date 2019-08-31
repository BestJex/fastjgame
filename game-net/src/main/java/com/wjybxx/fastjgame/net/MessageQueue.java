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

package com.wjybxx.fastjgame.net;

import com.wjybxx.fastjgame.misc.LongSequencer;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.LinkedList;

/**
 * 消息队列，可与tcp的收发缓冲区比较
 * （知识点：滑动窗口，捎带确认）
 * <pre>
 * 消息队列的视图大致如下：
 *
 *              ↓nextSequence
 * |---------------------------
 * | sentQueue |  unsentQueue  |
 * | --------------------------|
 * |    0~n    |      0~n      |
 * |---------------------------
 * </pre>
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/27 11:43
 * github - https://github.com/hl845740757
 */
public final class MessageQueue {
    /**
     * 初始ACK
     */
    public static final int INIT_ACK = 0;

    /**
     * 序号分配器
     */
    private final LongSequencer sequencer = new LongSequencer(INIT_ACK);

    /**
     * 接收到对方的最大消息编号
     */
    private long ack = INIT_ACK;

    /**
     * 已发送待确认的消息队列，只要发送过就不会再放入{@link #unsentQueue}
     * Q: 为什么不使用arrayList?
     * A: 1.存在大量的删除操作 2.太占用内存。 3.遍历很少
     */
    private LinkedList<SentMessage> sentQueue = new LinkedList<>();

    /**
     * 未发送的消息队列,还没有尝试过发送的消息
     */
    private LinkedList<UnsentMessage> unsentQueue = new LinkedList<>();

    // ---------------------------------------------- 已发送的rpc信息 -----------------------------------
    /**
     * RpcRequestId分配器
     */
    private final LongSequencer rpcRequestGuidSequencer = new LongSequencer(0);

    /**
     * 当前会话上的rpc请求
     * (提供顺序保证，先发起的请求先超时)
     */
    private Long2ObjectMap<RpcPromiseInfo> rpcPromiseInfoMap = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * 对方发送过来的ack是否有效。
     * 每次返回的Ack不小于上次返回的ack,不大于发出去的最大消息号
     */
    public boolean isAckOK(long ack){
        return ack >= getAckLowerBound() && ack <= getAckUpperBound();
    }

    /**
     * 获取上一个已确认的消息号
     */
    private long getAckLowerBound(){
        // 有已发送未确认的消息，那么它的上一个就是ack下界
        if (sentQueue.size()>0){
            return sentQueue.getFirst().getSequence()-1;
        }
        // 都已确认，且没有新消息，那么上次分配的就是ack下界
        return sequencer.get();
    }

    /**
     * 获取下一个可能的最大ackGuid，
     */
    private long getAckUpperBound(){
        // 有已发送待确认的消息，那么它的最后一个就是ack上界
        if (sentQueue.size()>0){
            return sentQueue.getLast().getSequence();
        }
        // 都已确认，且没有新消息，那么上次分配的就是ack上界
        return sequencer.get();
    }


    /**
     * 根据对方发送的ack更新已发送队列
     * @param ack 对方发来的ack
     */
    public void updateSentQueue(long ack){
        if (!isAckOK(ack)){
            throw new IllegalArgumentException(generateAckErrorInfo(ack));
        }
        while (sentQueue.size()>0){
            if (sentQueue.getFirst().getSequence()>ack){
                break;
            }
            sentQueue.removeFirst();
        }
    }

    /**
     * 生成ack信息
     * @param ack 服务器发送来的ack
     */
    public String generateAckErrorInfo(long ack) {
        return String.format("{ack=%d, lastAckGuid=%d, nextMaxAckGuid=%d}", ack, getAckLowerBound(), getAckUpperBound());
    }

    /**
     * 分配下一个包的编号
     */
    public long nextSequence(){
        return sequencer.incAndGet();
    }

    public long getAck() {
        return ack;
    }

    public void setAck(long ack) {
        this.ack = ack;
    }

    public LinkedList<SentMessage> getSentQueue() {
        return sentQueue;
    }

    public LinkedList<UnsentMessage> getUnsentQueue() {
        return unsentQueue;
    }

    public Long2ObjectMap<RpcPromiseInfo> getRpcPromiseInfoMap() {
        return rpcPromiseInfoMap;
    }

    public long nextRpcRequestGuid() {
        return rpcRequestGuidSequencer.incAndGet();
    }

    /** 交换未发送的缓冲区 */
    public LinkedList<UnsentMessage> exchangeUnsentMessages() {
        LinkedList<UnsentMessage> result = unsentQueue;
        unsentQueue = new LinkedList<>();
        return result;
    }

    /** 删除rpcPromiseInfoMap并返回 */
    public Long2ObjectMap<RpcPromiseInfo> detachRpcPromiseInfoMap() {
        Long2ObjectMap<RpcPromiseInfo> result = rpcPromiseInfoMap;
        rpcPromiseInfoMap = null;
        return result;
    }

    /** 删除已发送和未发送的消息队列 */
    public void detachMessageQueue() {
        sentQueue = null;
        unsentQueue = null;
    }

    /**
     * 获取当前缓存的消息数
     */
    public int getCacheMessageNum(){
        return sentQueue.size() + unsentQueue.size();
    }

    @Override
    public String toString() {
        return "MessageQueue{" +
                "sequencer=" + sequencer +
                ", ack=" + ack +
                ", sentQueueSize=" + sentQueue.size() +
                ", needSendQueueSize=" + unsentQueue.size() +
                "}";
    }
}
