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

package com.wjybxx.fastjgame.scene;

import com.wjybxx.fastjgame.shape.Grid2D;
import com.wjybxx.fastjgame.shape.Point2D;
import com.wjybxx.fastjgame.utils.GameConstant;
import com.wjybxx.fastjgame.utils.GridUtils;

import javax.annotation.concurrent.Immutable;

/**
 * 地图格子。
 * 地图被切为一个个小格子，为什么要切割为格子？
 * 基于格子寻路。
 * 因此格子不能过小，太小导致地图切割后的格子数过多，寻路效率会受到影响。
 * 此外格子不能太大，太大导致精细度太差，寻路等效果会很差。
 * <p>
 * 地图左下角为(0,0)
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/6/1 18:29
 * github - https://github.com/hl845740757
 */
@Immutable
public class MapGrid implements Grid2D {

    /**
     * 行索引（y索引）
     */
    private final int rowIndex;
    /**
     * 列索引（x索引）
     */
    private final int colIndex;

    /**
     * 该格子的中心坐标
     */
    private final Point2D center;

    /**
     * 格子遮挡标记
     */
    private final GridObstacle obstacleValue;

    /**
     * 是否处于安全区
     * (更好的方式是创建Area场景对象，可以在地图编辑器编辑，也可以程序生成)
     */
    private final boolean safeArea;

    // TODO 其他标记

    public MapGrid(int rowIndex, int colIndex, int obstacleValue, boolean safeArea) {
        this(rowIndex, colIndex, GridObstacle.forNumber(obstacleValue), safeArea);
    }

    public MapGrid(int rowIndex, int colIndex, GridObstacle obstacleValue, boolean safeArea) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.obstacleValue = obstacleValue;
        this.safeArea = safeArea;

        this.center = GridUtils.gridCenterLocation(rowIndex, colIndex, GameConstant.MAP_GRID_WIDTH)
                .unmodifiable();
    }

    @Override
    public int getColIndex() {
        return colIndex;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public int getX() {
        return colIndex;
    }

    @Override
    public int getY() {
        return rowIndex;
    }

    public Point2D getCenter() {
        return center;
    }

    public float getCenterX() {
        return center.getX();
    }

    public float getCenterY() {
        return center.getY();
    }

    public GridObstacle getObstacleValue() {
        return obstacleValue;
    }

    public boolean isSafeArea() {
        return safeArea;
    }
}
