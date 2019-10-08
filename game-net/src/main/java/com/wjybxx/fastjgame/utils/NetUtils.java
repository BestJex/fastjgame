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

import com.wjybxx.fastjgame.annotation.UnstableApi;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.manager.NetConfigManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * 网络包工具类
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/27 10:28
 * github - https://github.com/hl845740757
 */
public class NetUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    /**
     * 最大缓冲区大小1M,一个消息如果超过1M不能忍。
     */
    public static final int MAX_BUFFER_SIZE = 1024 * 1024;
    /**
     * 读超时handler的名字
     */
    public static final String READ_TIMEOUT_HANDLER_NAME = "readTimeoutHandler";
    /**
     * 本机内网地址
     */
    private static final String localIp;
    /**
     * 本机外网地址
     */
    private static final String outerIp;
    /**
     * 请求图标的路径
     */
    public static final String FAVICON_PATH = "/favicon.ico";

    static {
        String tempLocalIp = null;
        String tempOuterIp = null;

        // 优先使用配置指定的ip
        try {
            ConfigWrapper configWrapper = ConfigLoader.loadConfig(ClassLoader.getSystemClassLoader(), NetConfigManager.NET_CONFIG_NAME);
            tempLocalIp = configWrapper.getAsString("localIp");
            tempOuterIp = configWrapper.getAsString("outerIp");

        } catch (IOException ignore) {
        }

        localIp = null == tempLocalIp ? findLocalIp() : tempLocalIp;
        outerIp = null == tempOuterIp ? findOuterIp() : tempOuterIp;

        logger.info("localIp {}", localIp);
        logger.info("outerIp {}", outerIp);
    }

    // close
    private NetUtils() {

    }

    /**
     * 计算sessionId对应的固定的key
     *
     * @param sessionId 待计算的sessionId
     * @return 对于同一个sessionId，它计算得到的key是不变的
     */
    public static int fixedKey(String sessionId) {
        return sessionId.hashCode();
    }

    /**
     * 计算Channel对应的固定的key
     *
     * @param channel 待计算的channel
     * @return 对于同一个Channel，它计算得到的key是不变的
     */
    public static int fixedKey(Channel channel) {
        return channel.id().hashCode();
    }

    /**
     * 安静的关闭channel,不产生任何影响
     */
    public static void closeQuietly(Channel channel) {
        if (null != channel) {
            try {
                channel.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 安静的关闭future，不产生任何影响
     */
    public static void closeQuietly(ChannelFuture channelFuture) {
        if (null != channelFuture) {
            try {
                channelFuture.cancel(true);
                channelFuture.channel().close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 安静的关闭ctx，不产生任何影响
     */
    public static void closeQuietly(ChannelHandlerContext ctx) {
        if (null != ctx) {
            try {
                ctx.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 关闭一个资源
     */
    public static void closeQuietly(Closeable resource) {
        if (null != resource) {
            try {
                resource.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 计算byteBuf指定区域字节的校验和
     *
     * @param byteBuf byteBuf
     * @param offset  偏移量
     * @param length  有效长度，不可越界
     * @return 校验和
     */
    public static long calChecksum(ByteBuf byteBuf, int offset, int length) {
        long checkSum = 0;
        for (int index = offset, end = offset + length; index < end; index++) {
            checkSum += (byteBuf.getByte(index) & 255);
        }
        return checkSum;
    }

    /**
     * 获取机器内网ip
     *
     * @return 内网地址
     */
    @UnstableApi
    public static String getLocalIp() {
        return localIp;
    }

    /**
     * 获取机器外网ip
     *
     * @return 如果无法获取到外网地址，返回的是内网地址
     */
    @UnstableApi
    public static String getOuterIp() {
        return outerIp;
    }

    /**
     * 查找内网ip。
     *
     * @return localIp
     */
    private static String findLocalIp() {
        String hostAddress = "127.0.0.1";
        try {
            hostAddress = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("get localHost caught exception,use {} instead.", hostAddress, e);
        }
        return hostAddress;
    }

    /**
     * 获取本机外网ip，如果不存在则返回内网ip。
     * <p>
     * 个人对此不是很熟，参考自以下文章
     * - http://www.voidcn.com/article/p-rludpgmk-yb.html
     * - https://rainbow702.iteye.com/blog/2066431
     * <p>
     * {@link Inet4Address#isSiteLocalAddress()} 是否是私有网段
     * 10/8 prefix ，172.16/12 prefix ，192.168/16 prefix
     * {@link Inet4Address#isLoopbackAddress()} 是否是本机回环ip (127.x.x.x)
     * {@link Inet4Address#isAnyLocalAddress()} 是否是通配符地址 (0.0.0.0)
     *
     * @return outerIp
     */
    private static String findOuterIp() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!(ip instanceof Inet4Address)) {
                        continue;
                    }
                    // 回环地址
                    Inet4Address inet4Address = (Inet4Address) ip;
                    if (inet4Address.isLoopbackAddress()) {
                        continue;
                    }
                    // 通配符地址
                    if (inet4Address.isAnyLocalAddress()) {
                        continue;
                    }
                    // 私有地址(内网地址)
                    if (ip.isSiteLocalAddress()) {
                        continue;
                    }
                    return inet4Address.getHostAddress();
                }
            }
        } catch (SocketException e) {
            logger.error("find outerIp caught exception,will use localIp instead.", e);
            return findLocalIp();
        }
        logger.error("can't find outerIp, will use localIp instead.");
        return findLocalIp();
    }

    /**
     * 创建一个初始化好的byteBuf
     * 预分配消息长度 校验和 和包类型字段
     *
     * @param ctx           获取allocator
     * @param contentLength 有效内容长度
     * @param pkgType       包类型
     * @return 以初始化前三个字段
     */
    public static ByteBuf newInitializedByteBuf(ChannelHandlerContext ctx, int contentLength, byte pkgType) {
        // 消息长度字段 + 校验和 + 包类型
        ByteBuf byteBuf = ctx.alloc().buffer(4 + 8 + 1 + contentLength);
        byteBuf.writeInt(0);
        byteBuf.writeLong(0);
        byteBuf.writeByte(pkgType);
        return byteBuf;
    }

    /**
     * 添加长度字段和校验和
     */
    public static void appendLengthAndCheckSum(ByteBuf byteBuf) {
        byteBuf.setInt(0, byteBuf.readableBytes() - 4);
        byteBuf.setLong(4, NetUtils.calChecksum(byteBuf, 12, byteBuf.readableBytes() - 12));
    }

    /**
     * 将byteBuf中剩余的字节读取到一个字节数组中。
     *
     * @param byteBuf 方法返回之后 readableBytes == 0
     * @return new instance
     */
    public static byte[] readRemainBytes(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() == 0) {
            return new byte[0];
        }
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return result;
    }

    /**
     * 设置channel性能偏好.
     * <p>
     * 可参考 - https://blog.csdn.net/zero__007/article/details/51723434
     * <p>
     * 在 JDK 1.5 中, 还为 Socket 类提供了{@link Socket#setPerformancePreferences(int, int, int)}方法:
     * 以上方法的 3 个参数表示网络传输数据的 3 选指标.
     * connectionTime: 表示用最少时间建立连接.
     * latency: 表示最小延迟.
     * bandwidth: 表示最高带宽.
     * setPerformancePreferences() 方法用来设定这 3 项指标之间的相对重要性.
     * 可以为这些参数赋予任意的整数, 这些整数之间的相对大小就决定了相应参数的相对重要性.
     * 例如, 如果参数 connectionTime 为 2, 参数 latency 为 1, 而参数bandwidth 为 3,
     * 就表示最高带宽最重要, 其次是最少连接时间, 最后是最小延迟.
     */
    public static void setChannelPerformancePreferences(Channel channel) {
        ChannelConfig channelConfig = channel.config();
        if (channelConfig instanceof DefaultSocketChannelConfig) {
            DefaultSocketChannelConfig socketChannelConfig = (DefaultSocketChannelConfig) channelConfig;
            socketChannelConfig.setPerformancePreferences(0, 1, 2);
            socketChannelConfig.setAllocator(PooledByteBufAllocator.DEFAULT);
        }
    }

    public static void main(String[] args) {
        System.out.println("localIp " + localIp);
        System.out.println("outerIp " + outerIp);
    }
}
