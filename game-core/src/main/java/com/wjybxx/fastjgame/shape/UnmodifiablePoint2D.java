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

package com.wjybxx.fastjgame.shape;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/31 23:22
 * github - https://github.com/hl845740757
 */
public class UnmodifiablePoint2D extends Point2D {

    private final float x;

    private final float y;

    public UnmodifiablePoint2D(Point2D point2D) {
        this.x = point2D.getX();
        this.y = point2D.getY();
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void updateLocation(float x, float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setX(float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setY(float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point2D unmodifiable() {
        return this;
    }

    @Override
    public String toString() {
        return "UnmodifiablePoint2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
