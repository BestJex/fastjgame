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

package com.wjybxx.fastjgame.mgr;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.core.onlinenode.SceneNodeData;
import com.wjybxx.fastjgame.core.onlinenode.SceneNodeName;
import com.wjybxx.fastjgame.core.onlinenode.WarzoneNodeData;
import com.wjybxx.fastjgame.core.onlinenode.WarzoneNodeName;
import com.wjybxx.fastjgame.misc.CloseableHandle;
import com.wjybxx.fastjgame.misc.RoleType;
import com.wjybxx.fastjgame.utils.JsonUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

/**
 * CenterServer端的节点发现逻辑，类似服务发现。
 * <p>
 * CenterServer需要探测所有的scene和warzone，并派发事件与之建立链接
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/15 23:06
 * github - https://github.com/hl845740757
 */
public class CenterDiscoverMgr {

    private static final Logger logger = LoggerFactory.getLogger(CenterDiscoverMgr.class);

    private final CuratorMgr curatorMgr;
    private final CenterWorldInfoMgr centerWorldInfoMgr;
    private final WarzoneInCenterInfoMgr warzoneInCenterInfoMgr;
    private final SceneInCenterInfoMgr sceneInCenterInfoMgr;

    /**
     * 资源句柄，提供关闭关联的资源的方法
     */
    private CloseableHandle closeableHandle;
    /**
     * 当前在先节点信息，只在逻辑线程使用
     */
    private Map<String, ChildData> onlineNodeInfoMap = new HashMap<>();

    @Inject
    public CenterDiscoverMgr(CuratorMgr curatorMgr, CenterWorldInfoMgr centerWorldInfoMgr,
                             WarzoneInCenterInfoMgr warzoneInCenterInfoMgr, SceneInCenterInfoMgr sceneInCenterInfoMgr) {
        this.curatorMgr = curatorMgr;
        this.centerWorldInfoMgr = centerWorldInfoMgr;
        this.warzoneInCenterInfoMgr = warzoneInCenterInfoMgr;
        this.sceneInCenterInfoMgr = sceneInCenterInfoMgr;
    }

    public void start() throws Exception {
        String watchPath = ZKPathUtils.onlineParentPath(centerWorldInfoMgr.getWarzoneId());
        closeableHandle = curatorMgr.watchChildren(watchPath, (client, event) -> onEvent(event.getType(), event.getData()));
    }

    public void shutdown() throws IOException {
        if (closeableHandle != null) {
            closeableHandle.close();
        }
    }

    /**
     * 新版本：回调就在world所在线程，不必考虑线程安全性。
     */
    private void onEvent(Type type, ChildData childData) {
        // 只处理节点增加和移除两件事情
        if (type != Type.CHILD_ADDED && type != Type.CHILD_REMOVED) {
            return;
        }
        String nodeName = ZKPathUtils.findNodeName(childData.getPath());
        RoleType roleType = ZKPathUtils.parseServerType(nodeName);
        // 只处理战区和scene信息
        if (roleType != RoleType.SCENE && roleType != RoleType.WARZONE) {
            return;
        }

        // 更新缓存(方便debug跟踪)
        if (type == Type.CHILD_ADDED) {
            onlineNodeInfoMap.put(childData.getPath(), childData);
        } else {
            onlineNodeInfoMap.remove(childData.getPath());
        }

        if (roleType == RoleType.SCENE) {
            onSceneEvent(type, childData);
        } else {
            onWarzoneEvent(type, childData);
        }
    }

    /**
     * 该节点下会有我的私有场景、其它服的场景和跨服场景
     *
     * @param type      事件类型
     * @param childData 场景数据
     */
    private void onSceneEvent(Type type, ChildData childData) {
        final SceneNodeName sceneNodeName = ZKPathUtils.parseSceneNodeName(childData.getPath());
        final SceneNodeData sceneNodeData = JsonUtils.parseJsonBytes(childData.getData(), SceneNodeData.class);
        if (type == Type.CHILD_ADDED) {
            sceneInCenterInfoMgr.onDiscoverSceneNode(sceneNodeName, sceneNodeData);
            logger.info("discover scene {}", sceneNodeName.getWorldGuid());
        } else {
            // remove
            sceneInCenterInfoMgr.onSceneNodeRemoved(sceneNodeName, sceneNodeData);
            logger.info("remove scene {}", sceneNodeName.getWorldGuid());
        }
    }

    /**
     * 监测的路径下只会有一个战区节点，且节点名字是不会变的。
     * <p>
     * 由于节点名字不会变，那么只能保证 有一次add必然有一次remove，但是remove对应的数据可能和add并不一致！
     * 见测试用例中的 WatcherTest说明
     *
     * @param type      事件类型
     * @param childData 战区数据
     */
    private void onWarzoneEvent(Type type, ChildData childData) {
        WarzoneNodeName warzoneNodeName = ZKPathUtils.parseWarzoneNodeName(childData.getPath());
        WarzoneNodeData warzoneNodeData = JsonUtils.parseJsonBytes(childData.getData(), WarzoneNodeData.class);
        if (type == Type.CHILD_ADDED) {
            warzoneInCenterInfoMgr.onDiscoverWarzone(warzoneNodeName, warzoneNodeData);
            logger.debug("discover warzone {}", warzoneNodeName.getWarzoneId());
        } else {
            // child remove
            warzoneInCenterInfoMgr.onWarzoneNodeRemoved(warzoneNodeName, warzoneNodeData);
            logger.debug("remove warzone {}", warzoneNodeName.getWarzoneId());
        }
    }
}
