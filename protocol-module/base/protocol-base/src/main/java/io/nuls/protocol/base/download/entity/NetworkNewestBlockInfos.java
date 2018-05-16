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

package io.nuls.protocol.base.download.entity;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.model.Node;

import java.util.List;

/**
 * Created by ln on 2018/4/8.
 */
public class NetworkNewestBlockInfos {

    private long netBestHeight;
    private NulsDigestData netBestHash;
    private long localBestHeight;
    private String localBestHash;

    private List<Node> nodes;

    public NetworkNewestBlockInfos() {
    }

    public NetworkNewestBlockInfos(long netBestHeight, NulsDigestData netBestHash, List<Node> nodes) {
        this.netBestHeight = netBestHeight;
        this.netBestHash = netBestHash;
        this.nodes = nodes;
    }

    public void setNetBestHeight(long netBestHeight) {
        this.netBestHeight = netBestHeight;
    }

    public void setNetBestHash(NulsDigestData netBestHash) {
        this.netBestHash = netBestHash;
    }

    public void setLocalBestHeight(long localBestHeight) {
        this.localBestHeight = localBestHeight;
    }

    public void setLocalBestHash(String localBestHash) {
        this.localBestHash = localBestHash;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public long getNetBestHeight() {
        return netBestHeight;
    }

    public NulsDigestData getNetBestHash() {
        return netBestHash;
    }

    public long getLocalBestHeight() {
        return localBestHeight;
    }

    public String getLocalBestHash() {
        return localBestHash;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "NetworkNewestBlockInfos{" +
                "netBestHeight=" + netBestHeight +
                ", netBestHash='" + netBestHash + '\'' +
                ", localBestHeight=" + localBestHeight +
                ", localBestHash='" + localBestHash + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
