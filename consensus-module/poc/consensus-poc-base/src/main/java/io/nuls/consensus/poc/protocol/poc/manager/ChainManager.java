/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.protocol.poc.manager;

import io.nuls.consensus.poc.protocol.poc.container.ChainContainer;
import io.nuls.consensus.poc.protocol.poc.model.Chain;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainManager {

    private ChainContainer masterChain;
    private List<ChainContainer> chains;
    private List<ChainContainer> orphanChains;

    public ChainManager() {
        chains = new ArrayList<>();
        orphanChains = new ArrayList<>();
    }

    public void newOrphanChain(Block block) {
        BlockHeader header = block.getHeader();

        Chain orphanChain = new Chain();
        orphanChain.setStartBlockHeader(header);
        orphanChain.setEndBlockHeader(header);
        orphanChain.getBlockHeaderList().add(header);
        orphanChain.getBlockList().add(block);

        ChainContainer orphanChainContainer = new ChainContainer(orphanChain);
        orphanChains.add(orphanChainContainer);
    }

    public boolean checkIsBeforeOrphanChainAndAdd(Block block) {
        BlockHeader header = block.getHeader();

        boolean success = false;
        for(ChainContainer chainContainer : orphanChains) {
            Chain chain = chainContainer.getChain();
            if(header.getHash().equals(chain.getStartBlockHeader().getPreHash())) {
                success = true;
                chain.setStartBlockHeader(header);
                chain.getBlockHeaderList().add(0, header);
                chain.getBlockList().add(0, block);
            }
        }
        return success;
    }

    public boolean checkIsAfterOrphanChainAndAdd(Block block) {
        BlockHeader header = block.getHeader();

        for(ChainContainer chainContainer : orphanChains) {
            Chain chain = chainContainer.getChain();
            if(header.getPreHash().equals(chain.getEndBlockHeader().getHash())) {
                chain.setEndBlockHeader(header);
                chain.getBlockHeaderList().add(header);
                chain.getBlockList().add(block);
                return true;
            }
        }
        return false;
    }

    public long getBestBlockHeight() {
        if(masterChain == null || masterChain.getChain() == null || masterChain.getChain().getEndBlockHeader() == null) {
            return 0L;
        }
        return masterChain.getChain().getEndBlockHeader().getHeight();
    }

    public void clear() {
        masterChain = null;
        chains.clear();
        orphanChains.clear();
    }

    public ChainContainer getMasterChain() {
        return masterChain;
    }

    public void setMasterChain(ChainContainer masterChain) {
        this.masterChain = masterChain;
    }

    public List<ChainContainer> getChains() {
        return chains;
    }

    public Block getBestBlock() {
        return masterChain.getBestBlock();
    }

    public List<ChainContainer> getOrphanChains() {
        return orphanChains;
    }
}
