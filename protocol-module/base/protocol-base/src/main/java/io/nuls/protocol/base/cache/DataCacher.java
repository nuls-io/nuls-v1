/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
 * @date: 2018/6/26
 */
public class DataCacher<T> {

    private final MessageDataType type;

    public DataCacher(MessageDataType type) {
        this.type = type;
    }

    private Map<NulsDigestData, CompletableFuture<T>> cacher = new HashMap<>();

    public CompletableFuture<T> addFuture(NulsDigestData hash) {
        CompletableFuture future = new CompletableFuture<>();
        cacher.put(hash, future);
        return future;
    }

    public void callback(NulsDigestData hash, T t) {
        this.callback(hash, t, true);
    }

    public void callback(NulsDigestData hash, T t, boolean log) {
        CompletableFuture<T> future = cacher.get(hash);
        if (future == null) {
            if (log) {
                Log.warn("Time out: ({}) : {}", type, hash.getDigestHex());
            }
            return;
        }
        future.complete(t);
        cacher.remove(hash);
    }

    public void notFound(NulsDigestData hash) {
        CompletableFuture<T> future = cacher.get(hash);
        if (future == null) {
            Log.warn("{} NotFound : " + hash, type);
            return;
        }
        future.complete(null);
        cacher.remove(hash);
    }

    public void removeFuture(NulsDigestData hash) {
        cacher.remove(hash);
    }
}
