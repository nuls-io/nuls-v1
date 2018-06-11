/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.service.impl;

import io.nuls.consensus.poc.protocol.entity.*;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;
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

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/14
 */
public class UtxoLedgerServiceImplTest {

    private List<Transaction> allList;
    private List<Transaction> txList;

    private static LedgerService ledgerService;
    private static UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;
    private static UtxoLedgerTransactionStorageService utxoLedgerTransactionStorageService;

    @BeforeClass
    public static void setUp() throws Exception {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();

        Block block = new Block();
        BlockHeader header = new BlockHeader();
        header.setHeight(1);
        block.setHeader(header);
        NulsContext.getInstance().setBestBlock(block);

        LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
        bootstrap.init();
        bootstrap.start();

        ledgerService = SpringLiteContext.getBean(LedgerService.class);
        utxoLedgerUtxoStorageService = SpringLiteContext.getBean(UtxoLedgerUtxoStorageService.class);
        utxoLedgerTransactionStorageService = SpringLiteContext.getBean(UtxoLedgerTransactionStorageService.class);

    }
    @Before
    public void start() throws IOException, NulsException {
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
    public void testGetWholeUTXO() throws NulsException, IOException {
        LevelDBManager.destroyArea(LedgerStorageConstant.DB_NAME_LEDGER_TX);
        LevelDBManager.destroyArea(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
        LevelDBManager.createArea(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
        LevelDBManager.createArea(LedgerStorageConstant.DB_NAME_LEDGER_TX);
        Transaction tx8 = allList.get(8);

        ECKey ecKeyPre0 = new ECKey();
        ECKey ecKey0 = new ECKey();
        ECKey ecKey1 = new ECKey();
        ECKey ecKey2 = new ECKey();

        TransferTransaction prePrePreTx = createTransferTransaction(ecKeyPre0, null, ecKey0, Na.ZERO);
        TransferTransaction prePreTx = createTransferTransaction(ecKey0, null, ecKey1, Na.ZERO);
        TransferTransaction preTx = createTransferTransaction(ecKey1, null, ecKey2, Na.ZERO);

        P2PKHScriptSig preTxScript = P2PKHScriptSig.createFromBytes(preTx.getScriptSig());
        P2PKHScriptSig txScript = P2PKHScriptSig.createFromBytes(tx8.getScriptSig());

        byte[] prePreTxHashBytes = prePreTx.getHash().serialize();
        byte[] preTxHashBytes = preTx.getHash().serialize();

        CoinData prePreTxCoinData = prePreTx.getCoinData();
        prePreTxCoinData.getFrom().clear();
        prePreTxCoinData.getTo().clear();
        prePreTxCoinData.getFrom().add(new Coin(Arrays.concatenate(prePrePreTx.getHash().serialize(), new VarInt(0).encode()), Na.parseNuls(30031), 0));
        prePreTxCoinData.getTo().add(new Coin(AddressTool.getAddress(preTxScript.getPublicKey()), Na.parseNuls(30031), 0));
        ledgerService.saveTx(prePreTx);

        CoinData preTxCoinData = preTx.getCoinData();
        preTxCoinData.getFrom().clear();
        preTxCoinData.getTo().clear();
        preTxCoinData.getFrom().add(new Coin(Arrays.concatenate(prePreTxHashBytes, new VarInt(0).encode()), Na.parseNuls(30031), 0));
        preTxCoinData.getTo().add(new Coin(AddressTool.getAddress(txScript.getPublicKey()), Na.parseNuls(10001), 0));
        preTxCoinData.getTo().add(new Coin(AddressTool.getAddress(txScript.getPublicKey()), Na.parseNuls(10001), 0));
        preTxCoinData.getTo().add(new Coin(AddressTool.getAddress(txScript.getPublicKey()), Na.parseNuls(10001), 0));

        Result result = ledgerService.saveTx(preTx);
        Assert.assertTrue(result.isSuccess());
        long total = ledgerService.getWholeUTXO();
        Assert.assertEquals(Na.parseNuls(10001 + 10001 + 10001).getValue(), total);
        result = ledgerService.rollbackTx(preTx);
        total = ledgerService.getWholeUTXO();
        Assert.assertEquals(Na.parseNuls(30031).getValue(), total);


    }

    @Test
    public void saveTx() throws IOException, NulsException {
        // 无from的交易
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Result result = ledgerService.saveTx(tx);
        System.out.println(result);
        Assert.assertTrue(result.isSuccess());
        byte[] toCoin = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(tx.getHash().serialize(), new VarInt(0).encode()));
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
        byte[] to3Coin0 = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(0).encode()));
        byte[] to3Coin1 = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(1).encode()));
        byte[] to3Coin2 = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(2).encode()));
        Assert.assertNotNull(to3Coin0);
        Assert.assertNotNull(to3Coin1);
        Assert.assertNotNull(to3Coin2);
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(0).serialize()), new Slice(to3Coin0));
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(1).serialize()), new Slice(to3Coin1));
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(2).serialize()), new Slice(to3Coin2));
    }

    @Test
    public void getTx() throws IOException, NulsException {
        saveTx();
        Transaction tx = allList.get(0);
        System.out.println("tx: " + new Slice(tx.serialize()));
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + new Slice(txFromDB.serialize()));
        Assert.assertEquals(new Slice(tx.serialize()), new Slice(txFromDB.serialize()));

        Transaction tx3 = allList.get(3);
        System.out.println("tx3: " + new Slice(tx3.serialize()));
        Transaction tx3FromDB = ledgerService.getTx(tx3.getHash());
        System.out.println("tx3FromDB: " + new Slice(tx3FromDB.serialize()));
        Assert.assertEquals(new Slice(tx3.serialize()), new Slice(tx3FromDB.serialize()));

    }

    @Test
    public void rollbackTx() throws IOException, NulsException {
        recoveryTx3Data();
        Transaction tx03 = allList.get(3);
        Transaction preTx = initTxDataForTestVerifyCoinData(tx03);
        byte[] preTxHashBytes = preTx.getHash().serialize();

        // 无from的交易
        Transaction tx = allList.get(0);
        Result result = ledgerService.saveTx(tx);
        Assert.assertTrue(result.isSuccess());
        result = ledgerService.rollbackTx(tx);
        Assert.assertTrue(result.isSuccess());
        byte[] toCoin = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(tx.getHash().serialize(), new VarInt(0).encode()));
        Assert.assertNull(toCoin);
        Transaction txFromDB = ledgerService.getTx(tx.getHash());
        System.out.println("txFromDB: " + txFromDB);
        Assert.assertNull(txFromDB);

        // 有from的交易, 检验交易和coindata
        CoinData from3 = preTx.getCoinData();
        result = ledgerService.rollbackTx(preTx);
        Assert.assertTrue(result.isSuccess());
        byte[] to3Coin0 = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(preTx.getHash().serialize(), new VarInt(0).encode()));
        byte[] to3Coin1 = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(preTx.getHash().serialize(), new VarInt(1).encode()));
        byte[] to3Coin2 = utxoLedgerUtxoStorageService.getUtxoBytes(Arrays.concatenate(preTx.getHash().serialize(), new VarInt(2).encode()));
        Assert.assertNull(to3Coin0);
        Assert.assertNull(to3Coin1);
        Assert.assertNull(to3Coin2);
        byte[] from3Coin0 = utxoLedgerUtxoStorageService.getUtxoBytes(from3.getFrom().get(0).getOwner());
        Assert.assertNotNull(from3Coin0);
        P2PKHScriptSig p2PKHScriptSig = P2PKHScriptSig.createFromBytes(preTx.getScriptSig());
        byte[] fromAdress = AddressTool.getAddress(p2PKHScriptSig.getPublicKey());
        preTx.getCoinData().getFrom().get(0).setOwner(fromAdress);
        Assert.assertEquals(new Slice(preTx.getCoinData().getFrom().get(0).serialize()), new Slice(from3Coin0));
    }

    private Transaction initTxDataForTestVerifyCoinData(Transaction tx) throws NulsException, IOException {
        allList.remove(3);
        ECKey ecKeyPre0 = new ECKey();
        ECKey ecKey0 = new ECKey();
        ECKey ecKey1 = new ECKey();
        ECKey ecKey2 = new ECKey();
        TransferTransaction prePrePreTx = createTransferTransaction(ecKeyPre0, null, ecKey0, Na.ZERO);
        TransferTransaction prePreTx = createTransferTransaction(ecKey0, null, ecKey1, Na.ZERO);
        TransferTransaction preTx = createTransferTransaction(ecKey1, null, ecKey2, Na.ZERO);
        P2PKHScriptSig preTxScript = P2PKHScriptSig.createFromBytes(preTx.getScriptSig());
        P2PKHScriptSig txScript = P2PKHScriptSig.createFromBytes(tx.getScriptSig());
        //byte[] user = p2PKHScriptSig.getSignerHash160();
        //byte[] owner0 = new byte[23];
        //owner0[0] = (byte) 001;
        //owner0[1] = (byte) 002;
        //owner0[22] = (byte) 003;
        //System.arraycopy(user, 0, owner0, 2, 20);
        //byte[] owner1 = new byte[23];
        //owner1[0] = (byte) 101;
        //owner1[1] = (byte) 102;
        //owner1[22] = (byte) 103;
        //System.arraycopy(user, 0, owner1, 2, 20);
        //byte[] owner2 = new byte[23];
        //owner2[0] = (byte) 201;
        //owner2[1] = (byte) 202;
        //owner2[22] = (byte) 203;
        //System.arraycopy(user, 0, owner2, 2, 20);
        byte[] prePreTxHashBytes = prePreTx.getHash().serialize();
        byte[] preTxHashBytes = preTx.getHash().serialize();
        //utxoLedgerUtxoStorageService.saveUtxo(Arrays.concatenate(prePreTxHashBytes, new VarInt(0).encode()),
        //        new Coin(AddressTool.getAddress(preTxScript.getPublicKey()), Na.parseNuls(30031), 0));

        //byte[] user = SerializeUtils.sha256hash160(new ECKey().getPubKey());
        //byte[] owner3 = new byte[23];
        //owner3[0] = (byte) 31;
        //owner3[1] = (byte) 32;
        //owner3[22] = (byte) 33;
        //System.arraycopy(user, 0, owner3, 2, 20);

        CoinData prePreTxCoinData = prePreTx.getCoinData();
        prePreTxCoinData.getFrom().clear();
        prePreTxCoinData.getTo().clear();
        prePreTxCoinData.getFrom().add(new Coin(Arrays.concatenate(prePrePreTx.getHash().serialize(), new VarInt(0).encode()), Na.parseNuls(30031), 0));
        prePreTxCoinData.getTo().add(new Coin(AddressTool.getAddress(preTxScript.getPublicKey()), Na.parseNuls(30031), 0));
        ledgerService.saveTx(prePreTx);

        CoinData preTxCoinData = preTx.getCoinData();
        preTxCoinData.getFrom().clear();
        preTxCoinData.getTo().clear();
        preTxCoinData.getFrom().add(new Coin(Arrays.concatenate(prePreTxHashBytes, new VarInt(0).encode()), Na.parseNuls(30031), 0));
        preTxCoinData.getTo().add(new Coin(AddressTool.getAddress(txScript.getPublicKey()), Na.parseNuls(10001), 0));
        preTxCoinData.getTo().add(new Coin(AddressTool.getAddress(txScript.getPublicKey()), Na.parseNuls(10001), 0));
        preTxCoinData.getTo().add(new Coin(AddressTool.getAddress(txScript.getPublicKey()), Na.parseNuls(10001), 0));
        ledgerService.saveTx(preTx);
        //utxoLedgerUtxoStorageService.saveUtxo(preTxCoinData.getFrom().get(0).getOwner(), new Coin(owner0, Na.parseNuls(10001), 0));
        //utxoLedgerUtxoStorageService.saveUtxo(preTxCoinData.getFrom().get(1).getOwner(), new Coin(owner1, Na.parseNuls(10001), 0));
        //utxoLedgerUtxoStorageService.saveUtxo(preTxCoinData.getFrom().get(2).getOwner(), new Coin(owner2, Na.parseNuls(10001), 0));

        tx.getCoinData().getFrom().clear();
        tx.getCoinData().getTo().clear();
        tx.getCoinData().getFrom().add(new Coin(Arrays.concatenate(preTxHashBytes, new VarInt(0).encode()), Na.parseNuls(10001), 0));
        tx.getCoinData().getFrom().add(new Coin(Arrays.concatenate(preTxHashBytes, new VarInt(1).encode()), Na.parseNuls(10001), 0));
        tx.getCoinData().getFrom().add(new Coin(Arrays.concatenate(preTxHashBytes, new VarInt(2).encode()), Na.parseNuls(10001), 0));
        tx.getCoinData().getTo().add(new Coin(AddressTool.getAddress(ecKey2.getPubKey()), Na.parseNuls(30001), 0));
        return preTx;

    }


    @Test
    public void verifyDoubleSpend() {
        // 无双花，测试期望是成功
        allList.get(3).getCoinData().getFrom().get(0).setOwner("abcd3".getBytes());
        allList.get(3).getCoinData().getFrom().get(0).setLockTime(3);
        allList.get(4).getCoinData().getFrom().get(0).setOwner("abcd4".getBytes());
        allList.get(4).getCoinData().getFrom().get(0).setLockTime(4);
        ValidateResult result = ledgerService.verifyDoubleSpend(allList);
        Assert.assertTrue(result.isSuccess());

        // 存在双花，测试期望是失败
        allList.get(4).getCoinData().getFrom().get(0).setOwner("abcd3".getBytes());
        ValidateResult<List<Transaction>> validateResult = ledgerService.verifyDoubleSpend(allList);
        List<Transaction> resultList = validateResult.getData();
        Assert.assertNotNull(resultList);
        Assert.assertEquals(2, resultList.size());
        Assert.assertEquals(7, resultList.get(0).getCoinData().getFrom().get(0).getLockTime() + resultList.get(1).getCoinData().getFrom().get(0).getLockTime());

        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), validateResult.getErrorCode().getCode());
    }

    @Test
    public void unlockTxCoinData() throws IOException, NulsException {
        recoveryTx3Data();

        // 有一条LockTime为-1的，测试期望是成功
        Transaction tx3 = allList.get(3);
        Coin coin = allList.get(3).getCoinData().getTo().get(0);
        coin.setOwner("abcd3".getBytes());
        coin.setNa(Na.parseNuls(10001));
        coin.setLockTime(-1);
        Result result = ledgerService.unlockTxCoinData(allList.get(3), 123);
        //System.out.println(result);
        Assert.assertTrue(result.isSuccess());
        Coin to3Coin0 = utxoLedgerUtxoStorageService.getUtxo(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(0).encode()));
        Assert.assertEquals(123, to3Coin0.getLockTime());
        to3Coin0.setLockTime(-1);
        Assert.assertNotNull(to3Coin0);
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(0).serialize()), new Slice(to3Coin0.serialize()));

        // 回滚
        result = ledgerService.rollbackUnlockTxCoinData(allList.get(3));
        Assert.assertTrue(result.isSuccess());
        to3Coin0 = utxoLedgerUtxoStorageService.getUtxo(Arrays.concatenate(tx3.getHash().serialize(), new VarInt(0).encode()));
        Assert.assertNotNull(to3Coin0);
        Assert.assertEquals(-1, to3Coin0.getLockTime());
        Assert.assertEquals(new Slice(tx3.getCoinData().getTo().get(0).serialize()), new Slice(to3Coin0.serialize()));



        // 没有LockTime为-1的，测试期望是失败
        coin = allList.get(3).getCoinData().getTo().get(0);
        coin.setLockTime(1000);
        result = ledgerService.unlockTxCoinData(allList.get(3), 123);
        Assert.assertEquals(LedgerErrorCode.UTXO_STATUS_CHANGE.getCode(), result.getErrorCode().getCode());

        // LockTime既有-1的，又有不是-1的，测试期望是成功
        coin = allList.get(3).getCoinData().getTo().get(0);
        coin.setLockTime(-1);
        CoinData coinData = allList.get(3).getCoinData();
        coinData.getTo().add(new Coin("abcd3.1".getBytes(), Na.parseNuls(10001), 0));
        result = ledgerService.unlockTxCoinData(allList.get(3), 123);
        Assert.assertTrue(result.isSuccess());
    }


