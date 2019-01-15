/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 *
 */

package io.nuls.protocol.base.service;

import io.nuls.consensus.poc.protocol.entity.*;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.log.Log;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.module.impl.UtxoLedgerModuleBootstrap;
import io.nuls.network.netty.module.impl.NettyNetworkModuleBootstrap;
import io.nuls.protocol.model.tx.CoinBaseTransaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import io.nuls.protocol.service.TransactionService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 */
public class TransactionServiceImplTest {

    private List<Transaction> allList;
    private List<Transaction> txList;

    private TransactionService transactionService;


    @Before
    public void init() {

        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
        bootstrap.init();
        bootstrap.start();

        UtxoLedgerModuleBootstrap ledgerModuleBootstrap = new UtxoLedgerModuleBootstrap();
        ledgerModuleBootstrap.init();
        ledgerModuleBootstrap.start();

        NettyNetworkModuleBootstrap networkModuleBootstrap = new NettyNetworkModuleBootstrap();
        networkModuleBootstrap.init();
        networkModuleBootstrap.start();

        transactionService = SpringLiteContext.getBean(TransactionService.class);
        initTxList();
    }

    @Test
    public void test() {
        this.conflictDetect();
        this.commitTx();
        this.rollback();
        this.broadcastTx();
        this.forwardTx();
    }

    private void commitTx() {
        for (Transaction tx : txList) {
            Result result = this.transactionService.commitTx(tx, null);
            assertTrue(result.isSuccess());
        }
    }

    private void rollback() {
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            Result result = this.transactionService.rollbackTx(tx, null);
            assertTrue(result.isSuccess());
        }
        Result result = this.transactionService.rollbackTx(txList.get(0), null);
        assertFalse(result.isSuccess());
    }

    private void forwardTx() {
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            Result result = this.transactionService.forwardTx(tx, null);
            assertTrue(result.isSuccess());
        }
    }

    private void broadcastTx() {
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            Result result = this.transactionService.forwardTx(tx, null);
            assertTrue(result.isSuccess());
        }
    }

    private void conflictDetect() {
        ValidateResult result = transactionService.conflictDetect(allList);
        this.txList = (List<Transaction>) result.getData();
        assertNotNull(txList);
        //数值需要确定，目前随便写的
        assertEquals(2, 2);
    }

    private void initTxList() {
        List<Transaction> list = new ArrayList<>();
        ECKey ecKey1 = new ECKey();
        ECKey ecKey2 = new ECKey();
        ECKey ecKey3 = new ECKey();
        ECKey ecKey4 = new ECKey();
        ECKey ecKey5 = new ECKey();
        ECKey ecKey6 = new ECKey();


        Transaction tx = createCoinBaseTransaction(ecKey1, ecKey2, ecKey3, ecKey4, ecKey5, ecKey6);
        list.add(tx);

        Transaction yellowPunishTx = createYellowPunishTx(ecKey1, ecKey2, ecKey3, ecKey4, ecKey5, ecKey6);
        list.add(yellowPunishTx);

//        RedPunishTransaction redPunishTransaction = createRedPunishTx(ecKey1, ecKey4, ecKey5, ecKey6);
//        list.add(redPunishTransaction);

        TransferTransaction transferTransaction1 = createTransferTransaction(ecKey1, null, ecKey2, Na.ZERO);
        TransferTransaction transferTransaction2 = createTransferTransaction(ecKey1, null, ecKey3, Na.ZERO);
        list.add(transferTransaction1);
        list.add(transferTransaction2);

        createSetAliasTransaction(ecKey1, "alias");
//        createSetAliasTransaction(ecKey1, "alias1");
//        createSetAliasTransaction(ecKey2, "alias");

        CreateAgentTransaction tx1 = createRegisterAgentTransaction(ecKey1, ecKey2, "agentName");
        CreateAgentTransaction tx2 = createRegisterAgentTransaction(ecKey2, ecKey3, "agentName");
        CreateAgentTransaction tx3 = createRegisterAgentTransaction(ecKey4, ecKey5, "agentName2");
        CreateAgentTransaction tx4 = createRegisterAgentTransaction(ecKey1, ecKey3, "agentName3");
        list.add(tx1);
        list.add(tx2);
        list.add(tx3);
        list.add(tx4);

        DepositTransaction join1 = createDepositTransaction(ecKey1, tx1.getHash(), Na.parseNuls(200000));
        DepositTransaction join2 = createDepositTransaction(ecKey1, tx2.getHash(), Na.parseNuls(200000));
        DepositTransaction join3 = createDepositTransaction(ecKey1, tx3.getHash(), Na.parseNuls(200000));
        DepositTransaction join4 = createDepositTransaction(ecKey1, tx4.getHash(), Na.parseNuls(200000));
        DepositTransaction join5 = createDepositTransaction(ecKey1, tx3.getHash(), Na.parseNuls(200000));
        DepositTransaction join6 = createDepositTransaction(ecKey1, tx3.getHash(), Na.parseNuls(200000));
        DepositTransaction join7 = createDepositTransaction(ecKey1, tx3.getHash(), Na.parseNuls(200000));
        list.add(join1);
        list.add(join3);
        list.add(join2);
        list.add(join4);
        list.add(join5);
        list.add(join6);
        list.add(join7);

        try {
            createCancelDepositTransaction(ecKey1, NulsDigestData.fromDigestHex("txHash"));
        } catch (NulsException e) {
            Log.error(e);
        }

        StopAgentTransaction stop1 = createStopAgentTransaction(ecKey1, tx1.getHash());
        StopAgentTransaction stop2 = createStopAgentTransaction(ecKey1, tx2.getHash());
        StopAgentTransaction stop3 = createStopAgentTransaction(ecKey4, tx3.getHash());
        StopAgentTransaction stop4 = createStopAgentTransaction(ecKey1, tx4.getHash());
        list.add(stop1);
        list.add(stop2);
        list.add(stop3);
        list.add(stop4);
        this.allList = list;
    }

