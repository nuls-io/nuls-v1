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
 *
 */

package io.nuls.protocol.base.service;

import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.ledger.module.impl.UtxoLedgerModuleBootstrap;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.storage.po.BlockHeaderPo;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author: Niels Wang
 */
public class BlockServiceImplTest {

    private BlockService service;

    @Before
    public void init() {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
        bootstrap.init();
        bootstrap.start();

        UtxoLedgerModuleBootstrap ledgerModuleBootstrap = new UtxoLedgerModuleBootstrap();
        ledgerModuleBootstrap.init();
        ledgerModuleBootstrap.start();


        service = NulsContext.getServiceBean(BlockService.class);
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(NulsDigestData.calcDigestData("hashhash".getBytes()));
        blockHeader.setHeight(1286L);
        blockHeader.setExtend("extends".getBytes());
        blockHeader.setMerkleHash(NulsDigestData.calcDigestData("merkleHash".getBytes()));
        blockHeader.setPreHash(NulsDigestData.calcDigestData("prehash".getBytes()));
        try {
            blockHeader.setPackingAddress("address".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        blockHeader.setScriptSig(new P2PKHScriptSig());
        blockHeader.setTime(12345678901L);
        blockHeader.setTxCount(3);
        List<NulsDigestData> txHashList = new ArrayList<>();
        txHashList.add(NulsDigestData.calcDigestData("first-tx-hash".getBytes()));
        txHashList.add(NulsDigestData.calcDigestData("second-tx-hash".getBytes()));
        txHashList.add(NulsDigestData.calcDigestData("third-tx-hash".getBytes()));
//        block.setTxHashList(txHashList);
//        this.model = blockHeader;
    }

    @Test
    public void test() {

    }
//
//    @Test
//    public void getGengsisBlock() {
//    }
//
//    @Test
//    public void getBestBlock() {
//    }
//
//    @Test
//    public void getBestBlockHeader() {
//    }
//
//    @Test
//    public void getBlockHeader() {
//    }
//
//    @Test
//    public void getBlockHeader1() {
//    }
//
//    @Test
//    public void getBlock() {
//    }
//
//    @Test
//    public void getBlock1() {
//    }
//
//    @Test
//    public void saveBlock() {
//    }
//
//    @Test
//    public void rollbackBlock() {
//    }
//
//    @Test
//    public void forwardBlock() {
//    }
//
//    @Test
//    public void broadcastBlock() {
//    }
}