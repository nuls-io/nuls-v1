/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Niels
 * @date 2018/1/12
 */
public class BifurcateProcessor {

    private static final BifurcateProcessor INSTANCE = new BifurcateProcessor();

    private List<BlockHeaderChain> chainList = new CopyOnWriteArrayList<>();

    private long bestHeight;

    private BifurcateProcessor() {
    }

    public static BifurcateProcessor getInstance() {
        return INSTANCE;
    }

    public synchronized boolean addHeader(BlockHeader header) {
        boolean result = add(header);
        if (result) {
            checkIt();
        }
        return result;
    }

    private void checkIt() {
        int size = 0;
        for (BlockHeaderChain chain : chainList) {
            int listSize = chain.size();
            if (size < listSize) {
                size = listSize;
            }
        }
        for (int i = chainList.size() - 1; i >= 0; i--) {
            BlockHeaderChain chain = chainList.get(i);
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
        if (chainList.isEmpty()) {
            return;
        }
        List<BlockHeaderChain> tempList = new ArrayList<>(this.chainList);
        tempList.forEach((BlockHeaderChain chain) -> removeBlock(chain, hash));
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
        List<String> hashList = this.getHashList(height);
        if (hashList.size() == 1) {
            return true;
        }
        if (hashList.isEmpty()) {
            Log.warn("lost a block:" + height);
            return false;
        }
        int maxSize = 0;
        int secondMaxSize = 0;
        for (BlockHeaderChain chain : chainList) {
            int size = chain.size();
            if (size > maxSize) {
                secondMaxSize = maxSize;
                maxSize = size;
            } else if (size > secondMaxSize) {
                secondMaxSize = size;
            } else if (size == maxSize) {
                secondMaxSize = size;
            }
        }
        if (maxSize <= (secondMaxSize + 6)) {
            return false;
        }
        for (int i = chainList.size() - 1; i >= 0; i--) {
            if (chainList.size() < maxSize) {
                chainList.remove(i);
            }
        }
        return true;
    }

    public int getHashSize() {
        Set<String> hashSet = new HashSet<>();
        for (BlockHeaderChain chain : chainList) {
            hashSet.addAll(chain.getHashSet());
        }
        return hashSet.size();
    }
}
