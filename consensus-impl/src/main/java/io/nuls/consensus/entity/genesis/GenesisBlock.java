/**
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
package io.nuls.consensus.entity.genesis;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.utils.StringFileLoader;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.CoinBaseTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/10
 */
public final class GenesisBlock extends Block {
    private static final String CONFIG_FILED_TIME = "time";
    private static final String CONFIG_FILED_HEIGHT = "height";
    private static final String CONFIG_FILED_TXS = "txs";
    private static final String CONFIG_FILED_ADDRESS = "address";
    private static final String CONFIG_FILED_NULS = "nuls";
    private static final String CONFIG_FILED_UNLOCK_HEIGHT = "unlockHeight";

    private static GenesisBlock INSTANCE;

    public static GenesisBlock getInstance() {
        if (null == INSTANCE) {
            String json = null;
            try {
                json = StringFileLoader.read(PocConsensusConstant.GENESIS_BLOCK_FILE);
            } catch (NulsException e) {
                Log.error(e);
            }
            INSTANCE = new GenesisBlock(json);
        }
        return INSTANCE;
    }

    private GenesisBlock(String json) {
        Map<String, Object> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json);
        } catch (Exception e) {
            Log.error(e);
        }
        this.initGengsisTxs(jsonMap);
        this.fillHeader(jsonMap);
    }

    private void initGengsisTxs(Map<String, Object> jsonMap) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) jsonMap.get(CONFIG_FILED_TXS);
        if (null == list || list.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.CONFIG_ERROR);
        }
        CoinTransferData data = new CoinTransferData();
        data.setFee(Na.ZERO);
        Na total = Na.ZERO;
        for (Map<String, Object> map : list) {
            String address = (String) map.get(CONFIG_FILED_ADDRESS);
            AssertUtil.canNotEmpty(address, ErrorCode.NULL_PARAMETER);
            Integer nuls = (Integer) map.get(CONFIG_FILED_NULS);
            AssertUtil.canNotEmpty(nuls, ErrorCode.NULL_PARAMETER);
            Integer height = (Integer) map.get(CONFIG_FILED_UNLOCK_HEIGHT);
            Coin coin = new Coin();
            coin.setNa(Na.parseNuls(nuls));
            coin.setCanBeUnlocked(false);
            coin.setUnlockTime(0);
            if (height == null) {
                coin.setUnlockTime(0);
            } else {
                coin.setUnlockHeight(height.longValue());
            }
            data.addTo(address, coin);
            total = total.add(coin.getNa());
        }
        data.setTotalNa(total);
        CoinBaseTransaction tx = null;
        try {
            tx = new CoinBaseTransaction(data, null);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        tx.setFee(Na.ZERO);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        //todo
        tx.setSign(new NulsSignData());
        List<Transaction> txlist = new ArrayList<>();
        txlist.add(tx);
        setTxs(txlist);
    }


    private void fillHeader(Map<String, Object> jsonMap) {
        Integer height = (Integer) jsonMap.get(CONFIG_FILED_HEIGHT);
        AssertUtil.canNotEmpty(height, ErrorCode.CONFIG_ERROR);
        String time = (String) jsonMap.get(CONFIG_FILED_TIME);
        AssertUtil.canNotEmpty(time, ErrorCode.CONFIG_ERROR);
        long blockTime = DateUtil.convertStringToDate(time).getTime();
        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(height);
        header.setTime(blockTime);
        header.setPreHash(NulsDigestData.EMPTY_HASH);
        header.setTxCount(this.getTxs().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : this.getTxs()) {
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setHash(NulsDigestData.calcDigestData(this));
        BlockRoundData data = new BlockRoundData();
        data.setRoundIndex(1);
        data.setRoundStartTime(header.getTime());
        data.setConsensusMemberCount(1);
        data.setPackingIndexOfRound(1);
        try {
            header.setExtend(data.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        //todo change to real address & signature
        header.setPackingAddress("00000");
        header.setSign(new NulsSignData());
    }


}

