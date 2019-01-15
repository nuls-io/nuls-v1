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

package io.nuls.protocol.base.download.entity;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.model.Node;
import io.nuls.kernel.model.Block;

import java.util.List;

/**
 * Created by ln on 2018/4/8.
 */
public class ResultMessage {
    private NulsDigestData startHash;
    private NulsDigestData endHash;
    private long startHeight;
    private int size;
    private Node node;

    private List<Block> blockList;

    public ResultMessage(NulsDigestData startHash, NulsDigestData endHash, long startHeight, int size, Node node, List<Block> blockList) {
        this.startHash = startHash;
        this.endHash = endHash;
        this.startHeight = startHeight;
        this.size = size;
        this.node = node;
        this.blockList = blockList;
    }

    public NulsDigestData getStartHash() {
        return startHash;
    }

    public NulsDigestData getEndHash() {
        return endHash;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public int getSize() {
        return size;
    }

    public Node getNode() {
        return node;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public void setStartHash(NulsDigestData startHash) {
        this.startHash = startHash;
    }

    public void setEndHash(NulsDigestData endHash) {
        this.endHash = endHash;
    }

    public void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }
}
