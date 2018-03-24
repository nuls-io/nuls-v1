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
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.*;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;
import io.nuls.ledger.event.notice.BalanceChangeData;
import io.nuls.ledger.event.notice.BalanceChangeNotice;
import io.nuls.ledger.service.impl.LedgerCacheService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 */
public class UtxoTransactionTool {

    private static UtxoTransactionTool instance = new UtxoTransactionTool();

    private Lock lock = new ReentrantLock();

    public static final int APPROVE = 1;

    public static final int COMMIT = 2;

    public static final int ROLLBACK = 3;

    private UtxoTransactionTool() {

    }

    public static UtxoTransactionTool getInstance() {
        return instance;
    }

    private AccountService accountService;
    private EventBroadcaster eventBroadcaster;
    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    public TransferTransaction createTransferTx(CoinTransferData transferData, String password, String remark) throws Exception {
        if (transferData.getFrom().isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        TransferTransaction tx = new TransferTransaction(transferData, password);
        if (StringUtils.isNotBlank(remark)) {
            tx.setRemark(remark.getBytes(NulsContext.DEFAULT_ENCODING));
        }
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        AccountService accountService = getAccountService();
        Account account = accountService.getAccount(transferData.getFrom().get(0));
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        return tx;
    }

    public LockNulsTransaction createLockNulsTx(CoinTransferData transferData, String password, String remark) throws Exception {
        LockNulsTransaction tx = new LockNulsTransaction(transferData, password);
        if (StringUtils.isNotBlank(remark)) {
            tx.setRemark(remark.getBytes(NulsContext.DEFAULT_ENCODING));
        }
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        AccountService accountService = getAccountService();
        if (transferData.getFrom().isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        Account account = accountService.getAccount(transferData.getFrom().get(0));
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        return tx;
    }

    /**
     * check the tx is mine
     * when any input or output has my address
     *
     * @param tx
     * @return
     */
    public boolean isMine(AbstractCoinTransaction tx) throws NulsException {
        if (NulsContext.LOCAL_ADDRESS_LIST.isEmpty()) {
            return false;
        }

        UtxoData coinData = (UtxoData) tx.getCoinData();
        //check input
        if (coinData.getInputs() != null && !coinData.getInputs().isEmpty()) {
            for (UtxoInput input : coinData.getInputs()) {
                if (input.getFrom() == null) {
                    continue;
                }
                if (NulsContext.LOCAL_ADDRESS_LIST.contains(Address.fromHashs(input.getFrom().getAddress()).getBase58())) {
                    tx.setTransferType(Transaction.TRANSFER_SEND);
                    return true;
                }
            }
        }
        // check output
        if (coinData.getOutputs() != null && !coinData.getOutputs().isEmpty()) {
            for (UtxoOutput output : coinData.getOutputs()) {
                if (NulsContext.LOCAL_ADDRESS_LIST.contains(Address.fromHashs(output.getAddress()).getBase58())) {
                    tx.setTransferType(Transaction.TRANSFER_RECEIVE);
                    return true;
                }
            }
        }
        return false;
    }

    public void calcBalance(String address, boolean sendNotice) {
        lock.lock();
        try {
            UtxoBalance balance = (UtxoBalance) ledgerCacheService.getBalance(address);
            if (balance == null) {
                return;
            }
            checkUtxoTimeLock(balance.getUnSpends());

            long usable = 0;
            long lock = 0;
            for (UtxoOutput output : balance.getUnSpends()) {
                if (output.isUsable()) {
                    usable += output.getValue();
                } else if (output.isLocked()) {
                    usable += output.getValue();
                }
            }

            Balance oldBalance = new Balance(balance.getUsable(), balance.getLocked());

            balance.setLocked(Na.valueOf(lock));
            balance.setUsable(Na.valueOf(usable));
            balance.setBalance(balance.getUsable().add(balance.getLocked()));

            //check exchange
            if (oldBalance.getBalance().getValue() == balance.getBalance().getValue() &&
                    oldBalance.getUsable().getValue() == balance.getUsable().getValue() &&
                    oldBalance.getLocked().getValue() == balance.getLocked().getValue()) {
                return;
            }

            if (sendNotice) {
                sendNotice(oldBalance, balance, address);
            }

            if (balance.getUnSpends().isEmpty()) {
                ledgerCacheService.removeBalance(address);
            }
        } finally {
            lock.unlock();
        }
    }

    private void sendNotice(Balance oldBalance, Balance balance, String address) {
        long oldNa = oldBalance.getBalance().getValue();
        long change = balance.getBalance().getValue() - oldNa;

        if (NulsContext.LOCAL_ADDRESS_LIST != null && NulsContext.LOCAL_ADDRESS_LIST.contains(address)) {
            if (change == 0) {
                return;
            }
            BalanceChangeNotice notice = new BalanceChangeNotice();
            BalanceChangeData data = new BalanceChangeData();
            data.setAddress(address);
            data.setStatus(1);
            if (change < 0) {
                data.setType(1);
                data.setAmount(Na.valueOf(0 - change));
            } else {
                data.setType(0);
                data.setAmount(Na.valueOf(change));
            }
            notice.setEventBody(data);
            if (eventBroadcaster == null) {
                eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
            }
            eventBroadcaster.publishToLocal(notice);
        }
    }

    public void checkUtxoTimeLock(List<UtxoOutput> outputList) {
        long currentTime = TimeService.currentTimeMillis();
        long genesisTime = NulsContext.getInstance().getGenesisBlock().getHeader().getTime();
        long bestHeight = NulsContext.getInstance().getNetBestBlockHeight();
        for (UtxoOutput output : outputList) {
            if (output.isLocked()) {
                // check lock by time
                if (output.getLockTime() >= genesisTime && output.getLockTime() <= currentTime) {
                    if (OutPutStatusEnum.UTXO_CONFIRM_TIME_LOCK.equals(output.getStatus())) {
                        output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_UNSPEND);
                    } else if (OutPutStatusEnum.UTXO_UNCONFIRM_TIME_LOCK.equals(output.getStatus())) {
                        output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
                    }
                }
                // check lock by height
                else if (output.getLockTime() < genesisTime && output.getLockTime() >= bestHeight) {
                    if (OutPutStatusEnum.UTXO_CONFIRM_TIME_LOCK.equals(output.getStatus())) {
                        output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_UNSPEND);
                    } else if (OutPutStatusEnum.UTXO_UNCONFIRM_TIME_LOCK.equals(output.getStatus())) {
                        output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
                    }
                }
            }
        }
    }

    private AccountService getAccountService() {
        if (accountService == null) {
            accountService = NulsContext.getServiceBean(AccountService.class);
        }
        return accountService;
    }
}
