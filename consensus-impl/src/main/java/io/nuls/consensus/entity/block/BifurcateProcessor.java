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

import io.nuls.consensus.cache.manager.block.ConfrimingBlockCacheManager;
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

    private ConfrimingBlockCacheManager confrimingBlockCacheManager = ConfrimingBlockCacheManager.getInstance();

    private List<BlockHeaderChain> chainList = new CopyOnWriteArrayList<>();
    private long maxHeight;

    private BifurcateProcessor() {
    }

    public static BifurcateProcessor getInstance() {
        return INSTANCE;
    }

    public synchronized void addHeader(BlockHeader header) {
        boolean result = add(header);
        if (result) {
            if (header.getHeight() > maxHeight) {
                maxHeight = header.getHeight();
            }
            checkIt();
        }
    }

    private void checkIt() {
        int maxSize = 0;
        for (BlockHeaderChain chain : chainList) {
            int listSize = chain.size();
            if (maxSize < listSize) {
                maxSize = listSize;
            }
        }
        Set<String> rightHashSet = new HashSet<>();
        Set<String> removeHashSet = new HashSet<>();
        for (int i = chainList.size() - 1; i >= 0; i--) {
            BlockHeaderChain chain = chainList.get(i);
            if (chain.size() < (maxSize - 6)) {
                removeHashSet.addAll(chain.getHashSet());
                this.chainList.remove(chain);
            } else {
                rightHashSet.addAll(chain.getHashSet());
            }
        }

        for (String hash : removeHashSet) {
            if (!rightHashSet.contains(hash)) {
                confrimingBlockCacheManager.removeBlock(hash);
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
                return true;
            } else if (index >= 0) {
                BlockHeaderChain newChain = chain.getBifurcateChain(header);
                chainList.add(newChain);
                return true;
            }
        }
        BlockHeaderChain chain = new BlockHeaderChain();
        chain.addHeader(header);
        chainList.add(chain);
        return true;
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
        if (hashList.isEmpty()) {
            //Log.warn("lost a block:" + height);
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

        Set<String> rightHashSet = new HashSet<>();
        Set<String> removeHashSet = new HashSet<>();
        for (int i = chainList.size() - 1; i >= 0; i--) {
            BlockHeaderChain chain = chainList.get(i);
            if (chain.size() < maxSize) {
                removeHashSet.addAll(chain.getHashSet());
                chainList.remove(i);
            } else {
                rightHashSet.addAll(chain.getHashSet());
            }
        }

        for (String hash : removeHashSet) {
            if (!rightHashSet.contains(hash)) {
                confrimingBlockCacheManager.removeBlock(hash);
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

    public int getChainSize() {
        return chainList.size();
    }

    public long getMaxHeight() {
        return maxHeight;
    }
}
