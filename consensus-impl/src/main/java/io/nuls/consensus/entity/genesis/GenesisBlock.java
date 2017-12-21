package io.nuls.consensus.entity.genesis;

import io.nuls.account.service.intf.AccountService;
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

    private static final GenesisBlock INSTANCE = new GenesisBlock();

    public static GenesisBlock getInstance() {
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

    private String readJsonFile() throws NulsException {
        String jsonPath = GenesisBlock.class.getResource("genesis-block.json").getPath();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(jsonPath));
        } catch (FileNotFoundException e) {
            Log.error(e);
            throw new NulsException(e);
        }
        StringBuilder str = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                str.append(line.trim());
            }
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(e);
        }finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return str.toString();
    }

    private void fillHeader() {
        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(1);
        header.setMerkleHash(NulsDigestData.calcDigestData(new byte[]{0}));
        header.setTime(0L);
        header.setTxCount(1);
        header.setHash(NulsDigestData.calcDigestData(this));
        header.setSign(NulsContext.getInstance().getService(AccountService.class).signData(header.getHash()));
    }

    private void initGengsisTxs() {
        //todo 总nuls量及每个地址的nuls量
        CoinbaseTransaction tx = new CoinbaseTransaction();
        List<Transaction> list = new ArrayList<>();
        setTxs(list);
    }

}
