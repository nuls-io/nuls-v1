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

package io.nuls.protocol.base.utils;

import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.protocol.storage.po.BlockHeaderPo;

/**
 * @author: Niels Wang
 */
public class PoConvertUtil {


    public static BlockHeader fromBlockHeaderPo(BlockHeaderPo po) {
        BlockHeader header = new BlockHeader();
        header.setHash(po.getHash());
        header.setHeight(po.getHeight());
        header.setExtend(po.getExtend());
        header.setPreHash(po.getPreHash());
        header.setTime(po.getTime());
        header.setMerkleHash(po.getMerkleHash());
        header.setTxCount(po.getTxCount());
        header.setScriptSig(po.getScriptSign());
        return header;
    }


    public static BlockHeaderPo toBlockHeaderPo(Block block) {
        BlockHeaderPo po = new BlockHeaderPo();
        po.setHash(block.getHeader().getHash());
        po.setPreHash(block.getHeader().getPreHash());
        po.setMerkleHash(block.getHeader().getMerkleHash());
        po.setTime(block.getHeader().getTime());
        po.setHeight(block.getHeader().getHeight());
        po.setTxCount(block.getHeader().getTxCount());
        po.setPackingAddress(block.getHeader().getPackingAddress());
        po.setScriptSign(block.getHeader().getScriptSig());
        po.setExtend(block.getHeader().getExtend());
        po.setTxHashList(block.getTxHashList());
        return po;
    }
}
