package io.nuls.account.ledger.model;

import io.nuls.kernel.model.Transaction;

public class TransactionDataResult {

    private Transaction transaction;

    private boolean enough;

    /**
     * 判断交易中UTXO的签名类型（只包含老交易为00000001,只包含新交易为00000010,都包含为00000011）
     * */
    private byte signType;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public boolean isEnough() {
        return enough;
    }

    public void setEnough(boolean enough) {
        this.enough = enough;
    }

    public byte getSignType() {
        return signType;
    }

    public void setSignType(byte signType) {
        this.signType = signType;
    }
}
