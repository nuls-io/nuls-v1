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

package io.nuls.protocol.storage.po;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.NulsDigestData;

import io.nuls.kernel.script.BlockSignature;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 区块头实体单元测试工具类
 * Block header model unit test tool class.
 *
 * @author: Niels Wang
 */
public class BlockHeaderPoTest {

    /**
     * 验证区块头实体的序列化和反序列化的正确性
     * Verify the correctness of serialization and deserialization of block header entities.
     */
    @Test
    public void serializeAndParse() {
        BlockHeaderPo po = new BlockHeaderPo();
        po.setHeight(1286L);
        po.setExtend("extends".getBytes());
        po.setMerkleHash(NulsDigestData.calcDigestData("merkleHash".getBytes()));
        try {
            po.setPackingAddress("address".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        po.setScriptSign(new BlockSignature());
        po.setTime(12345678901L);
        po.setTxCount(3);
        List<NulsDigestData> txHashList = new ArrayList<>();
        txHashList.add(NulsDigestData.calcDigestData("first-tx-hash".getBytes()));
        txHashList.add(NulsDigestData.calcDigestData("second-tx-hash".getBytes()));
        txHashList.add(NulsDigestData.calcDigestData("third-tx-hash".getBytes()));
        po.setTxHashList(txHashList);

        byte[] bytes = new byte[0];
        try {
            bytes = po.serialize();
        } catch (IOException e) {
            Log.error(e);
        }

        BlockHeaderPo newPo = new BlockHeaderPo();
        try {
            newPo.parse(bytes,0);
        } catch (NulsException e) {
            Log.error(e);
        }
        assertNull(newPo.getHash());
        assertEquals(po.getHeight(), newPo.getHeight());
        assertEquals(po.getPreHash(), newPo.getPreHash());
        assertEquals(po.getMerkleHash(), newPo.getMerkleHash());
        assertTrue(Arrays.equals(po.getExtend(), newPo.getExtend()));
        assertTrue(Arrays.equals(po.getPackingAddress(), newPo.getPackingAddress()));
        assertEquals(po.getScriptSign().getPublicKey(), newPo.getScriptSign().getPublicKey());
        assertEquals(po.getScriptSign().getSignData(), newPo.getScriptSign().getSignData());
        assertEquals(po.getTime(), newPo.getTime());
        assertEquals(po.getTxCount(), newPo.getTxCount());
        assertEquals(po.getTxHashList().get(0), newPo.getTxHashList().get(0));
        assertEquals(po.getTxHashList().get(1), newPo.getTxHashList().get(1));
        assertEquals(po.getTxHashList().get(2), newPo.getTxHashList().get(2));
    }

}