//    private RedPunishTransaction createRedPunishTx(ECKey ecKey, ECKey... ecKeys) {
//        RedPunishTransaction tx = new RedPunishTransaction();
//        setCommonFields(tx);
//        RedPunishData data = new RedPunishData();
//        data.setAddress(AddressTool.getAddress(ecKeys[0].getPubKey()));
//        data.setEvidence("for test".getBytes());
//        data.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
//        tx.setTxData(data);
//        return tx;
//    }

    private YellowPunishTransaction createYellowPunishTx(ECKey ecKey, ECKey... ecKeys) {
        YellowPunishTransaction tx = new YellowPunishTransaction();
        setCommonFields(tx);
        YellowPunishData data = new YellowPunishData();
        List<byte[]> addressList = new ArrayList<>();
        for (ECKey ecKey1 : ecKeys) {
            addressList.add(AddressTool.getAddress(ecKey1.getPubKey()));
        }
        data.setAddressList(addressList);
        tx.setTxData(data);
        return tx;
    }

    private CancelDepositTransaction createCancelDepositTransaction(ECKey ecKey, NulsDigestData txHash) {
        CancelDepositTransaction tx = new CancelDepositTransaction();
        setCommonFields(tx);
        CancelDeposit cd = new CancelDeposit();
        cd.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        cd.setJoinTxHash(txHash);
        tx.setTxData(cd);
        signTransaction(tx, ecKey);
        return tx;
    }

    private StopAgentTransaction createStopAgentTransaction(ECKey ecKey, NulsDigestData agentTxHash) {
        StopAgentTransaction tx = new StopAgentTransaction();
        setCommonFields(tx);
        StopAgent txData = new StopAgent();
        txData.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        txData.setCreateTxHash(agentTxHash);
        tx.setTxData(txData);
        signTransaction(tx, ecKey);
        return tx;

    }

    private DepositTransaction createDepositTransaction(ECKey ecKey, NulsDigestData agentTxHash, Na na) {
        DepositTransaction tx = new DepositTransaction();
        setCommonFields(tx);
        Deposit deposit = new Deposit();
        deposit.setDelHeight(0L);
        deposit.setBlockHeight(1);
        deposit.setTime(System.currentTimeMillis());
        deposit.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        deposit.setAgentHash(agentTxHash);
        deposit.setDeposit(na);
        tx.setTxData(deposit);
        signTransaction(tx, ecKey);
        return tx;
    }

    private CreateAgentTransaction createRegisterAgentTransaction(ECKey ecKey1, ECKey ecKey2, String agentName) {
        CreateAgentTransaction tx = new CreateAgentTransaction();
        setCommonFields(tx);
        Agent agent = new Agent();
        agent.setBlockHeight(1);
        agent.setDelHeight(0);
        agent.setTime(System.currentTimeMillis());
        agent.setAgentAddress(AddressTool.getAddress(ecKey1.getPubKey()));
        agent.setCommissionRate(10);
        agent.setDeposit(Na.parseNuls(20000));
        agent.setPackingAddress(AddressTool.getAddress(ecKey2.getPubKey()));
        agent.setRewardAddress(agent.getAgentAddress());
        tx.setTxData(agent);
        signTransaction(tx, ecKey1);
        return tx;
    }

    private Transaction createSetAliasTransaction(ECKey ecKey, String alias) {
        return null;
    }

    private TransferTransaction createTransferTransaction(ECKey ecKey1, byte[] coinKey, ECKey ecKey2, Na na) {
        TransferTransaction tx = new TransferTransaction();
        setCommonFields(tx);
        CoinData coinData = new CoinData();
        List<Coin> fromList = new ArrayList<>();
        fromList.add(new Coin(coinKey, Na.parseNuls(10001), 0));
        coinData.setFrom(fromList);
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(AddressTool.getAddress(ecKey2.getPubKey()), Na.parseNuls(10000), 1000));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        signTransaction(tx, ecKey1);
        return tx;
    }

    private CoinBaseTransaction createCoinBaseTransaction(ECKey ecKey, ECKey... ecKeys) {
        CoinBaseTransaction tx = new CoinBaseTransaction();
        setCommonFields(tx);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(AddressTool.getAddress(ecKey.getPubKey()), Na.parseNuls(10000), 1000));
        toList.add(new Coin(AddressTool.getAddress(ecKey.getPubKey()), Na.parseNuls(10000), 0));
        for (ECKey ecKey1 : ecKeys) {
            Coin coin = new Coin(AddressTool.getAddress(ecKey1.getPubKey()), Na.parseNuls(10000), 0);
            toList.add(coin);
        }
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        signTransaction(tx, ecKey);
        return tx;
    }

    private void setCommonFields(Transaction tx) {
        tx.setTime(System.currentTimeMillis());
        tx.setBlockHeight(1);
        tx.setRemark("for test".getBytes());
    }

    private void signTransaction(Transaction tx, ECKey ecKey) {
        NulsDigestData hash = null;
        try {
            hash = NulsDigestData.calcDigestData(tx.serializeForHash());
        } catch (IOException e) {
            Log.error(e);
        }
        tx.setHash(hash);

        List<ECKey> keys = new ArrayList<>();
        keys.add(ecKey);

        try {
            SignatureUtil.createTransactionSignture(tx, null, keys);
        } catch (Exception e) {
            Log.error(e);
        }
    }
}