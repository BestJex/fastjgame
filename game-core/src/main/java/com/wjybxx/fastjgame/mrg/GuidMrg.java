package com.wjybxx.fastjgame.mrg;

/**
 * 64位的GUID生成器。
 * GUID，Globally Unique Identifier，全局唯一标识符。
 * 具体的策略由自己决定，数据库，Zookeeper，Redis等等都是可以的。
 *
 * @apiNote
 * 如果没有必要，千万不要维持全局的生成顺序(如redis的incr指令)，那样的guid确实很好，但是在性能上的损失是巨大的。
 * 建议采用预分配的方式，本地缓存一定数量(如10000个)，本地缓存使用完之后再次申请一部分缓存到本地。
 * 如redis的 Incrby 指令: INCRBY guid 10000
 *
 * 缓存越大越安全(对方挂掉的影响越小)，但容易造成资源浪费，缓存过小又降低了缓存的意义；这个全凭自己估量。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 11:47
 * @github - https://github.com/hl845740757
 */
public interface GuidMrg {

    /**
     * 生成一个全局唯一的id
     * @return unique
     */
    long generateGuid();

}
