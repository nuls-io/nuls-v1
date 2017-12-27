package io.nuls.consensus.entity.genesis;

import io.nuls.consensus.constant.PocConsensusConstant;
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
            Long nuls = (Long) map.get(CONFIG_FILED_NULS);
            AssertUtil.canNotEmpty(nuls, ErrorCode.NULL_PARAMETER);
            Long height = (Long) map.get(CONFIG_FILED_UNLOCK_HEIGHT);
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
        CoinBaseTransaction tx = new CoinBaseTransaction(data, null);
        tx.setHash(NulsDigestData.calcDigestData(tx));
        List<Transaction> txlist = new ArrayList<>();
        txlist.add(tx);
        setTxs(txlist);
    }


    private void fillHeader(Map<String, Object> jsonMap) {
        Long height = (Long) jsonMap.get(CONFIG_FILED_HEIGHT);
        AssertUtil.canNotEmpty(height,ErrorCode.CONFIG_ERROR);
        String time = (String) jsonMap.get(CONFIG_FILED_TIME);
        AssertUtil.canNotEmpty(time,ErrorCode.CONFIG_ERROR);
        long blockTime = DateUtil.convertStringToDate(time).getTime();
        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(height);
        header.setTime(blockTime);
        header.setPreHash(NulsDigestData.EMPTY_HASH);
        header.setTxCount(this.getTxs().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for(Transaction tx:this.getTxs()){
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setTxHashList(txHashList);
        header.setHash(NulsDigestData.calcDigestData(this));
    }


}

