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

package io.nuls.consensus.poc;

import io.nuls.consensus.poc.customer.ConsensusBlockServiceImpl;
import io.nuls.consensus.poc.customer.ConsensusDownloadServiceImpl;
import io.nuls.consensus.poc.customer.ConsensusNetworkService;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.DownloadService;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ln on 2018/5/8.
 */
public class BaseTest {

    protected static BlockService blockService;
    protected ECKey ecKey = new ECKey();

    @BeforeClass
    public static void initClass() {
        try {
            try {
                blockService = SpringLiteContext.getBean(BlockService.class);
            } catch (Exception e) {
                SpringLiteContext.putBean(ConsensusBlockServiceImpl.class, false);
            }
            try {
                SpringLiteContext.getBean(DownloadService.class);
            } catch (Exception e) {
                SpringLiteContext.putBean(ConsensusDownloadServiceImpl.class, false);
            }
            try {
                SpringLiteContext.getBean(NetworkService.class);
            } catch (Exception e) {
                SpringLiteContext.putBean(ConsensusNetworkService.class, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Block createBlock() {
        // new a block header
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(0);
        blockHeader.setPreHash(NulsDigestData.calcDigestData("00000000000".getBytes()));
        blockHeader.setTime(1L);
        blockHeader.setTxCount(1);
        blockHeader.setMerkleHash(NulsDigestData.calcDigestData(new byte[20]));

        // add a round data
        BlockRoundData roundData = new BlockRoundData();
        roundData.setConsensusMemberCount(1);
        roundData.setPackingIndexOfRound(1);
        roundData.setRoundIndex(1);
        roundData.setRoundStartTime(1L);
        try {
            blockHeader.setExtend(roundData.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }

        // new a block of height 0
        Block block = new Block();
        block.setHeader(blockHeader);

        List<Transaction> txs = new ArrayList<>();
        block.setTxs(txs);

        Transaction tx = new TestTransaction();
        txs.add(tx);

        List<NulsDigestData> txHashList = block.getTxHashList();

        blockHeader.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));

        NulsSignData signData = signDigest(blockHeader.getHash().getDigestBytes(), ecKey);

        P2PKHScriptSig sig = new P2PKHScriptSig();
        sig.setSignData(signData);
        sig.setPublicKey(ecKey.getPubKey());

        blockHeader.setScriptSig(sig);

        return block;
    }

    protected NulsSignData signDigest(byte[] digest, ECKey ecKey) {
        byte[] signbytes = ecKey.sign(digest);
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignAlgType(NulsSignData.SIGN_ALG_ECC);
        nulsSignData.setSignBytes(signbytes);

        return nulsSignData;
    }
}