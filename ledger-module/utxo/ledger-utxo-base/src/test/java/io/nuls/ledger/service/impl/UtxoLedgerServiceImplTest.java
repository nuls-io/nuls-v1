package io.nuls.ledger.service.impl;

import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.*;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.constant.LedgerStorageConstant;
import io.nuls.ledger.storage.service.UtxoLedgerTransactionStorageService;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;
import io.nuls.protocol.model.tx.CoinBaseTransaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import org.iq80.leveldb.util.Slice;
import org.junit.*;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UtxoLedgerServiceImplTest {

    private static List<Transaction> allList;
    private static List<Transaction> txList;

    private static LedgerService ledgerService;
    private static UtxoLedgerUtxoStorageService utxoStorageService;
    private static UtxoLedgerTransactionStorageService transactionStorageService;

    @BeforeClass
    public static void setUp() throws Exception {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
        bootstrap.init();
        bootstrap.start();

        ledgerService = SpringLiteContext.getBean(LedgerService.class);
        utxoStorageService = SpringLiteContext.getBean(UtxoLedgerUtxoStorageService.class);
        transactionStorageService = SpringLiteContext.getBean(UtxoLedgerTransactionStorageService.class);
        initAllList();
    }

    private void recoveryTx3Data() {
        allList.get(3).getCoinData().getFrom().clear();
        allList.get(3).getCoinData().getFrom().add(new Coin("abcd3".getBytes(), Na.parseNuls(10001), 0));
        Coin coinTemp = allList.get(3).getCoinData().getTo().remove(0);
        allList.get(3).getCoinData().getTo().clear();
        allList.get(3).getCoinData().getTo().add(coinTemp);
    }

    @Test
    public void saveTx() throws IOException {
        // 无from的交易
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Result result = ledgerService.saveTx(tx);
        System.out.println(result);
        Assert.assertTrue(result.isSuccess());
        byte[] toCoin = utxoStorageService.getCoinBytes(Arrays.concatenate(tx.getHash().serialize(), new VarInt(0).encode()));
        Assert.assertNotNull(toCoin);
        Assert.assertEquals(new Slice(tx.getCoinData().getTo().get(0).serialize()), new Slice(toCoin));
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + new Slice(txFromDB.serialize()));
        Assert.assertEquals(new Slice(tx.serialize()), new Slice(txFromDB.serialize()));

        // 有from的交易, 检验交易和coindata
        Transaction tx3 = allList.get(3);
        CoinData from3 = tx3.getCoinData();
        from3.getFrom().get(0).setOwner("abcd3".getBytes());
        from3.getFrom().add(new Coin("abcd3.1".getBytes(), Na.parseNuls(10002), 0));
        from3.getFrom().add(new Coin("abcd3.2".getBytes(), Na.parseNuls(10003), 0));
        from3.getTo().add(new Coin("abcd3.1".getBytes(), Na.parseNuls(10002), 0));
        from3.getTo().add(new Coin("abcd3.2".getBytes(), Na.parseNuls(10003), 0));
        result = ledgerService.saveTx(tx3);
        Assert.assertTrue(result.isSuccess());
        byte[] to3Coin0 = utxoStorageService.getCoinBytes(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(0).encode()));
        byte[] to3Coin1 = utxoStorageService.getCoinBytes(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(1).encode()));
        byte[] to3Coin2 = utxoStorageService.getCoinBytes(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(2).encode()));
        Assert.assertNotNull(to3Coin0);
        Assert.assertNotNull(to3Coin1);
        Assert.assertNotNull(to3Coin2);
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(0).serialize()), new Slice(to3Coin0));
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(1).serialize()), new Slice(to3Coin1));
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(2).serialize()), new Slice(to3Coin2));
    }

    @Test
    public void rollbackTx() throws IOException {
        //TODO pierre
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
        //TODO pierre
        saveTx();
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + new Slice(txFromDB.serialize()));
        Assert.assertEquals(new Slice(tx.serialize()), new Slice(txFromDB.serialize()));
    }

    @Test
    public void verifyCoinDataItself() throws IOException {
        recoveryTx3Data();

        CoinData from3 = allList.get(3).getCoinData();
        from3.getFrom().get(0).setOwner("abcd3".getBytes());
        from3.getFrom().add(new Coin("abcd3.1".getBytes(), Na.parseNuls(10001), 0));
        from3.getFrom().add(new Coin("abcd3.2".getBytes(), Na.parseNuls(10001), 0));
        // 普通校验
        Result result = ledgerService.verifyCoinData(from3);
        System.out.println(result.getErrorCode().getCode());
        System.out.println(result);
        Assert.assertTrue(result.isSuccess());

        // 双花校验
        from3.getFrom().add(new Coin("abcd3.2".getBytes(), Na.parseNuls(10001), 0));
        result = ledgerService.verifyCoinData(from3);
        System.out.println(result.getErrorCode().getCode());
        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());

        // 是否可用校验
        from3.getFrom().remove(from3.getFrom().size() - 1);
        from3.getFrom().add(new Coin("abcd3.3".getBytes(), Na.parseNuls(10001), System.currentTimeMillis() + 1000 * 9));
        result = ledgerService.verifyCoinData(from3);
        System.out.println(result.getErrorCode().getCode());
        Assert.assertEquals(LedgerErrorCode.UTXO_UNUSABLE.getCode(), result.getErrorCode().getCode());

        // 是否输出大于输入校验
        from3.getFrom().remove(from3.getFrom().size() - 1);
        from3.getTo().add(new Coin("abcd3.3".getBytes(), Na.parseNuls(90001), 0));
        result = ledgerService.verifyCoinData(from3);
        System.out.println(result.getErrorCode().getCode());
        Assert.assertEquals(LedgerErrorCode.INVALID_AMOUNT.getCode(), result.getErrorCode().getCode());


    }

    @Test
    public void verifyCoinDataIsExist() throws IOException {
        allList.get(3).getCoinData().getFrom().get(0).setOwner("abcd3".getBytes());
        allList.get(4).getCoinData().getFrom().get(0).setOwner("abcd4".getBytes());

        // 存在，测试期望是成功
        CoinData coinData = new CoinData();
        coinData.getFrom().add(new Coin("abcd3".getBytes(), Na.parseNuls(10001), 0));
        Result result = ledgerService.verifyCoinData(coinData, allList);
        Assert.assertTrue(result.isSuccess());

        // 不存在，测试期望是失败 - 孤儿交易
        coinData.getFrom().get(0).setOwner("abcd3.0".getBytes());
        result = ledgerService.verifyCoinData(coinData, allList);
        Assert.assertEquals(LedgerErrorCode.ORPHAN_TX.getCode(), result.getErrorCode().getCode());

        // 不存在，测试期望是失败 - 双花交易
        ECKey ecKey1 = new ECKey();
        ECKey ecKey2 = new ECKey();
        ECKey ecKey3 = new ECKey();
        TransferTransaction tx = createTransferTransaction(ecKey1, null, ecKey2, Na.ZERO);
        byte[] txHashBytes = tx.getHash().serialize();
        coinData = tx.getCoinData();
        coinData.getFrom().get(0).setOwner(Arrays.concatenate(txHashBytes, new VarInt(0).encode()));
        result = ledgerService.saveTx(tx);
        Object object = transactionStorageService.getTxBytes(txHashBytes);
        //System.out.println("before=" + java.util.Arrays.toString(txHashBytes) + ", size=" + txHashBytes.length);
        Assert.assertTrue(result.isSuccess());
        result = ledgerService.verifyCoinData(coinData, allList);
        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
    }

    @Test
    public void verifyDoubleSpend() {
        // 无双花，测试期望是成功
        allList.get(3).getCoinData().getFrom().get(0).setOwner("abcd3".getBytes());
        allList.get(4).getCoinData().getFrom().get(0).setOwner("abcd4".getBytes());
        Result result = ledgerService.verifyDoubleSpend(allList);
        Assert.assertTrue(result.isSuccess());

        // 存在双花，测试期望是失败
        allList.get(4).getCoinData().getFrom().get(0).setOwner("abcd3".getBytes());
        result = ledgerService.verifyDoubleSpend(allList);
        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
    }

    @Test
    public void unlockTxCoinData() {
        recoveryTx3Data();

        // 有一条LockTime为-1的，测试期望是成功
        Coin coin = allList.get(3).getCoinData().getFrom().get(0);
        coin.setOwner("abcd3".getBytes());
        coin.setNa(Na.parseNuls(10001));
        coin.setLockTime(-1);
        Result result = ledgerService.unlockTxCoinData(allList.get(3));
        System.out.println(result);
        Assert.assertTrue(result.isSuccess());

        // 没有LockTime为-1的，测试期望是失败
        coin = allList.get(3).getCoinData().getFrom().get(0);
        coin.setLockTime(1000);
        result = ledgerService.unlockTxCoinData(allList.get(3));
        Assert.assertEquals(LedgerErrorCode.UTXO_STATUS_CHANGE.getCode(), result.getErrorCode().getCode());

        // LockTime既有-1的，又有不是-1的，测试期望是失败
        coin = allList.get(3).getCoinData().getFrom().get(0);
        coin.setLockTime(-1);
        CoinData coinData = allList.get(3).getCoinData();
        coinData.getFrom().add(new Coin("abcd3.1".getBytes(), Na.parseNuls(10001), 0));
        result = ledgerService.unlockTxCoinData(allList.get(3));
        Assert.assertEquals(LedgerErrorCode.UTXO_STATUS_CHANGE.getCode(), result.getErrorCode().getCode());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LevelDBManager.destroyArea(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION);
        LevelDBManager.destroyArea(LedgerStorageConstant.DB_AREA_LEDGER_UTXO);
    }

    private static void initAllList() throws NulsException, IOException {
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

        //createCancelDepositTransaction(ecKey1, NulsDigestData.fromDigestHex("ab"));

        StopAgentTransaction stop1 = createStopAgentTransaction(ecKey1, tx1.getHash());
        StopAgentTransaction stop2 = createStopAgentTransaction(ecKey1, tx2.getHash());
        StopAgentTransaction stop3 = createStopAgentTransaction(ecKey4, tx3.getHash());
        StopAgentTransaction stop4 = createStopAgentTransaction(ecKey1, tx4.getHash());
        list.add(stop1);
        list.add(stop2);
        list.add(stop3);
        list.add(stop4);
        allList = list;
    }

    private static RedPunishTransaction createRedPunishTx(ECKey ecKey, ECKey... ecKeys) {
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

    private static YellowPunishTransaction createYellowPunishTx(ECKey ecKey, ECKey... ecKeys) {
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

    private static StopAgentTransaction createStopAgentTransaction(ECKey ecKey, NulsDigestData agentTxHash) throws IOException {
        StopAgentTransaction tx = new StopAgentTransaction();
        setCommonFields(tx);
        StopAgent txData = new StopAgent();
        txData.setAddress(AddressTool.getAddress(ecKey.getPubKey()));
        txData.setCreateTxHash(agentTxHash);
        tx.setTxData(txData);
        signTransaction(tx, ecKey);
        return tx;

    }

    private static DepositTransaction createDepositTransaction(ECKey ecKey, NulsDigestData agentTxHash, Na na) throws IOException {
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

    private static CreateAgentTransaction createRegisterAgentTransaction(ECKey ecKey1, ECKey ecKey2, String agentName) throws IOException {
        CreateAgentTransaction tx = new CreateAgentTransaction();
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

    private static TransferTransaction createTransferTransaction(ECKey ecKey1, byte[] coinKey, ECKey ecKey2, Na na) throws IOException {
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

    private static CoinBaseTransaction createCoinBaseTransaction(ECKey ecKey, ECKey... ecKeys) throws IOException {
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

    private static void setCommonFields(Transaction tx) {
        tx.setTime(System.currentTimeMillis());
        tx.setBlockHeight(1);
        tx.setRemark("for test".getBytes());
    }

    private static void signTransaction(Transaction tx, ECKey ecKey) throws IOException {
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

}