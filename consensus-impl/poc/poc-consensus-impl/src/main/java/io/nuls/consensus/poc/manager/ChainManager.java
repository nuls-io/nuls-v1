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
 */

package io.nuls.consensus.poc.manager;

import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainManager {

    private ChainContainer masterChain;
    private List<ChainContainer> chains;
    private List<ChainContainer> isolatedChains;

    private RoundManager roundManager;

    public ChainManager() {
        chains = new ArrayList<>();
        isolatedChains = new ArrayList<>();
    }

    public void newIsolatedChain(Block block) {
        BlockHeader header = block.getHeader();

        Chain isolatedChain = new Chain();
        isolatedChain.setStartBlockHeader(header);
        isolatedChain.setEndBlockHeader(header);
        isolatedChain.getBlockHeaderList().add(header);
        isolatedChain.getBlockList().add(block);

        ChainContainer isolatedChainContainer = new ChainContainer(isolatedChain);
        isolatedChains.add(isolatedChainContainer);
    }

    public boolean checkIsBeforeIsolatedChainAndAdd(Block block) {
        BlockHeader header = block.getHeader();

        boolean success = false;
        for(ChainContainer chainContainer : isolatedChains) {
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

    public boolean checkIsAfterIsolatedChainAndAdd(Block block) {
        BlockHeader header = block.getHeader();

        for(ChainContainer chainContainer : isolatedChains) {
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
            return 0l;
        }
        return masterChain.getChain().getEndBlockHeader().getHeight();
    }

    public void clear() {
        masterChain = null;
        chains.clear();
        isolatedChains.clear();
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

    public void setChains(List<ChainContainer> chains) {
        this.chains = chains;
    }

    public RoundManager getRoundManager() {
        return roundManager;
    }

    public List<ChainContainer> getIsolatedChains() {
        return isolatedChains;
    }

    public void setRoundManager(RoundManager roundManager) {
        this.roundManager = roundManager;
    }

    public Block getBestBlock() {
        return masterChain.getBestBlock();
    }
}
