package io.nuls.consensus.cache.manager.tx;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class ReceivedTxCacheManager {
    private static ReceivedTxCacheManager INSTANCE = new ReceivedTxCacheManager();
    private ReceivedTxCacheManager(){}
    public static ReceivedTxCacheManager getInstance(){
        return INSTANCE;
    }

    public boolean txExist(NulsDigestData hash){
        //todo
        return false;
    }

    public Transaction getTx(NulsDigestData txHash) {
        // todo auto-generated method stub(niels)
        return null;
    }

    public void removeTx(List<NulsDigestData> txHashList) {
        // todo auto-generated method stub(niels)

    }

    public List<Transaction> getTxList() {
        // todo auto-generated method stub(niels)
        return null;
    }
}
