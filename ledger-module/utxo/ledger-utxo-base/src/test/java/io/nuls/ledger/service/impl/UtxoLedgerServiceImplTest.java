package io.nuls.ledger.service.impl;

import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.*;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.model.tx.CoinBaseTransaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import org.iq80.leveldb.util.Slice;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UtxoLedgerServiceImplTest {

    private List<Transaction> allList;
    private List<Transaction> txList;

    private LedgerService ledgerService;

    @Before
    public void setUp() throws Exception {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
        bootstrap.init();
        bootstrap.start();

        ledgerService = SpringLiteContext.getBean(LedgerService.class);
        initAllList();
    }

    @Test
    public void saveTx() throws IOException {
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Result result = ledgerService.saveTx(tx);
        Assert.assertTrue(result.isSuccess());
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + new Slice(txFromDB.serialize()));
        Assert.assertEquals(new Slice(tx.serialize()), new Slice(txFromDB.serialize()));

    }

    @Test
    public void rollbackTx() throws IOException {
        saveTx();
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Result result = ledgerService.rollbackTx(tx);
        Assert.assertTrue(result.isSuccess());
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + txFromDB);
        Assert.assertNull(txFromDB);
    }

    @Test
    public void getTx() throws IOException {
        saveTx();
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + new Slice(txFromDB.serialize()));
        Assert.assertEquals(new Slice(tx.serialize()), new Slice(txFromDB.serialize()));
    }

    @Test
    public void verifyCoinDataItself() throws IOException {
        for(Transaction tx : allList) {
            if(tx.getCoinData() == null) {
                continue;
            }
            if(tx.getCoinData().getFrom().size() == 0) {
                continue;
            }
            System.out.println("tx: " + new Slice(tx.serialize()));
            Result result = ledgerService.verifyCoinData(tx.getCoinData());
            System.out.println(result.getErrorCode().getCode());
            Assert.assertTrue(result.isSuccess());
        }

    }

    @Test
    public void verifyCoinDataIsExist() {
        Result result = ledgerService.verifyCoinData(allList.get(2).getCoinData(), allList);
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void verifyDoubleSpend() {
        Result result = ledgerService.verifyDoubleSpend(allList);
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void unlockTxCoinData() {
        Result result = ledgerService.unlockTxCoinData(allList.get(1));
        Assert.assertTrue(result.isSuccess());
    }

    private void initAllList() throws NulsException, IOException {
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

        RedPunishTransaction redPunishTransaction = createRedPunishTx(ecKey1, ecKey4, ecKey5, ecKey6);
        list.add(redPunishTransaction);

        TransferTransaction transferTransaction1 = createTransferTransaction(ecKey1, null, ecKey2, Na.ZERO);
        TransferTransaction transferTransaction2 = createTransferTransaction(ecKey1, null, ecKey3, Na.ZERO);
        list.add(transferTransaction1);
        list.add(transferTransaction2);

//        createSetAliasTransaction(ecKey1, "alias");
//        createSetAliasTransaction(ecKey1, "alias1");
//        createSetAliasTransaction(ecKey2, "alias");

        RegisterAgentTransaction tx1 = createRegisterAgentTransaction(ecKey1, ecKey2, "agentName");
        RegisterAgentTransaction tx2 = createRegisterAgentTransaction(ecKey2, ecKey3, "agentName");
        RegisterAgentTransaction tx3 = createRegisterAgentTransaction(ecKey4, ecKey5, "agentName2");
        RegisterAgentTransaction tx4 = createRegisterAgentTransaction(ecKey1, ecKey3, "agentName3");
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

        //createCancelDepositTransaction(ecKey1, NulsDigestData.fromDigestHex("ab"));

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

    private CancelDepositTransaction createCancelDepositTransaction(ECKey ecKey, NulsDigestData txHash) throws IOException {
        CancelDepositTransaction tx = new CancelDepositTransaction();
        setCommonFields(tx);
        CancelDeposit cd = new CancelDeposit();
        cd.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        cd.setJoinTxHash(txHash);
        tx.setTxData(cd);
        signTransaction(tx, ecKey);
        return tx;
    }

    private StopAgentTransaction createStopAgentTransaction(ECKey ecKey, NulsDigestData agentTxHash) throws IOException {
        StopAgentTransaction tx = new StopAgentTransaction();
        setCommonFields(tx);
        StopAgent txData = new StopAgent();
        txData.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        txData.setRegisterTxHash(agentTxHash);
        tx.setTxData(txData);
        signTransaction(tx, ecKey);
        return tx;

    }

    private DepositTransaction createDepositTransaction(ECKey ecKey, NulsDigestData agentTxHash, Na na) throws IOException {
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

    private RegisterAgentTransaction createRegisterAgentTransaction(ECKey ecKey1, ECKey ecKey2, String agentName) throws IOException {
        RegisterAgentTransaction tx = new RegisterAgentTransaction();
        setCommonFields(tx);
        Agent agent = new Agent();
        agent.setBlockHeight(1);
        agent.setDelHeight(0);
        agent.setTime(System.currentTimeMillis());
        agent.setAgentAddress(AddressTool.getAddress(ecKey1.getPubKey()));
        agent.setAgentName("test-agent-1".getBytes());
        agent.setCommissionRate(10);
        agent.setDeposit(Na.parseNuls(20000));
        agent.setIntroduction("说明".getBytes());
        agent.setPackingAddress(AddressTool.getAddress(ecKey2.getPubKey()));
        agent.setRewardAddress(agent.getAgentAddress());
        tx.setTxData(agent);
        signTransaction(tx, ecKey1);
        return tx;
    }

    private Transaction createSetAliasTransaction(ECKey ecKey, String alias) {
        return null;
    }

    private TransferTransaction createTransferTransaction(ECKey ecKey1, byte[] coinKey, ECKey ecKey2, Na na) throws IOException {
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

    private CoinBaseTransaction createCoinBaseTransaction(ECKey ecKey, ECKey... ecKeys) throws IOException {
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

    private void signTransaction(Transaction tx, ECKey ecKey) throws IOException {
        NulsDigestData hash = null;
        hash = NulsDigestData.calcDigestData(tx.serializeForHash());
        tx.setHash(hash);
        byte[] signbytes = new byte[0];
        signbytes = ecKey.sign(hash.serialize());
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignAlgType(NulsSignData.SIGN_ALG_ECC);
        nulsSignData.setSignBytes(signbytes);
        P2PKHScriptSig scriptSig = new P2PKHScriptSig();
        scriptSig.setPublicKey(ecKey.getPubKey());
        scriptSig.setSignData(nulsSignData);
        tx.setScriptSig(scriptSig.serialize());
    }

    @After
    public void tearDown() throws Exception {
    }
}