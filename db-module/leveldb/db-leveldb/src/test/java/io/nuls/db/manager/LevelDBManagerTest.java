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
package io.nuls.db.manager;

import io.nuls.core.tools.crypto.Base58;
import io.nuls.db.entity.DBTestEntity;
import io.nuls.db.model.Entry;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.nuls.db.manager.LevelDBManager.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

/**
 * @desription:
 * @author: PierreLuo
 * @date:
 */
public class LevelDBManagerTest {

    private String area;
    private String key;

    @Before
    public void before() throws Exception {
        init();
        area = "pierre-test";
        key = "testkey";
        createArea(area);
    }

    public void snapshotOfAddress() throws Exception{
        Map<String, Na> balanceMap = new HashMap<>();
        List<byte[]> valueList = valueList("ledger_utxo");
        Coin coin;
        String strAddress;
        Na balance;
        for (byte[] bytes : valueList) {
            coin = new Coin();
            coin.parse(bytes);
            strAddress = Base58.encode(coin.getOwner());
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

        closeArea(area);
        Comparator comparator = getModel(getBaseAreaName(), bytes(area + "-comparator"), Comparator.class);
        Long cacheSize = getModel(getBaseAreaName(), bytes(area + "-cacheSize"), Long.class);
        Assert.assertTrue(createArea(area, cacheSize, comparator).isSuccess());
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

    @After
    public void after() {
        close();
    }

}
