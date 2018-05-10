/*
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
 *
 */

package io.nuls.protocol.base.service;

import io.nuls.consensus.constant.PunishReasonEnum;
import io.nuls.consensus.entity.Deposit;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.tx.*;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.model.tx.CoinBaseTransaction;
import org.apache.tools.ant.taskdefs.Echo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/5/8
 */
public class TransactionServiceImplTest {

    @Before
    public void init() {
        initTxList();
    }

    @Test
    public void commitTx() {
    }

    @Test
    public void rollback() {
    }

    @Test
    public void forwardTx() {
    }

    @Test
    public void broadcastTx() {
    }

    @Test
    public void conflictDetect() {
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

        createYellowPunishTx(ecKey1, new ECKey[3]);

        createRedPunishTx(ecKey1, new ECKey[3]);

        createTransaferTransaction(ecKey1, ecKey1, Na.ZERO);

        createSetAliasTransaction(ecKey1, "alias");

        createRegisterAgentTransaction(ecKey1, ecKey1, "agentName");

        createDepositTransaction(ecKey1, NulsDigestData.fromDigestHex("agentTxHash"), Na.ZERO);

        createCancelDepositTransaction(ecKey1, NulsDigestData.fromDigestHex("txHash"));

//        createStopAgentTransaction(ecKey1);


    }

    private RedPunishTransaction createRedPunishTx(ECKey ecKey, ECKey... ecKeys) {
        RedPunishTransaction tx = new RedPunishTransaction();
        setCommonFields(tx);
        RedPunishData data = new RedPunishData();
        data.setAddress(AddressTool.getAddress(ecKeys[0].getPubKey()));
        data.setEvidence("for test".getBytes());
        data.setHeight(1);
        data.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
        tx.setTxData(data);
        return tx;
    }

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
        tx.setTxData(txHash);
        signTransaction(tx, ecKey);
        return tx;
    }

    private StopAgentTransaction createStopAgentTransaction(ECKey ecKey, NulsDigestData agentTxHash) {
        StopAgentTransaction tx = new StopAgentTransaction();
        setCommonFields(tx);
        tx.setTxData(agentTxHash);
        signTransaction(tx, ecKey);
        return tx;

    }

    private JoinConsensusTransaction createDepositTransaction(ECKey ecKey, NulsDigestData agentTxHash, Na na) {
        JoinConsensusTransaction tx = new JoinConsensusTransaction();
        setCommonFields(tx);
        Deposit deposit = new Deposit();
        deposit.setDelHeight(0L);





        return tx;
    }

    private Transaction createRegisterAgentTransaction(ECKey ecKey1, ECKey ecKey2, String agentName) {
        return null;
    }

    private Transaction createSetAliasTransaction(ECKey ecKey, String alias) {
        return null;
    }

    private Transaction createTransaferTransaction(ECKey ecKey1, ECKey ecKey2, Na na) {
        return null;
    }

    private Transaction createCoinBaseTransaction(ECKey ecKey, ECKey... ecKeys) {
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
        NulsDigestData hash = NulsDigestData.calcDigestData(tx.serializeForHash());
        tx.setHash(hash);
        byte[] signbytes = ecKey.sign(hash.serialize());
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignAlgType(NulsSignData.SIGN_ALG_ECC);
        nulsSignData.setSignBytes(signbytes);
        P2PKHScriptSig scriptSig = new P2PKHScriptSig();
        scriptSig.setPublicKey(ecKey.getPubKey());
        scriptSig.setSignData(nulsSignData);
        tx.setScriptSig(scriptSig.serialize());
    }
}