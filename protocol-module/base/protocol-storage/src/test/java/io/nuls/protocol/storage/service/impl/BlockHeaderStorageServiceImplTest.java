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

package io.nuls.protocol.storage.service.impl;

import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.protocol.storage.po.BlockHeaderPo;
import io.nuls.protocol.storage.service.BlockHeaderStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 */
public class BlockHeaderStorageServiceImplTest {

    private BlockHeaderStorageService service;

    private BlockHeaderPo entity;

    @Before
    public void init() {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
        bootstrap.init();
        bootstrap.start();

        service = NulsContext.getServiceBean(BlockHeaderStorageService.class);
        BlockHeaderPo po = new BlockHeaderPo();
        po.setHash(NulsDigestData.calcDigestData("hashhash".getBytes()));
        po.setHeight(1286L);
        po.setExtend("extends".getBytes());
        po.setMerkleHash(NulsDigestData.calcDigestData("merkleHash".getBytes()));
        po.setPreHash(NulsDigestData.calcDigestData("prehash".getBytes()));
        try {
            po.setPackingAddress("address".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        po.setScriptSign(new P2PKHScriptSig());
        po.setTime(12345678901L);
        po.setTxCount(3);
        List<NulsDigestData> txHashList = new ArrayList<>();
        txHashList.add(NulsDigestData.calcDigestData("first-tx-hash".getBytes()));
        txHashList.add(NulsDigestData.calcDigestData("second-tx-hash".getBytes()));
        txHashList.add(NulsDigestData.calcDigestData("third-tx-hash".getBytes()));
        po.setTxHashList(txHashList);
        this.entity = po;
    }

    @Test
    public void test() {
        assertNotNull(service);
        this.saveBlockHeader();

        this.getBlockPo();

        this.getBlockPo1();

        this.removeBlockHerader();
    }

    public void getBlockPo() {
        BlockHeaderPo po = this.service.getBlockHeaderPo(entity.getHeight());
        this.testEquals(po, entity);
    }

    public void getBlockPo1() {
        BlockHeaderPo po = this.service.getBlockHeaderPo(entity.getHash());
        this.testEquals(po, entity);
    }

    public void saveBlockHeader() {
        Result result = service.saveBlockHeader(entity);
        assertTrue(result.isSuccess());
    }

    public void removeBlockHerader() {
        service.removeBlockHerader(entity);
        BlockHeaderPo po = this.service.getBlockHeaderPo(entity.getHash());
        assertNull(po);
    }


    private void testEquals(BlockHeaderPo po, BlockHeaderPo entity) {
        assertEquals(po.getHash(),entity.getHash());
        assertEquals(po.getHeight(), entity.getHeight());
        assertEquals(po.getPreHash(), entity.getPreHash());
        assertEquals(po.getMerkleHash(), entity.getMerkleHash());
        assertTrue(Arrays.equals(po.getExtend(), entity.getExtend()));
        assertTrue(Arrays.equals(po.getPackingAddress(), entity.getPackingAddress()));
        assertEquals(po.getScriptSign().getPublicKey(), entity.getScriptSign().getPublicKey());
        assertEquals(po.getScriptSign().getSignData(), entity.getScriptSign().getSignData());
        assertEquals(po.getTime(), entity.getTime());
        assertEquals(po.getTxCount(), entity.getTxCount());
        assertEquals(po.getTxHashList().get(0), entity.getTxHashList().get(0));
        assertEquals(po.getTxHashList().get(1), entity.getTxHashList().get(1));
        assertEquals(po.getTxHashList().get(2), entity.getTxHashList().get(2));
    }

}