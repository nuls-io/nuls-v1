package io.nuls.ledger.util;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.UtxoInputDataService;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.service.impl.LedgerCacheService;

import java.util.List;

public class UtxoTransactionTool {

    private static UtxoTransactionTool instance = new UtxoTransactionTool();

    private UtxoTransactionTool() {

    }

    public static UtxoTransactionTool getInstance() {
        return instance;
    }

    private AccountService accountService;

    private UtxoInputDataService inputDataService;

    private LedgerCacheService ledgerCacheService;

    public TransferTransaction createTransferTx(CoinTransferData transferData, String password, String remark) throws Exception {
        TransferTransaction tx = new TransferTransaction(transferData, password);
        tx.setRemark(remark.getBytes(NulsContext.DEFAULT_ENCODING));
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));

        tx.setSign(getAccountService().signData(tx.getHash(), password));
        return tx;
    }

    public LockNulsTransaction createLockNulsTx(CoinTransferData transferData, String password) throws Exception {
        LockNulsTransaction tx = new LockNulsTransaction(transferData, password);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));

        tx.setSign(getAccountService().signData(tx.getHash(), password));
        return tx;
    }

    public boolean isMine(AbstractCoinTransaction tx) {
        UtxoData coinData = (UtxoData) tx.getCoinData();
        List<Account> accounts = getAccountService().getAccountList();

        for (Account account : accounts) {
            UtxoBalance balance = (UtxoBalance) ledgerCacheService.getBalance(account.getAddress().getBase58());
            for (UtxoOutput output : balance.getUnSpends()) {
                for (UtxoInput input : coinData.getInputs()) {
                    if (output.getTxHash().getDigestHex().equals(input.getTxHash().getDigestHex()) &&
                            output.getIndex() == input.getFromIndex()) {
                        return true;
                    }
                }
            }
            for (UtxoOutput output : coinData.getOutputs()) {
                if (new Address(0, output.getAddress()).equals(account.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private AccountService getAccountService() {
        if (accountService == null) {
            accountService = NulsContext.getInstance().getService(AccountService.class);
        }
        return accountService;
    }

    public void setInputDataService(UtxoInputDataService inputDataService) {
        this.inputDataService = inputDataService;
    }
}
