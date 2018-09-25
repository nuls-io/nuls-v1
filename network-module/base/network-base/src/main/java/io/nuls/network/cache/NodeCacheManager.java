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
package io.nuls.network.cache;

import io.nuls.cache.CacheMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.protocol.message.P2PNodeBody;

import java.util.HashSet;
import java.util.Set;

public class NodeCacheManager {

    private static NodeCacheManager instance = new NodeCacheManager();

    private NodeCacheManager() {

    }

    public static NodeCacheManager getInstance() {
        return instance;
    }

    //将收到的广播节点的信息缓存5分钟，避免重复广播
    private CacheMap<String, P2PNodeBody> cacheNodeMap = new CacheMap<>(NetworkConstant.CACHE_P2P_NODE, 8, String.class, P2PNodeBody.class, 60 * 5, 0, null);

    private CacheMap<String, Set<String>> cacheIpMap = new CacheMap<>(NetworkConstant.CACHE_P2P_IP, 2, String.class, HashSet.class, 60, 0, null);

    public void cacheNode(P2PNodeBody body) {
        cacheNodeMap.put(body.getId(), body);
    }

    public P2PNodeBody getNode(String id) {
        return cacheNodeMap.get(id);
    }

    public void cacheIpSet(Set ipSet) {
        cacheIpMap.put("ipSet", ipSet);
    }

    public Set<String> getIpSet() {
        return cacheIpMap.get("ipSet");
    }
}
