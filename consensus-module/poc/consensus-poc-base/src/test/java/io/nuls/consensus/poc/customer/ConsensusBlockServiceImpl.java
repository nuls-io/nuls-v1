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

package io.nuls.consensus.poc.customer;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.network.model.Node;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.service.BlockService;

import java.util.ArrayList;

/**
 * Created by ln on 2018/5/8.
 */
public class ConsensusBlockServiceImpl implements BlockService {
    @Override
    public Result<Block> getGengsisBlock() {
        return null;
    }

    @Override
    public Result<Block> getBestBlock() {
        return null;
    }

    @Override
    public Result<BlockHeader> getBestBlockHeader() {
        return null;
    }

    @Override
    public Result<BlockHeader> getBlockHeader(long height) {
        return null;
    }

    @Override
    public Result<BlockHeader> getBlockHeader(NulsDigestData hash) {
        return null;
    }

    @Override
    public Result<Block> getBlock(NulsDigestData hash) {
        Result result = new Result(true, null);

        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(hash);
        block.setHeader(blockHeader);
        block.setTxs(new ArrayList<>());

        result.setData(block);

        return result;
    }

    @Override
    public Result<Block> getBlock(long height) {
        return null;
    }

    @Override
    public Result saveBlock(Block block) throws NulsException {
        return new Result(true, null);
    }

    @Override
    public Result rollbackBlock(Block block) throws NulsException {
        return new Result(false, null);
    }

    @Override
    public Result forwardBlock(SmallBlock block, Node excludeNode) {
        return null;
    }

    @Override
    public Result broadcastBlock(SmallBlock block) {
        return null;
    }

}