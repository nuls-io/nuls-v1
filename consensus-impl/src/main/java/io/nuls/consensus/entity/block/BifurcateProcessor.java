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
package io.nuls.consensus.entity.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2018/1/12
 */
public class BifurcateProcessor {

    private static final BifurcateProcessor INSTANCE = new BifurcateProcessor();

    private List<BlockHeaderChain> chainList = Collections.synchronizedList(new ArrayList<>());

    private long bestHeight;

    private Lock lock = new ReentrantLock();

    private BifurcateProcessor() {
    }

    public static BifurcateProcessor getInstance() {
        return INSTANCE;
    }

    public synchronized boolean addHeader(BlockHeader header) {
        lock.lock();
        try {
            boolean result = add(header);
            if (result) {
                checkIt();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    private void checkIt() {
        List<BlockHeaderChain> chainList1 = new ArrayList<>(this.chainList);
        int size = 0;
        for (BlockHeaderChain chain : chainList1) {
            int listSize = chain.size();
            if (size < listSize) {
                size = listSize;
            }
        }
        for (BlockHeaderChain chain : chainList1) {
            if (chain.size() < (size - 6)) {
                this.chainList.remove(chain);
            }
        }
    }

    private boolean add(BlockHeader header) {
        for (int i = 0; i < this.chainList.size(); i++) {
            BlockHeaderChain chain = chainList.get(i);
            if (chain.contains(header)) {
                return false;
            }
        }
        for (int i = 0; i < this.chainList.size(); i++) {
            BlockHeaderChain chain = chainList.get(i);
            int index = chain.indexOf(header.getPreHash().getDigestHex(), header.getHeight() - 1);
            if (index == chain.size() - 1) {
                chain.addHeader(header);
                setBestHeight(header);
                return true;
            } else if (index >= 0) {
                BlockHeaderChain newChain = chain.getBifurcateChain(header);
                chainList.add(newChain);
                return true;
            }
        }
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        if ((bestBlock.getHeader().getHeight() + 1) != header.getHeight()
                || !bestBlock.getHeader().getHash().getDigestHex().equals(header.getPreHash().getDigestHex())) {
            return false;
        }
        BlockHeaderChain chain = new BlockHeaderChain();
        chain.addHeader(header);
        chainList.add(chain);
        setBestHeight(header);
        return true;
    }

    private void setBestHeight(BlockHeader header) {
        if (header.getHeight() <= bestHeight) {
            return;
        }
        bestHeight = header.getHeight();
    }

    public BlockHeaderChain getLongestChain() {
        List<BlockHeaderChain> longestChainList = new ArrayList<>();
        List<BlockHeaderChain> list = new ArrayList<>(chainList);
        for (BlockHeaderChain chain : list) {
            if (longestChainList.isEmpty() || chain.size() > longestChainList.get(0).size()) {
                longestChainList.clear();
                longestChainList.add(chain);
            } else if (longestChainList.isEmpty() || chain.size() == longestChainList.get(0).size()) {
                longestChainList.add(chain);
            }
        }
        if (longestChainList.size() > 1 || longestChainList.isEmpty()) {
            return new BlockHeaderChain();
        }
        return longestChainList.get(0);
    }


    public long getBestHeight() {
        if (bestHeight == 0 && null != NulsContext.getInstance().getBestBlock()) {
            bestHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
        }
        return bestHeight;
    }

    public void removeHash(String hash) {
        lock.lock();
        try {
            if (chainList.isEmpty()) {
                return;
            }
            List<BlockHeaderChain> tempList = new ArrayList<>(this.chainList);
            tempList.forEach((BlockHeaderChain chain) -> removeBlock(chain, hash));
        } finally {
            lock.unlock();
        }
    }

    private void removeBlock(BlockHeaderChain chain, String hashHex) {
        HeaderDigest hd = chain.getHeaderDigest(hashHex);
        if (hd == null) {
            return;
        }
        chain.removeHeaderDigest(hashHex);
        if (chain.size() == 0) {
            this.chainList.remove(chain);
        }
    }

    public List<String> getHashList(long height) {
        Set<String> set = new HashSet<>();
        List<BlockHeaderChain> chainList1 = new ArrayList<>(this.chainList);
        for (BlockHeaderChain chain : chainList1) {
            HeaderDigest headerDigest = chain.getHeaderDigest(height);
            if (null != headerDigest) {
                set.add(headerDigest.getHash());
            }
        }
        return new ArrayList<>(set);
    }

    public boolean processing(long height) {
        if (chainList.isEmpty()) {
            return false;
        }
        lock.lock();
        try {
            List<String> hashList = this.getHashList(height);
            if (hashList.size() == 1) {
                return true;
            }
            if (hashList.isEmpty()) {
                Log.warn("lost a block:" + height);
                return false;
            }
            List<BlockHeaderChain> longestChainList = new ArrayList<>();
            int size = 0;
            for (BlockHeaderChain chain : chainList) {
                if (chain.size() == size) {
                    longestChainList.add(chain);
                } else if (chain.size() > size && size == 0) {
                    longestChainList.add(chain);
                } else if (chain.size() > size && size != 0) {
                    longestChainList.clear();
                    longestChainList.add(chain);
                }
            }
            if (longestChainList.size() != 1) {
                return false;
            }
            chainList = longestChainList;
            return true;
        } finally {
            lock.unlock();
        }
    }
}
