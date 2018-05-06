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

package io.nuls.cache.utils;

import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.model.CacheListenerItem;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

/**
 * @author Niels
 * @date 2018/1/23
 */
public class EhcacheListener implements CacheEventListener {

    private final NulsCacheListener listener;

    public EhcacheListener(NulsCacheListener listener) {
        this.listener = listener;
    }

    @Override
    public void onEvent(CacheEvent event) {
        CacheListenerItem item = new CacheListenerItem(event.getKey(), event.getNewValue(), event.getOldValue());
        switch (event.getType()) {
            case CREATED:
                listener.onCreate(item);
                break;
            case EVICTED:
                listener.onEvict(item);
                break;
            case EXPIRED:
                listener.onExpire(item);
                break;
            case REMOVED:
                listener.onRemove(item);
                break;
            case UPDATED:
                listener.onUpdate(item);
                break;
            default:
                return;
        }
    }
}
