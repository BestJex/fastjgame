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

package com.wjybxx.fastjgame.test;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 有一些特殊数字，commons包的解析不令人满意。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/7 23:00
 * github - https://github.com/hl845740757
 */
public class NumberUtilsTest {

    public static void main(String[] args) {
        String numStr = "08";
        System.out.println(StringUtils.isNumeric(numStr));
        System.out.println(NumberUtils.isNumber(numStr));
        System.out.println(NumberUtils.toInt(numStr));
    }
}
