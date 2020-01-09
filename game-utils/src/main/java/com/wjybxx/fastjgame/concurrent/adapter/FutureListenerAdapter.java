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

package com.wjybxx.fastjgame.concurrent.adapter;

import com.wjybxx.fastjgame.concurrent.FutureListener;
import com.wjybxx.fastjgame.concurrent.FutureResult;
import com.wjybxx.fastjgame.concurrent.GenericFutureResultListener;
import com.wjybxx.fastjgame.concurrent.ListenableFuture;

/**
 * 将{@link GenericFutureResultListener}适配为{@link FutureListener}
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/9
 * github - https://github.com/hl845740757
 */
public class FutureListenerAdapter<FR extends FutureResult<V>, V> implements FutureListener<V> {

    private final GenericFutureResultListener<FR> futureResultListener;

    public FutureListenerAdapter(GenericFutureResultListener<FR> futureResultListener) {
        this.futureResultListener = futureResultListener;
    }

    @Override
    public void onComplete(ListenableFuture<? extends V> future) throws Exception {
        @SuppressWarnings("unchecked") final FR fr = (FR) future.getAsResult();
        futureResultListener.onComplete(fr);
    }
}
