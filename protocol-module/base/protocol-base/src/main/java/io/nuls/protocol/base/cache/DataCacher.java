/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.protocol.base.cache;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.protocol.constant.MessageDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author: Niels Wang
 */
public class DataCacher<T> {

    private final MessageDataType type;

    public DataCacher(MessageDataType type) {
        this.type = type;
    }

    private Map<NulsDigestData, CompletableFuture<T>> cacher = new HashMap<>();

    public CompletableFuture<T> addFuture(NulsDigestData hash) {
        CompletableFuture future = new CompletableFuture<>();
//        Log.info(type + "创建future：：：" + hash);
        cacher.put(hash, future);
        return future;
    }

    public boolean callback(NulsDigestData hash, T t) {
        return this.callback(hash, t, true);
    }

    public boolean callback(NulsDigestData hash, T t, boolean log) {
        CompletableFuture<T> future = cacher.get(hash);
        if (future == null) {
//            if (log) {
//                Log.warn("Time out: ({}) : {}", type, hash.getDigestHex());
//            }
            return false;
        }
        future.complete(t);
//        Log.info(type + "完成并删除future：：：" + hash);
        cacher.remove(hash);
        return true;
    }

    public void notFound(NulsDigestData hash) {
        CompletableFuture<T> future = cacher.get(hash);
        if (future == null) {
            return;
        }
        future.complete(null);
//        Log.info(type + "not fount删除future：：：" + hash);
        cacher.remove(hash);
    }

    public void removeFuture(NulsDigestData hash) {
//        Log.info(type + "调用删除future：：：" + hash);
        cacher.remove(hash);
    }
}
