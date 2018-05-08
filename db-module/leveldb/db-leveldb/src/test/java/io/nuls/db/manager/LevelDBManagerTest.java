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

import io.nuls.db.entity.DBTestEntity;
import io.nuls.db.model.Entry;
import io.nuls.kernel.cfg.NulsConfig;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static io.nuls.db.manager.LevelDBManager.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
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
        String getValue = new String(get(area, key), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testPut_2() throws UnsupportedEncodingException {
        String value = "testvalue_2";
        put(area, key.getBytes(NulsConfig.DEFAULT_ENCODING), value);
        String getValue = new String(get(area, key), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testPut_3() throws UnsupportedEncodingException {
        String value = "testvalue_3";
        put(area, key, value);
        String getValue = new String(get(area, key), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testGet() throws UnsupportedEncodingException {
        String value = "testvalue_3";
        String getValue = new String(get(area, key), NulsConfig.DEFAULT_ENCODING);
        Assert.assertEquals(value, getValue);
    }

    public void testDelete() throws UnsupportedEncodingException {
        delete(area, key);
        Assert.assertNull(get(area, key));
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
            put(testArea, key, "testListArea");
            String getValue = new String(get(testArea, key), NulsConfig.DEFAULT_ENCODING);
            Assert.assertEquals("testListArea", getValue);
        }
    }

    public void testPutModel() {
        DBTestEntity entity = new DBTestEntity();
        putModel(area, key, entity);
        Object object = getModel(area, key);
        Assert.assertEquals(entity.getClass().getName(), object.getClass().getName());
    }

    public void testGetModel() {
        Object object = getModel(area, key);
        Assert.assertEquals(DBTestEntity.class.getName(), object.getClass().getName());
    }

    public void testGetModelByClass() {
        DBTestEntity object = getModel(area, key, DBTestEntity.class);
        Assert.assertEquals(DBTestEntity.class.getName(), object.getClass().getName());
    }

    public void testKeySet() {
        String area = "testKeySet";
        createArea(area);
        put(area, "set1", "set1value");
        put(area, "set2", "set2value");
        put(area, "set3", "set3value");
        Set<String> keys = keySet(area);
        Assert.assertEquals(3, keys.size());
        Assert.assertTrue(keys.contains("set1"));
        Assert.assertTrue(keys.contains("set2"));
        Assert.assertTrue(keys.contains("set3"));
        destroyArea(area);
    }

    public void testKeyList() {
        String area = "testKeyList";
        createArea(area);
        put(area, "set05", "set05value");
        put(area, "set06", "set06value");
        put(area, "set02", "set02value");
        put(area, "set01", "set01value");
        put(area, "set04", "set04value");
        put(area, "set03", "set03value");
        List<String> keys = keyList(area);
        Assert.assertEquals(6, keys.size());
        int i = 0;
        for (String key : keys) {
            Assert.assertEquals("set0" + (++i), key);
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
                    return Math.abs(s1.compareTo(s2));
                }
                return s1.compareTo(s2);
            }
        });
        put(area, "set05", "set05value");
        put(area, "set06", "set06value");
        put(area, "set02", "set02value");
        put(area, "set01", "set01value");
        put(area, "set04", "set04value");
        put(area, "set03", "set03value");
        List<String> keys = keyList(area);
        Assert.assertEquals(6, keys.size());
        int i = 0;
        String contactAllKeys = "";
        for (String key : keys) {
            contactAllKeys += key;
        }
        Assert.assertEquals("set01set02set04set05set06set03", contactAllKeys);

        closeArea(area);
        DBComparator dbComparator = getModel(getBaseAreaName(), area + "-comparator", DBComparator.class);
        Long cacheSize = getModel(getBaseAreaName(), area + "-cacheSize", Long.class);
        Assert.assertTrue(createArea(area, cacheSize, dbComparator).isSuccess());
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
        put(area, "set5", "set5value");
        put(area, "set6", "set6value");
        put(area, "set2", "set2value");
        put(area, "set1", "set1value");
        put(area, "set4", "set4value");
        put(area, "set3", "set3value");
        List<Entry<String, byte[]>> entries = entryList(area);
        Assert.assertEquals(6, entries.size());

        int i = 1;
        for (Entry<String, byte[]> entry : entries) {
            Assert.assertEquals("set" + i, entry.getKey());
            Assert.assertEquals("set" + i + "value", asString(entry.getValue()));
            i++;
        }
        closeArea(area);
        DBComparator dbComparator = getModel(getBaseAreaName(), area + "-comparator", DBComparator.class);
        Long cacheSize = getModel(getBaseAreaName(), area + "-cacheSize", Long.class);
        Assert.assertTrue(createArea(area, cacheSize, dbComparator).isSuccess());
        destroyArea(area);
    }

    public void testEntrySet() {
        String area = "testEntrySet";
        createArea(area);
        put(area, "set1", "set1value");
        put(area, "set2", "set2value");
        put(area, "set3", "set3value");
        Set<Entry<String, byte[]>> entries = entrySet(area);
        Assert.assertEquals(3, entries.size());

        for (Entry<String, byte[]> entry : entries) {
            Assert.assertEquals(Iq80DBFactory.asString(entry.getValue()), entry.getKey() + "value");
            Assert.assertTrue(Iq80DBFactory.asString(entry.getValue()).startsWith(entry.getKey()));
        }
        destroyArea(area);
    }

    public void testEntryList() {
        String area = "testEntryList";
        createArea(area);
        put(area, "set5", "set5value");
        put(area, "set6", "set6value");
        put(area, "set2", "set2value");
        put(area, "set1", "set1value");
        put(area, "set4", "set4value");
        put(area, "set3", "set3value");
        List<Entry<String, byte[]>> entries = entryList(area);
        Assert.assertEquals(6, entries.size());

        int i = 1;
        for (Entry<String, byte[]> entry : entries) {
            Assert.assertEquals("set" + i, entry.getKey());
            Assert.assertEquals("set" + i + "value", asString(entry.getValue()));
            i++;
        }
        destroyArea(area);
    }

    public void testEntryListByClass() {
        String area = "testEntryListByClass";
        createArea(area);
        DBTestEntity entity = new DBTestEntity();
        putModel(area, "entity1", entity);
        entity = new DBTestEntity();
        putModel(area, "entity2", entity);
        entity = new DBTestEntity();
        putModel(area, "entity3", entity);
        entity = new DBTestEntity();
        putModel(area, "entity4", entity);
        entity = new DBTestEntity();
        putModel(area, "entity5", entity);
        List<Entry<String, DBTestEntity>> list = entryList(area, DBTestEntity.class);
        Assert.assertEquals(5, list.size());
        destroyArea(area);
    }

    public void testValuesByClass() {
        String area = "testValuesByClass";
        createArea(area);
        DBTestEntity entity = new DBTestEntity();
        putModel(area, "entity1", entity);
        entity = new DBTestEntity();
        putModel(area, "entity2", entity);
        entity = new DBTestEntity();
        putModel(area, "entity3", entity);
        entity = new DBTestEntity();
        putModel(area, "entity4", entity);
        entity = new DBTestEntity();
        putModel(area, "entity5", entity);
        List<DBTestEntity> list = values(area, DBTestEntity.class);
        Assert.assertEquals(5, list.size());
        destroyArea(area);
    }

    @After
    public void after() {
        close();
    }

}
