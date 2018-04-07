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

import io.nuls.consensus.cache.manager.block.ConfirmingBlockCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.manager.RoundManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.BlockLog;
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

    private ConfirmingBlockCacheManager confirmingBlockCacheManager = ConfirmingBlockCacheManager.getInstance();

    private BlockHeaderChain approvingChain;

    private List<BlockHeaderChain> chainList = new CopyOnWriteArrayList<>();
    private long maxHeight;

    private long tempIndex = 0L;

    private BifurcateProcessor() {
    }

    public static BifurcateProcessor getInstance() {
        return INSTANCE;
    }

    public synchronized boolean addHeader(BlockHeader header) {
        boolean needUpdateBestBlock = false;
        boolean result = add(header);
        if (result) {
            if (header.getHeight() > maxHeight) {
                maxHeight = header.getHeight();
                needUpdateBestBlock = true;
            }
            checkIt();
        }
        return needUpdateBestBlock;
    }

    private void checkIt() {
        int maxSize = 0;
        BlockHeaderChain longestChain = null;
        StringBuilder str = new StringBuilder("++++++++++++++++++++++++chain info:");
        for (BlockHeaderChain chain : chainList) {
            str.append("+++++++++++\nchain:start-" + chain.getHeaderDigestList().get(0).getHeight() + ", end-" + chain.getHeaderDigestList().get(chain.size() - 1).getHeight());
            int listSize = chain.size();
            if (maxSize < listSize) {
                maxSize = listSize;
                longestChain = chain;
            } else if (maxSize == listSize) {
                HeaderDigest hd = chain.getLastHd();
                HeaderDigest hd_long = longestChain.getLastHd();
                if (hd.getTime() < hd_long.getTime()) {
                    longestChain = chain;
                }
            }
        }
        if (tempIndex % 10 == 0) {
            BlockLog.info(str.toString());
            tempIndex++;
        }
        if (this.approvingChain != null || !this.approvingChain.getId().equals(longestChain.getId())) {
            BlockService blockService = NulsContext.getServiceBean(BlockService.class);
            for (int i=approvingChain.size()-1;i>=0;i--) {
                HeaderDigest hd = approvingChain.getHeaderDigestList().get(i);
                try {
                    blockService.rollbackBlock(hd.getHash());
                } catch (NulsException e) {
                    Log.error(e);
                }
            }
            for(int i=0;i<longestChain.getHeaderDigestList().size();i++){
                HeaderDigest hd = longestChain.getHeaderDigestList().get(i);
                blockService.approvalBlock(hd.getHash());
            }
        }
        this.approvingChain = longestChain;
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
                confirmingBlockCacheManager.removeBlock(hash);
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

            int index = chain.indexOf(header.getPreHash().getDigestHex(), header.getHeight() - 1, header.getTime());
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

    public List<String> getAllHashList(long height) {
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

    public String getBlockHash(long height) {
        if (null == approvingChain) {
            return null;
        }
        HeaderDigest headerDigest = approvingChain.getHeaderDigest(height);
        if (null != headerDigest) {
            return headerDigest.getHash();
        }
        return null;
    }

    public boolean processing(long height) {
        if (chainList.isEmpty()) {
            return false;
        }
        this.checkIt();
        if (null == approvingChain) {
            return false;
        }
        if (approvingChain.getLastHd() != null && approvingChain.getLastHd().getHeight() >= (height + PocConsensusConstant.CONFIRM_BLOCK_COUNT)) {
            return true;
        }
        return false;
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

    public BlockHeaderChain getLongestChain() {
        BlockHeaderChain longest = null;
        for (BlockHeaderChain chain : chainList) {
            if (longest == null || chain.size() > longest.size()) {
                longest = chain;
            }
        }
        return longest;
    }

    public void clear() {
        this.chainList.clear();
        this.approvingChain = null;
        this.maxHeight = 0;
    }
}
