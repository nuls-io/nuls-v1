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

package io.nuls.protocol.base.download.thread;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.download.entity.ResultMessage;
import io.nuls.protocol.base.download.utils.DownloadUtils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadThread implements Callable<ResultMessage> {

    private NulsDigestData startHash;
    private NulsDigestData endHash;
    private long startHeight;
    private int size;
    private Node node;

    public DownloadThread(NulsDigestData startHash, NulsDigestData endHash, long startHeight, int size, Node node) {
        this.startHash = startHash;
        this.endHash = endHash;
        this.startHeight = startHeight;
        this.size = size;
        this.node = node;
    }

    @Override
    public ResultMessage call() throws Exception {
        List<Block> blockList = null;
        try {
            Log.info("download thread : " + Thread.currentThread().getName() + " ,  startHeight : " + startHeight + ", size : " + size + " , from node : " + node.getId() + " , startHash : " + startHash + " , endHash : " + endHash);
            blockList = DownloadUtils.getBlocks(node, startHash, endHash, startHeight, size);
            Log.info("download complete thread : " + Thread.currentThread().getName() + " ,  startHeight : " + startHeight + ", size : " + size + " , from node : " + node.getId() + " , get data size : " + (blockList == null ? 0 : blockList.size()));
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return new ResultMessage(startHash, endHash, startHeight, size, node, blockList);
    }
}
