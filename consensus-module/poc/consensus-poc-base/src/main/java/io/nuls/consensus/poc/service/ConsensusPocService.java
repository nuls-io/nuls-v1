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

package io.nuls.consensus.poc.service;

import io.nuls.consensus.service.ConsensusServiceIntf;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.network.entity.Node;

import java.util.List;

/**
 * Created by ln on 2018/5/5.
 */
public class ConsensusPocService implements ConsensusServiceIntf {
    @Override
    public boolean newTx(Transaction<? extends BaseNulsData> tx) {
        return false;
    }

    @Override
    public boolean newBlock(Block block) {
        return false;
    }

    @Override
    public boolean newBlock(Block block, Node node) {
        return false;
    }

    @Override
    public boolean addBlock(Block block) {
        return false;
    }

    @Override
    public boolean rollbackBlock(Block block) throws NulsException {
        return false;
    }

    @Override
    public Transaction getAndRemoveOfMemoryTxs(NulsDigestData hash) {
        return null;
    }

    @Override
    public Transaction getTxFromMemory(NulsDigestData hash) {
        return null;
    }

    @Override
    public List<BaseNulsData> getMemoryTxs() {
        return null;
    }

    @Override
    public boolean reset() {
        return false;
    }
}