//    @Test
//    public void verifyCoinData() throws IOException, NulsException {
//        recoveryTx3Data();
//        Transaction tx3 = allList.get(3);
//        Transaction preTx = initTxDataForTestVerifyCoinData(tx3);
//        byte[] preTxHashBytes = preTx.getHash().serialize();
//
//        CoinData from3 = tx3.getCoinData();
//        P2PKHScriptSig p2PKHScriptSig = P2PKHScriptSig.createFromBytes(tx3.getScriptSig());
//        byte[] user = p2PKHScriptSig.getSignerHash160();
//        byte[] owner0 = from3.getFrom().get(0).getOwner();
//        byte[] owner2 = from3.getFrom().get(2).getOwner();
//        byte[] owner3 = new byte[23];
//        owner3[0] = (byte) 31;
//        owner3[1] = (byte) 32;
//        owner3[22] = (byte) 33;
//        System.arraycopy(user, 0, owner3, 2, 20);
//
//        allList.get(3).getCoinData().getFrom().get(0).setOwner(owner3);
//
//        // 普通校验
//        Result result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertTrue(result.isSuccess());
//
//        // txList为空的校验
//        result = ledgerService.verifyCoinData(tx3, null, null);
//        System.out.println(result);
//        Assert.assertTrue(result.isSuccess());
//
//        // 双花校验 - 自身双花
//        from3.getFrom().add(new Coin(owner2, Na.parseNuls(10001), 0));
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(tx3, null, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
//
//        // 双花校验 - 验证与待确认交易列表中是否有双花
//        from3.getFrom().remove(from3.getFrom().size() - 1);
//        allList.get(3).getCoinData().getFrom().get(0).setOwner(owner2);
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
//        allList.get(3).getCoinData().getFrom().get(0).setOwner(owner3);
//
//
//        // 非解锁交易，是否可用校验
//        from3.getFrom().add(new Coin(Arrays.concatenate(preTxHashBytes, new VarInt(3).encode()), Na.parseNuls(10001), System.currentTimeMillis() + 1000 * 9));
//        utxoLedgerUtxoStorageService.saveUtxo(from3.getFrom().get(3).getOwner(), new Coin(AddressTool.getAddress(p2PKHScriptSig.getPublicKey()), Na.parseNuls(10001), System.currentTimeMillis() + 1000 * 9));
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.UTXO_UNUSABLE.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(tx3, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.UTXO_UNUSABLE.getCode(), result.getErrorCode().getCode());
//        utxoLedgerUtxoStorageService.deleteUtxo(from3.getFrom().get(3).getOwner());
//
//        // 解锁交易，状态校验，期望成功
//        CancelDepositTransaction txCancel = new CancelDepositTransaction();
//        ECKey ecKey1 = new ECKey();
//        this.setCommonFields(txCancel);
//        CancelDeposit txCancelData = new CancelDeposit();
//        txCancelData.setAddress(AddressTool.getAddress(ecKey1.getPubKey()));
//        txCancelData.setJoinTxHash(NulsDigestData.calcDigestData("1234567890".getBytes()));
//        this.signTransaction(txCancel, ecKey1);
//        P2PKHScriptSig p2PKHScriptSigCancel = P2PKHScriptSig.createFromBytes(txCancel.getScriptSig());
//        byte[] userCancel = p2PKHScriptSigCancel.getSignerHash160();
//        byte[] ownerCancel = new byte[23];
//        ownerCancel[0] = (byte) 001;
//        ownerCancel[1] = (byte) 002;
//        ownerCancel[22] = (byte) 003;
//        System.arraycopy(userCancel, 0, ownerCancel, 2, 20);
//        CoinData fromCancel = txCancel.getCoinData();
//        fromCancel = new CoinData();
//        txCancel.setCoinData(fromCancel);
//        fromCancel.getFrom().add(new Coin(ownerCancel, Na.parseNuls(10001), -1));
//        fromCancel.getTo().add(new Coin(ownerCancel, Na.parseNuls(10000), 0));
//        utxoLedgerUtxoStorageService.saveUtxo(fromCancel.getFrom().get(0).getOwner(), fromCancel.getFrom().get(0));
//        result = ledgerService.verifyCoinData(txCancel, allList);
//        System.out.println(result);
//        Assert.assertTrue(result.isSuccess());
//
//        result = ledgerService.verifyCoinData(txCancel, null, null);
//        System.out.println(result);
//        Assert.assertTrue(result.isSuccess());
//
//
//        // 解锁交易，状态校验，期望失败
//        fromCancel.getFrom().get(0).setLockTime(0);
//        utxoLedgerUtxoStorageService.saveUtxo(fromCancel.getFrom().get(0).getOwner(), fromCancel.getFrom().get(0));
//        result = ledgerService.verifyCoinData(txCancel, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.UTXO_STATUS_CHANGE.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(txCancel, null, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.UTXO_STATUS_CHANGE.getCode(), result.getErrorCode().getCode());
//
//
//
//        // 是否输出大于输入校验
//        from3.getFrom().remove(from3.getFrom().size() - 1);
//        ECKey ecKey2 = new ECKey();
//        from3.getTo().add(new Coin(AddressTool.getAddress(ecKey2.getPubKey()), Na.parseNuls(90001), 0));
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.INVALID_AMOUNT.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(tx3, null, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.INVALID_AMOUNT.getCode(), result.getErrorCode().getCode());
//
//
//        // 验证utxo是否属于交易发出者
//        from3.getTo().remove(from3.getTo().size() - 1);
//        // save 一笔 UTXO
//        byte[] owner = AddressTool.getAddress(p2PKHScriptSig.getPublicKey());
//        owner[2] = (byte) (owner[2] - 1);
//        from3.getFrom().add(new Coin(Arrays.concatenate(preTxHashBytes, new VarInt(3).encode()), Na.parseNuls(10001), 0));
//        utxoLedgerUtxoStorageService.saveUtxo(from3.getFrom().get(3).getOwner(), new Coin(owner, Na.parseNuls(10001), 0));
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.INVALID_INPUT.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(tx3, null, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.INVALID_INPUT.getCode(), result.getErrorCode().getCode());
//        utxoLedgerUtxoStorageService.deleteUtxo(from3.getFrom().get(3).getOwner());
//
//
//        // 是否可花费，查数据库中或者txList中是否存在UTXO，不在toList又不在数据库中，但存在这笔交易，测试期望是失败 - 双花交易
//        from3.getFrom().remove(from3.getFrom().size() - 1);
//        Coin utxo = utxoLedgerUtxoStorageService.getUtxo(from3.getFrom().get(0).getOwner());
//        utxoLedgerUtxoStorageService.deleteUtxo(from3.getFrom().get(0).getOwner());
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(tx3, null, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.LEDGER_DOUBLE_SPENT.getCode(), result.getErrorCode().getCode());
//        utxoLedgerUtxoStorageService.saveUtxo(from3.getFrom().get(0).getOwner(), utxo);
//
//
//        // 是否可花费，查数据库中或者txList中是否存在UTXO，在数据库中不存在而在toList中存在，测试期望是成功
//        ledgerService.rollbackTx(preTx);
//        allList.add(preTx);
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertTrue(result.isSuccess());
//        ledgerService.saveTx(preTx);
//        allList.remove(allList.size() - 1);
//
//
//        // 是否可花费，查数据库中或者txList中是否存在UTXO，不在toList又不在数据库中，不存在这笔交易，测试期望是失败 - 孤儿交易
//        // 数据库中删除保存的三笔UTXO
//        ECKey ecKeyPre0 = new ECKey();
//        ECKey ecKey0 = new ECKey();
//        TransferTransaction prePrePreTx = createTransferTransaction(ecKeyPre0, null, ecKey0, Na.ZERO);
//        from3.getFrom().add(new Coin(Arrays.concatenate(prePrePreTx.getHash().serialize(), new VarInt(3).encode()), Na.parseNuls(10001), 0));
//        result = ledgerService.verifyCoinData(tx3, allList);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.ORPHAN_TX.getCode(), result.getErrorCode().getCode());
//
//        result = ledgerService.verifyCoinData(tx3, null, null);
//        System.out.println(result);
//        Assert.assertEquals(LedgerErrorCode.ORPHAN_TX.getCode(), result.getErrorCode().getCode());
//        from3.getFrom().remove(from3.getFrom().size() - 1);
//
//        boolean flag = true;
//        if(flag) {
//            return;
//        }
//    }

    @AfterClass
    public static void tearDown() throws Exception {
        LevelDBManager.destroyArea(LedgerStorageConstant.DB_NAME_LEDGER_TX);
        LevelDBManager.destroyArea(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
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
        Transaction yellowPunishTx1 = createYellowPunishTx(ecKey1, ecKey2, ecKey3, ecKey4, ecKey5, ecKey6);
        list.add(yellowPunishTx1);

        //RedPunishTransaction redPunishTransaction = createRedPunishTx(ecKey1, ecKey4, ecKey5, ecKey6);
        //list.add(redPunishTransaction);

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

//    private static RedPunishTransaction createRedPunishTx(ECKey ecKey, ECKey... ecKeys) {
//        RedPunishTransaction tx = new RedPunishTransaction();
//        setCommonFields(tx);
//        RedPunishData data = new RedPunishData();
//        data.setAddress(AddressTool.getAddress(ecKeys[0].getPubKey()));
//        data.setEvidence("for test".getBytes());
//        data.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
//        tx.setTxData(data);
//        return tx;
//    }

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