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

import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 */
public class CollectThread implements Runnable {

    private static final CollectThread INSTANCE = new CollectThread();

    private long endHeight;
    private long startHeight;
    private Map<Long, Block> map = new HashMap<>();
    private RequestThread requestThread;

    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);


    public static final CollectThread getInstance() {
        return INSTANCE;
    }

    public static final CollectThread initInstance(long startHeight, long endHeight, RequestThread requestThread) {
        INSTANCE.setStartHeight(startHeight);
        INSTANCE.setEndHeight(endHeight);
        INSTANCE.setRequestThread(requestThread);
        return INSTANCE;
    }

    private CollectThread() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (startHeight > endHeight) {
                    break;
                }
                pushBlock();
            } catch (Exception e) {
                Log.error(e);
            }
        }
        this.init();
    }

    private void init() {
        this.endHeight = 0;
        this.startHeight = 0;
        this.map.clear();
        this.requestThread = null;
    }

    private void pushBlock() throws InterruptedException {
        Block block = map.get(startHeight);
        if (null == block) {
            block = waitBlock(startHeight);
        }
        Result result = consensusService.addBlock(block);
        if (result.isSuccess()) {
            map.remove(block.getHeader().getHeight());
            startHeight++;
        }
    }

    private Block waitBlock(long height) throws InterruptedException {
        Block block = null;
        long totalWait = 0;
        while (null == block && height == startHeight) {
            Thread.sleep(10L);
            totalWait += 10;
            if (totalWait > 5000) {
                this.requestThread.retryDownload(height, getRequestSize());
                totalWait = 0;
            }
            block = map.get(startHeight);
        }
        return block;
    }

    private int getRequestSize() {
        for (int i = 1; i <= 10; i++) {
            Block block = map.get(startHeight + i);
            if (null != block) {
                return i;
            }
        }
        return 10;
    }

    public boolean addBlock(Block block) {
        long height = block.getHeader().getHeight();
        if (height < startHeight || height > endHeight) {
            return false;
        }
        map.put(height, block);
        return true;
    }

    protected void setEndHeight(long endHeight) {
        this.endHeight = endHeight;
    }

    protected void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    protected void setRequestThread(RequestThread requestThread) {
        this.requestThread = requestThread;
    }
}
