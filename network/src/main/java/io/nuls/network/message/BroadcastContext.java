/**
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
 */
package io.nuls.network.message;

import io.nuls.network.entity.BroadcastResult;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author vivi
 * @date 2017/12/8.
 */
public class BroadcastContext {

    private static final BroadcastContext INSTALL = new BroadcastContext();

    private Map<String, BroadcastResult> context = new Hashtable<String, BroadcastResult>();

    private BroadcastContext() {
    }

    public synchronized static BroadcastContext get() {
        return INSTALL;
    }

    public void add(String hash, BroadcastResult result) {
        context.put(hash, result);
    }

    public boolean exist(String hash) {
        return context.containsKey(hash);
    }

    public BroadcastResult get(String hash) {
        return context.get(hash);
    }

    public BroadcastResult remove(String hash) {
        return context.remove(hash);
    }
}
