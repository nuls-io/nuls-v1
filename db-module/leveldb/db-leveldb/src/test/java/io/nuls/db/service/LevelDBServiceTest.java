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
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.nuls.db.manager.LevelDBManager.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/6.
 */
public class LevelDBServiceTest {

    private static DBService dbService;

    private static String areaName = "transaction";

    private static String area;
    private static String key;

    @BeforeClass
    public static void init() {
        dbService = new LevelDBServiceImpl();
        dbService.createArea(areaName);
        area = "pierre-test";
        key = "testkey";
        createArea(area);
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

        long maxCount = 10000;
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
        long getCount = 1000;

        System.out.println("Test random performance of " + getCount + " data ···");
        for (long i = 0; i < getCount; i++) {

            long index = (long) (Math.random() * maxCount);

            byte[] resultBytes = dbService.get(areaName, ("entitySerialize" + index).getBytes(StandardCharsets.UTF_8));
            assertNotNull(resultBytes);

            DBTestEntity e = new DBTestEntity();
            try {
                e.parse(resultBytes,0);
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


        batch = dbService.createWriteBatch(area);
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


    public void snapshotOfAddress() throws Exception{
        Map<String, Na> balanceMap = new HashMap<>();
        List<byte[]> valueList = valueList("ledger_utxo");
        Coin coin;
        String strAddress;
        Na balance;
        Address address;
        byte[] hash160;
        for (byte[] bytes : valueList) {
            coin = new Coin();
            coin.parse(bytes,0);
            //
            hash160 = new byte[20];
            System.arraycopy(coin.getOwner(), 2, hash160, 0, 20);
            address = new Address(NulsContext.DEFAULT_CHAIN_ID, NulsContext.DEFAULT_ADDRESS_TYPE, hash160);
            strAddress = address.toString();
            balance = balanceMap.get(strAddress);
            if(balance == null) {
                balance = Na.ZERO;
            }
            balance = balance.add(coin.getNa());
            balanceMap.put(strAddress, balance);
        }
        Set<Map.Entry<String, Na>> entries = balanceMap.entrySet();
        //for (Map.Entry<String, Na> entry : entries) {
        //    System.out.println(entry.getKey() + "," + entry.getValue().toText());
        //}

        List<Map.Entry<String, Na>> list = new ArrayList<>();
        list.addAll(entries);
        list.sort(new Comparator<Map.Entry<String, Na>>() {
            @Override
            public int compare(Map.Entry<String, Na> o1, Map.Entry<String, Na> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (Map.Entry<String, Na> entry : list) {
            System.out.println(entry.getKey() + " " + entry.getValue().toString());
        }
    }

    @Test
    public void test() throws UnsupportedEncodingException {
        testPutModel();
        testGetModel();
        testGetModelByClass();
        testPut_1();
        testPut_2();
        testPut_3();
        testGet();
        testDelete();
        testListArea();
        testFullCreateArea();
        testDestroyArea();
        testKeySet();
        testKeyList();
        testComparator();
        testCacheSize();
        testEntrySet();
        testEntryList();
        testEntryListByClass();
        testValuesByClass();
        testValueList();
    }

    public void testFullCreateArea() {
        for (int i = 0, length = getMax() + 10; i < length; i++) {
            createArea(area + "-" + i);
        }
        Assert.assertEquals(getMax(), listArea().length);
    }

    public void testDestroyArea() {
        for (int i = 0, length = getMax() + 10; i < length; i++) {
            destroyArea(area + "-" + i);
        }
        Assert.assertTrue(listArea().length < getMax());
    }

    public void testPut_1() throws UnsupportedEncodingException {
        String value = "testvalue_1";
        put(area, key.getBytes(NulsConfig.DEFAULT_ENCODING), value.getBytes(NulsConfig.DEFAULT_ENCODING));
        String getValue = new String(get(area, bytes(key)), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testPut_2() throws UnsupportedEncodingException {
        String value = "testvalue_2";
        put(area, bytes(key), bytes(value));
        String getValue = new String(get(area, bytes(key)), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testPut_3() throws UnsupportedEncodingException {
        String value = "testvalue_3";
        put(area, bytes(key), bytes(value));
        String getValue = new String(get(area, bytes(key)), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testGet() throws UnsupportedEncodingException {
        String value = "testvalue_3";
        String getValue = new String(get(area, bytes(key)), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testDelete() throws UnsupportedEncodingException {
        delete(area, bytes(key));
        Assert.assertNull(get(area, bytes(key)));
    }

    public void testListArea() throws UnsupportedEncodingException {
        String[] areas = listArea();
        if (areas.length < getMax()) {
            String testArea = "testListArea";
            createArea(testArea);
            areas = listArea();
            boolean exist = false;
            for (String area : areas) {
                if (area.equals(testArea)) {
                    exist = true;
                    break;
                }
            }
            Assert.assertTrue("create - list areas failed.", exist);
            put(testArea, bytes(key), bytes("testListArea"));
            String getValue = new String(get(testArea, bytes(key)), NulsConfig.DEFAULT_ENCODING);
            Assert.assertEquals("testListArea", getValue);
            destroyArea(testArea);
        }
    }

    public void testPutModel() {
        DBTestEntity entity = new DBTestEntity();
        putModel(area, bytes(key), entity);
        Object object = getModel(area, bytes(key));
        Assert.assertEquals(entity.getClass().getName(), object.getClass().getName());
    }

    public void testGetModel() {
        Object object = getModel(area, bytes(key));
        Assert.assertEquals(DBTestEntity.class.getName(), object.getClass().getName());
    }

    public void testGetModelByClass() {
        DBTestEntity object = getModel(area, bytes(key), DBTestEntity.class);
        Assert.assertEquals(DBTestEntity.class.getName(), object.getClass().getName());
    }

    public void testKeySet() {
        String area = "testKeySet";
        createArea(area);
        put(area, bytes("set1"), bytes("set1value"));
        put(area, bytes("set2"), bytes("set2value"));
        put(area, bytes("set3"), bytes("set3value"));
        Set<byte[]> keys = keySet(area);
        Set<String> keysStr = new HashSet<>();
        for(byte[] bytes : keys) {
            keysStr.add(asString(bytes));
        }
        Assert.assertEquals(3, keys.size());
        Assert.assertTrue(keysStr.contains("set1"));
        Assert.assertTrue(keysStr.contains("set2"));
        Assert.assertTrue(keysStr.contains("set3"));
        destroyArea(area);
    }

    public void testKeyList() {
        String area = "testKeyList";
        createArea(area);
        put(area, bytes("set05"), bytes("set05value"));
        put(area, bytes("set06"), bytes("set06value"));
        put(area, bytes("set02"), bytes("set02value"));
        put(area, bytes("set01"), bytes("set01value"));
        put(area, bytes("set04"), bytes("set04value"));
        put(area, bytes("set03"), bytes("set03value"));
        List<byte[]> keys = keyList(area);
        Assert.assertEquals(6, keys.size());
        int i = 0;
        for (byte[] key : keys) {
            Assert.assertEquals("set0" + (++i), asString(key));
        }
        destroyArea(area);
    }

    public void testComparator() {
        String area = "testComparator";
        destroyArea(area);
        createArea(area, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] o1, byte[] o2) {
                String s1 = asString(o1);
                String s2 = asString(o2);
                if ("set03".equals(s1)) {
                    return 1;
                }
                if("set03".equals(s2)) {
                    return -1;
                }
                return s1.compareTo(s2);
            }
        });
        put(area, bytes("set05"), bytes("set05value"));
        put(area, bytes("set06"), bytes("set06value"));
        put(area, bytes("set02"), bytes("set02value"));
        put(area, bytes("set01"), bytes("set01value"));
        put(area, bytes("set04"), bytes("set04value"));
        put(area, bytes("set03"), bytes("set03value"));
        List<byte[]> keys = keyList(area);
        Assert.assertEquals(6, keys.size());
        String contactAllKeys = "";
        for (byte[] key : keys) {
            contactAllKeys += asString(key);
        }
        System.out.println(contactAllKeys);
        Assert.assertEquals("set01set02set04set05set06set03", contactAllKeys);

        Comparator comparator = getModel(getBaseAreaName(), bytes(area + "-comparator"), Comparator.class);
        Long cacheSize = getModel(getBaseAreaName(), bytes(area + "-cacheSize"), Long.class);
        destroyArea(area);
        Assert.assertTrue(createArea(area, cacheSize, comparator).isSuccess());
        put(area, bytes("set05"), bytes("set05value"));
        put(area, bytes("set06"), bytes("set06value"));
        put(area, bytes("set02"), bytes("set02value"));
        put(area, bytes("set01"), bytes("set01value"));
        put(area, bytes("set04"), bytes("set04value"));
        put(area, bytes("set03"), bytes("set03value"));
        keys = keyList(area);
        Assert.assertEquals(6, keys.size());
        contactAllKeys = "";
        for (byte[] key : keys) {
            contactAllKeys += asString(key);
        }
        System.out.println(contactAllKeys);
        Assert.assertEquals("set01set02set04set05set06set03", contactAllKeys);
        destroyArea(area);
    }


    public void testCacheSize() {
        String area = "testCacheSize";
        createArea(area, 100 * 1024 * 1024l, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] o1, byte[] o2) {
                return asString(o1).compareTo(asString(o2));
            }
        });
        put(area, bytes("set5"), bytes("set5value"));
        put(area, bytes("set6"), bytes("set6value"));
        put(area, bytes("set2"), bytes("set2value"));
        put(area, bytes("set1"), bytes("set1value"));
        put(area, bytes("set4"), bytes("set4value"));
        put(area, bytes("set3"), bytes("set3value"));
        List<Entry<byte[], byte[]>> entries = entryList(area);
        Assert.assertEquals(6, entries.size());

        int i = 1;
        for (Entry<byte[], byte[]> entry : entries) {
            Assert.assertEquals("set" + i, asString(entry.getKey()));
            Assert.assertEquals("set" + i + "value", asString(entry.getValue()));
            i++;
        }
        closeArea(area);
        Comparator comparator = getModel(getBaseAreaName(), bytes(area + "-comparator"), Comparator.class);
        Long cacheSize = getModel(getBaseAreaName(), bytes(area + "-cacheSize"), Long.class);
        Assert.assertTrue(createArea(area, cacheSize, comparator).isSuccess());
        destroyArea(area);
    }

    public void testEntrySet() {
        String area = "testEntrySet";
        createArea(area);
        put(area, bytes("set1"), bytes("set1value"));
        put(area, bytes("set2"), bytes("set2value"));
        put(area, bytes("set3"), bytes("set3value"));
        Set<Entry<byte[], byte[]>> entries = entrySet(area);
        Assert.assertEquals(3, entries.size());

        for (Entry<byte[], byte[]> entry : entries) {
            Assert.assertEquals(Iq80DBFactory.asString(entry.getValue()), asString(entry.getKey()) + "value");
            Assert.assertTrue(Iq80DBFactory.asString(entry.getValue()).startsWith(asString(entry.getKey())));
        }
        destroyArea(area);
    }

    public void testEntryList() {
        String area = "testEntryList";
        createArea(area, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] o1, byte[] o2) {
                String s1 = asString(o1);
                String s2 = asString(o2);
                if ("set3".equals(s1)) {
                    return 1;
                }
                if("set3".equals(s2)) {
                    return -1;
                }
                return s1.compareTo(s2);
            }
        });
        put(area, bytes("set5"), bytes("set5value"));
        put(area, bytes("set6"), bytes("set6value"));
        put(area, bytes("set2"), bytes("set2value"));
        put(area, bytes("set1"), bytes("set1value"));
        put(area, bytes("set4"), bytes("set4value"));
        put(area, bytes("set3"), bytes("set3value"));
        List<Entry<byte[], byte[]>> entries = entryList(area);
        Assert.assertEquals(6, entries.size());

        String contact = "";
        for (Entry<byte[], byte[]> entry : entries) {
            System.out.println(asString(entry.getKey()) + "=" + asString(entry.getValue()));
            contact += asString(entry.getKey()) + asString(entry.getValue());
        }
        Assert.assertEquals("set1set1valueset2set2valueset4set4valueset5set5valueset6set6valueset3set3value", contact);
        destroyArea(area);
    }

    public void testEntryListByClass() {
        String area = "testEntryListByClass";
        createArea(area);
        DBTestEntity entity = new DBTestEntity();
        putModel(area, bytes("entity1"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity2"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity3"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity4"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity5"), entity);
        List<Entry<byte[], DBTestEntity>> list = entryList(area, DBTestEntity.class);
        Assert.assertEquals(5, list.size());
        destroyArea(area);
    }

    public void testValuesByClass() {
        String area = "testValuesByClass";
        createArea(area);
        DBTestEntity entity = new DBTestEntity();
        putModel(area, bytes("entity1"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity2"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity3"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity4"), entity);
        entity = new DBTestEntity();
        putModel(area, bytes("entity5"), entity);
        List<DBTestEntity> list = values(area, DBTestEntity.class);
        Assert.assertEquals(5, list.size());
        destroyArea(area);
    }

    public void testValueList() {
        String area = "testValueList";
        createArea(area, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] o1, byte[] o2) {
                String s1 = asString(o1);
                String s2 = asString(o2);
                if ("set3".equals(s1)) {
                    return 1;
                }
                if("set3".equals(s2)) {
                    return -1;
                }
                return s1.compareTo(s2);
            }
        });
        put(area, bytes("set5"), bytes("set5value"));
        put(area, bytes("set6"), bytes("set6value"));
        put(area, bytes("set2"), bytes("set2value"));
        put(area, bytes("set1"), bytes("set1value"));
        put(area, bytes("set4"), bytes("set4value"));
        put(area, bytes("set3"), bytes("set3value"));
        List<byte[]> list = valueList(area);
        Assert.assertEquals(6, list.size());

        String contact = "";
        for (byte[] value : list) {
            System.out.println(asString(value));
            contact += asString(value);
        }
        Assert.assertEquals("set1valueset2valueset4valueset5valueset6valueset3value", contact);
        destroyArea(area);

        createArea(area);
        put(area, bytes("set5"), bytes("set5value"));
        put(area, bytes("set6"), bytes("set6value"));
        put(area, bytes("set2"), bytes("set2value"));
        put(area, bytes("set1"), bytes("set1value"));
        put(area, bytes("set4"), bytes("set4value"));
        put(area, bytes("set3"), bytes("set3value"));
        list = valueList(area);
        Assert.assertEquals(6, list.size());

        contact = "";
        for (byte[] value : list) {
            System.out.println(asString(value));
            contact += asString(value);
        }
        Assert.assertEquals("set1valueset2valueset3valueset4valueset5valueset6value", contact);
        destroyArea(area);
    }

    @AfterClass
    public static void after() {
        close();
    }
}