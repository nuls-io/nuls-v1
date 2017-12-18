package io.nuls.consensus.entity.genesis;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.entity.NulsBlock;
import io.nuls.core.chain.entity.*;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.ledger.entity.tx.CoinbaseTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
//todo temp
public final class DevGenesisBlock extends NulsBlock {

    private static final DevGenesisBlock INSTANCE = new DevGenesisBlock();

    public static DevGenesisBlock getInstance() {
        return INSTANCE;
    }

    private DevGenesisBlock() {
        this.setCountOfRound(1);
        this.setOrderInRound(1);
        this.setRoundStartTime(DateUtil.convertStringToDate("2017-12-10 00:00:00").getTime());
        initGengsisTxs();
        fillHeader();
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
