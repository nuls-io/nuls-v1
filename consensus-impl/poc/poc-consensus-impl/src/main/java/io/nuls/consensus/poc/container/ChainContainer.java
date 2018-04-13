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

package io.nuls.consensus.poc.container;

import io.nuls.consensus.poc.entity.Chain;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.core.chain.entity.Block;

import java.util.List;

/**
 * Created by ln on 2018/4/13.
 */
public class ChainContainer {

    private Chain chain;

    private List<MeetingRound> roundList;

    public boolean addBlock(Block block) {
        //TODO
        return true;
    }

    public boolean verifyBlock(Block block) {
        return verifyBlock(block, false);
    }


    public boolean verifyBlock(Block block, boolean isDownload) {
        //TODO
        return true;
    }

    public boolean verifyAndAddBlock(Block block, boolean isDownload) {
        boolean success = verifyBlock(block, isDownload);
        if(success) {
            success = addBlock(block);
        }
        return success;
    }

    public boolean removeBlock(Block block) {
        //TODO
        return true;
    }

    public ChainContainer getBeforeTheForkChain(ChainContainer chainContainer) {
        //TODO
        return chainContainer;
    }

    public ChainContainer getAfterTheForkChain(ChainContainer chainContainer) {
        //TODO
        return chainContainer;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof  ChainContainer)) {
            return false;
        }
        ChainContainer other = (ChainContainer) obj;
        if(other.getChain() == null || this.chain == null) {
            return false;
        }
        return other.getChain().getId().equals(this.chain.getId());
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public List<MeetingRound> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<MeetingRound> roundList) {
        this.roundList = roundList;
    }
}
