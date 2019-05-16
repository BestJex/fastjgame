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

import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.nodename.CenterServerNodeName;
import com.wjybxx.fastjgame.core.nodename.CrossSceneNodeName;
import com.wjybxx.fastjgame.core.nodename.SingleSceneNodeName;
import com.wjybxx.fastjgame.core.nodename.WarzoneNodeName;
import com.wjybxx.fastjgame.net.common.RoleType;
import org.apache.curator.utils.PathUtils;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 20:49
 * @github - https://github.com/hl845740757
 */
public class ZKUtils {

    private static final String CHANNELID_PREFIX="channel-";

    /**
     * 寻找节点的名字，即最后一部分
     * @param path
     * @return
     */
    public static String findNodeName(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        return path.substring(delimiterIndex+1);
    }

    /**
     * 寻找节点的父节点路径
     * @param path 路径参数，不可以是根节点("/")
     * @return
     */
    public static String findParentPath(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        // root(nameSpace)
        if (delimiterIndex == 0){
            throw new IllegalArgumentException("path " + path + " is root");
        }
        return path.substring(0,delimiterIndex);
    }

    /**
     * 构建一个全路径
     * @param parent 父节点全路径
     * @param nodeName 属性名字
     * @return
     */
    public static String makePath(String parent,String nodeName){
        return parent + "/" + nodeName;
    }

    /**
     * 找到一个合适的锁节点，锁的范围越小越好。
     * 对于非根节点来说，使用它的父节点路径。
     * 对应根节点来说，使用全局锁位置。
     * @param path 查询的路径
     * @return 一个合适的加锁路径
     */
    public static String findAppropriateLockPath(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        if (delimiterIndex == 0){
            return globalLockPath();
        }
        return path.substring(0,delimiterIndex);
    }

    /**
     * 获取全局锁路径
     * @return
     */
    private static String globalLockPath() {
        return "/globalLock";
    }

    /**
     * 返回全局guidIndex所在路径
     * @return
     */
    public static String guidIndexPath(){
        return "/mutex/guid/guidIndex";
    }

    /**
     * 同一个战区下的服务器注册在同一节点下
     * @param warzoneId 战区id
     * @return
     */
    public static String onlineParentPath(int warzoneId){
        return "/online/warzone" + warzoneId;
    }

    /**
     * 本服进程申请channelId的地方(本服内竞争)
     * @param warzoneId 战区id
     * @param serverId 服id
     * @return 父节点路径
     */
    public static String singleChannelPath(int warzoneId,int serverId){
        // 不弄那么深
        return "/online/channel/single/" + warzoneId + "-" + serverId + "/" + CHANNELID_PREFIX;
    }

    /**
     * 跨服进程申请channelId的地方(战区内竞争)
     * @param warzoneId 战区id
     * @return 父节点路径
     */
    public static String crossChannelPath(int warzoneId){
        return "/online/channel/cross/" + warzoneId + "/" + CHANNELID_PREFIX;
    }

    /**
     * 解析临时顺序节点的序号
     * @param path 有序节点的名字
     * @return zk默认序号是从0开始的，最小为0
     */
    public static int parseSequentialId(String path){
        return Integer.parseInt(findNodeName(path).split("-",2)[1]);
    }

    /**
     * 为指定本服scene进程创建一个有意义的节点名字，用于注册到zookeeper
     * @param warzoneId 战区id
     * @param serverId 几服
     * @param processGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildSingleSceneNodeName(int warzoneId, int serverId, long processGuid){
        return RoleType.SCENE_SERVER  + "-" + SceneProcessType.SINGLE.name() + "-" + warzoneId + "-" + serverId + "-" + processGuid;
    }

    /**
     * 解析本地scene进程的节点路径(名字)
     * @param path fullpath
     * @return scene包含的基本信息
     */
    public static SingleSceneNodeName parseSingleSceneNodeName(String path){
        String[] params = findNodeName(path).split("-",5);
        int warzoneId = Integer.parseInt(params[2]);
        int serverId = Integer.parseInt(params[3]);
        long processGuid = Long.parseLong(params[4]);
        return new SingleSceneNodeName(warzoneId,serverId,processGuid);
    }

    /**
     * 为跨服节点创建一个有意义的节点名字，用于注册到zookeeper
     * @param warzoneId 战区id
     * @param processGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildCrossSceneNodeName(int warzoneId, long processGuid){
        return RoleType.SCENE_SERVER  + "-" + SceneProcessType.CROSS.name() + "-" + warzoneId + "-" + processGuid;
    }

    /**
     * 解析跨服节点的节点路径(名字)
     * @param path fullpath
     * @return 跨服节点信息
     */
    public static CrossSceneNodeName parseCrossSceneNodeName(String path){
        String[] params = findNodeName(path).split("-", 4);
        int warzoneId = Integer.parseInt(params[2]);
        long processGuid = Long.parseLong(params[3]);
        return new CrossSceneNodeName(warzoneId,processGuid);
    }

    /**
     * 通过场景节点的名字解析场景进程的类型
     * @param sceneNodePath scene节点的名字
     * @return scene进程的类型
     */
    public static SceneProcessType parseSceneType(String sceneNodePath){
        String[] params = findNodeName(sceneNodePath).split("-");
        return SceneProcessType.valueOf(params[1]);
    }

    /**
     * 为指定服创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @param serverId 几服
     * @return 唯一的有意义的名字
     */
    public static String buildCenterNodeName(int warzoneId, int serverId){
        return RoleType.CENTER_SERVER + "-" + warzoneId + "-" + serverId;
    }

    /**
     * 解析game节点的路径(名字)
     * @param gameNodeName fullpath
     * @return game服的信息
     */
    public static CenterServerNodeName parseCenterNodeName(String gameNodeName){
        String[] params = findNodeName(gameNodeName).split("-", 3);
        int warzoneId = Integer.parseInt(params[1]);
        int serverId = Integer.parseInt(params[2]);
        return new CenterServerNodeName(warzoneId, serverId);
    }

    /**
     * 为战区创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @return 唯一的有意义的名字
     */
    public static String buildWarzoneNodeName(int warzoneId){
        return RoleType.WARZONE_SERVER + "-" + warzoneId;
    }

    /**
     * 解析战区的节点路径(名字)
     * @param path fullpath
     * @return 战区基本信息
     */
    public static WarzoneNodeName parseWarzoneNodeNode(String path) {
        String[] params = findNodeName(path).split("-", 2);
        int warzoneId = Integer.parseInt(params[1]);
        return new WarzoneNodeName(warzoneId);
    }

    /**
     * 通过服务器的节点名字解析服务器的类型
     * @param nodeName 服务器节点名字
     * @return 返回服务器的类型
     */
    public static RoleType parseServerType(String nodeName){
        return RoleType.valueOf(nodeName.split("-",2)[0]);
    }

}
