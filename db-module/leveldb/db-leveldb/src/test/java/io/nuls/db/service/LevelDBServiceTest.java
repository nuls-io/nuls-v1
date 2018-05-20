/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.db.service;

import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.entity.DBTestEntity;
import io.nuls.db.manager.LevelDBManager;
import io.nuls.db.model.Entry;
import io.nuls.db.service.impl.LevelDBServiceImpl;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/6.
 */
public class LevelDBServiceTest {

    private DBService dbService;

    private String areaName = "transaction";

    @Before
    public void init() {
        dbService = new LevelDBServiceImpl();
        dbService.createArea(areaName);
    }

    @After
    public void clear() {
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

    private static DBTestEntity createTransferTransaction(byte[] coinKey, Na na, long index) throws IOException {
        ECKey ecKey1 = new ECKey();
        ECKey ecKey2 = new ECKey();
        DBTestEntity tx = new DBTestEntity();
        setCommonFields(tx);
        tx.setTime(index);
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

    @Test
    public void testPerformanceTesting() throws IOException {

        long time = System.currentTimeMillis();

        long maxCount = 100000;
        for (long i = 0; i < maxCount; i++) {
            DBTestEntity entity = createTransferTransaction(null, Na.ZERO, i);

            byte[] key = ("entitySerialize" + i).getBytes(StandardCharsets.UTF_8);
            byte[] value = entity.serialize();

            Result result = dbService.put(areaName, key, value);
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        System.out.println("Save " + maxCount + " transaction time-consuming : " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        long getCount = 10000;

        System.out.println("Test random performance of " + getCount + " data ···");
        for (long i = 0; i < getCount; i++) {

            long index = (long) (Math.random() * maxCount);

            byte[] resultBytes = dbService.get(areaName, ("entitySerialize" + index).getBytes(StandardCharsets.UTF_8));
            assertNotNull(resultBytes);

            DBTestEntity e = new DBTestEntity();
            try {
                e.parse(resultBytes);
            } catch (NulsException e1) {
                Log.error(e1);
            }
            assertEquals(e.getTime(), index);
        }
//        C:\workspace\nuls_v2\db-module\leveldb\db-leveldb\target\test-classes\data\test\pierre-test-15
//        C:\workspace\nuls_v2\db-module\leveldb\db-leveldb\target\test-classes\data\test\pierre-test-15\leveldb
        System.out.println("It takes " + (System.currentTimeMillis() - time) + " ms to randomly acquire " + getCount + " data");
        LevelDBManager.destroyArea(areaName);
    }

    @Test
    public void testBatch() {
        String area = "testBatch";
        dbService.createArea(area);
        BatchOperation batch = dbService.createWriteBatch(area);
        batch.put(bytes("Tampa"), bytes("green"));
        batch.put(bytes("London"), bytes("red"));
        batch.put(bytes("London1"), bytes("red1"));
        batch.put(bytes("London2"), bytes("red2"));
        batch.put(bytes("Qweqwe"), bytes("blue"));
        batch.delete(bytes("Qweqwe"));
        batch.delete(bytes("Qwe123qwe"));
        batch.executeBatch();

        List<Entry<byte[], byte[]>> entries = dbService.entryList(area);
        entries.stream().forEach(entry -> {
            System.out.print("[" + entry.getKey() + "=" + asString(entry.getValue()) + "], ");
        });
        System.out.println();

        Assert.assertEquals("green", asString(dbService.get(area, bytes("Tampa"))));
        Assert.assertEquals("red", asString(dbService.get(area, bytes("London"))));
        Assert.assertEquals("red1", asString(dbService.get(area, bytes("London1"))));
        Assert.assertEquals("red2", asString(dbService.get(area, bytes("London2"))));
        Assert.assertNull(dbService.get(area, bytes("Qweqwe")));

        // 校验重复执行，期望失败
        Result result = batch.executeBatch();
        Assert.assertTrue(result.isFailed());
        Assert.assertEquals(DBErrorCode.DB_BATCH_CLOSE.getCode(), result.getErrorCode().getCode());
        LevelDBManager.destroyArea(area);
    }

    @Test
    public void testBatchModel() {
        String area = "testBatchModel";
        dbService.createArea(area);
        BatchOperation batch = dbService.createWriteBatch(area);
        DBTestEntity entity = new DBTestEntity();
        entity.setType(11111);
        batch.putModel(bytes("entity1"), entity);
        entity = new DBTestEntity();
        entity.setType(22222);
        batch.putModel(bytes("entity2"), entity);
        entity = new DBTestEntity();
        entity.setType(33333);
        batch.putModel(bytes("entity3"), entity);
        entity = new DBTestEntity();
        entity.setType(44444);
        batch.putModel(bytes("entity4"), entity);
        entity = new DBTestEntity();
        entity.setType(55555);
        batch.putModel(bytes("entity5"), entity);
        batch.executeBatch();
        List<DBTestEntity> list = dbService.values(area, DBTestEntity.class);
        list.stream().forEach(dbTestEntity -> {
            System.out.println("[" + dbTestEntity.toString() + "=" + dbTestEntity.getType() + "], ");
        });
        System.out.println();
        Assert.assertEquals(11111, dbService.getModel(area, bytes("entity1"), DBTestEntity.class).getType());
        Assert.assertEquals(22222, dbService.getModel(area, bytes("entity2"), DBTestEntity.class).getType());
        Assert.assertEquals(33333, dbService.getModel(area, bytes("entity3"), DBTestEntity.class).getType());
        Assert.assertEquals(44444, dbService.getModel(area, bytes("entity4"), DBTestEntity.class).getType());
        Assert.assertEquals(55555, dbService.getModel(area, bytes("entity5"), DBTestEntity.class).getType());


        batch.delete(bytes("entity4"));
        batch.delete(bytes("entity5"));
        batch.executeBatch();
        List<Entry<byte[], DBTestEntity>> entries = dbService.entryList(area, DBTestEntity.class);
        entries.stream().forEach(entry -> {
            System.out.println("[" + asString(entry.getKey()) + "=" + entry.getValue().getType() + "], ");
        });
        System.out.println();
        Assert.assertEquals(11111, dbService.getModel(area, bytes("entity1"), DBTestEntity.class).getType());
        Assert.assertEquals(22222, dbService.getModel(area, bytes("entity2"), DBTestEntity.class).getType());
        Assert.assertEquals(33333, dbService.getModel(area, bytes("entity3"), DBTestEntity.class).getType());
        Assert.assertNotNull(dbService.get(area, bytes("entity3")));
        Assert.assertNull(dbService.get(area, bytes("entity4")));
        Assert.assertNull(dbService.get(area, bytes("entity5")));
        LevelDBManager.destroyArea(area);
    }
}