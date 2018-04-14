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
package io.nuls.consensus.utils;

import io.nuls.core.chain.entity.NulsDigestData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockInfo {
    private long bestHeight ;
    private NulsDigestData bestHash ;
    private Map<Long, NulsDigestData> heightHashMap = new HashMap<>();
    private List<String> nodeIdList;
    private boolean finished = false;


    public NulsDigestData getHash(long height) {
        return heightHashMap.get(height);
    }

    public void putHash(long height, NulsDigestData hash) {
        heightHashMap.put(height, hash);
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public NulsDigestData getBestHash() {
        return bestHash;
    }

    public void setBestHash(NulsDigestData bestHash) {
        this.bestHash = bestHash;
    }

    public List<String> getNodeIdList() {
        return nodeIdList;
    }

    public void setNodeIdList(List<String> nodeIdList) {
        this.nodeIdList = nodeIdList;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void clearHeightHashMap(){
        this.heightHashMap.clear();
    }
}
