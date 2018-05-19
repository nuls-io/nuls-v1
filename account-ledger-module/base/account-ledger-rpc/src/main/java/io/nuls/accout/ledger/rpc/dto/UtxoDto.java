package io.nuls.accout.ledger.rpc.dto;

import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;

public class UtxoDto {

    private String txHash;

    private long createTime;

    private int txType;

    private long lockTime;

    private long value;

    public UtxoDto() {

    }

    public UtxoDto(Coin coin, Transaction tx) {
        this.txHash = tx.getHash().getDigestHex();
        this.createTime = tx.getTime();
        this.txType = tx.getType();
        this.lockTime = coin.getLockTime();
        this.value = coin.getNa().getValue();
    }
}
