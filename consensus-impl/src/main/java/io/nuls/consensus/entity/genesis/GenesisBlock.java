package io.nuls.consensus.entity.genesis;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.utils.StringFileLoader;
import io.nuls.core.chain.entity.*;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.entity.tx.CoinbaseTransaction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
//todo temp
public final class GenesisBlock extends Block {

    private static GenesisBlock INSTANCE;

    public static void main(String args[]){
        GenesisBlock gb = new GenesisBlock();
        gb.setHeader();
    }

    public static GenesisBlock getInstance( ) {
        try {
            String json = StringFileLoader.read(PocConsensusConstant.GENESIS_BLOCK_FILE);
        } catch (NulsException e) {
            Log.error(e);
        }
        //todo
        if (null == INSTANCE) {
            INSTANCE = new GenesisBlock();
        }
        return INSTANCE;
    }

    private GenesisBlock() {
        //todo temp
        initGengsisTxs();
        fillHeader();

        // finally
//        try {
//            String json = readJsonFile();


//        } catch (NulsException e) {
//            Log.error(e);
//        }
    }


    private void fillHeader() {
        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(1);
        header.setPreHash(NulsDigestData.EMPTY_HASH);
        header.setMerkleHash(NulsDigestData.calcDigestData(new byte[]{0}));
        header.setTime(0L);
        header.setTxCount(1);
        List<NulsDigestData> txHashList = new ArrayList<>();
        txHashList.add(NulsDigestData.EMPTY_HASH);
        header.setTxHashList(txHashList);
        NulsDigestData hash = NulsDigestData.calcDigestData(this);
        header.setHash(hash);
        header.setSign(NulsContext.getInstance().getService(AccountService.class).signData(header.getHash()));
    }

    private void initGengsisTxs() {
        //todo 总nuls量及每个地址的nuls量
        CoinbaseTransaction tx = new CoinbaseTransaction();
        tx.setHash(NulsDigestData.EMPTY_HASH);
        List<Transaction> list = new ArrayList<>();
        list.add(tx);
        setTxs(list);
    }

}
