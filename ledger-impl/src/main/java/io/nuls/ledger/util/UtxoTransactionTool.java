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
package io.nuls.ledger.util;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
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

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    public TransferTransaction createTransferTx(CoinTransferData transferData, String password, String remark) throws Exception {
        TransferTransaction tx = new TransferTransaction(transferData, password);
        tx.setRemark(remark.getBytes(NulsContext.DEFAULT_ENCODING));
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));

        tx.setSign(getAccountService().signData(tx.getHash(), password));
        return tx;
    }

    public LockNulsTransaction createLockNulsTx(CoinTransferData transferData, String password,String remark) throws Exception {
        LockNulsTransaction tx = new LockNulsTransaction(transferData, password);
        if(StringUtils.isNotBlank(remark)){
            tx.setRemark(remark.getBytes(NulsContext.DEFAULT_ENCODING));
        }
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
