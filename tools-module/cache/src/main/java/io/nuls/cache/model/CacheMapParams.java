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

package io.nuls.cache.model;

import io.nuls.cache.listener.intf.NulsCacheListener;
import org.ehcache.spi.copy.Copier;

import java.io.Serializable;

/**
 * @author: Niels Wang
 */
public class CacheMapParams {
    private int heapMb;
    private int timeToLiveSeconds;
    private int timeToIdleSeconds;
    private NulsCacheListener listener;
    private Copier valueCopier;
    private Class keyType;
    private Class<? extends Serializable> valueType;

    public CacheMapParams (){}

    public CacheMapParams(int heapMb,Class keyType,Class<? extends Serializable> valueType, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener, Copier valueCopier){
        this.heapMb = heapMb;
        this.keyType = keyType;
        this.valueCopier = valueCopier;
        this.valueType = valueType;
        this.timeToIdleSeconds = timeToIdleSeconds;
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.listener = listener;
    }

    public int getHeapMb() {
        return heapMb;
    }

    public void setHeapMb(int heapMb) {
        this.heapMb = heapMb;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public int getTimeToIdleSeconds() {
        return timeToIdleSeconds;
    }

    public void setTimeToIdleSeconds(int timeToIdleSeconds) {
        this.timeToIdleSeconds = timeToIdleSeconds;
    }

    public NulsCacheListener getListener() {
        return listener;
    }

    public void setListener(NulsCacheListener listener) {
        this.listener = listener;
    }

    public Copier getValueCopier() {
        return valueCopier;
    }

    public Class getKeyType() {
        return keyType;
    }

    public void setKeyType(Class keyType) {
        this.keyType = keyType;
    }

    public Class<? extends Serializable> getValueType() {
        return valueType;
    }

    public void setValueType(Class<? extends Serializable> valueType) {
        this.valueType = valueType;
    }

    public void setValueCopier(Copier valueCopier) {
        this.valueCopier = valueCopier;
    }
}
