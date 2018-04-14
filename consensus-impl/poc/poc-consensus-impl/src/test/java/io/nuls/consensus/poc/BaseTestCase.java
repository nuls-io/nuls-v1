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

package io.nuls.consensus.poc;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.member.Deposit;
import io.nuls.protocol.entity.Consensus;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ln on 2018/4/14.
 */
public class BaseTestCase {

    protected List<Block> blockList = new ArrayList<>();
    protected List<BlockHeader> blockHeaderList = new ArrayList<>();
    protected List<Consensus<Agent>> agentList = new ArrayList<>();
    protected List<Consensus<Deposit>> depositList = new ArrayList<>();

    @Before
    public void initDatas() {
        BlockHeader header = new BlockHeader();
        header.setPreHash(NulsDigestData.fromDigestHex("00000000000000000000000000000"));
        header.setHeight(1l);
        header.setTxCount(1);
        header.setTime(1523709576806l);

        Block block = new Block();
        block.setHeader(header);

        blockList.add(block);
        blockHeaderList.add(header);
    }
}
