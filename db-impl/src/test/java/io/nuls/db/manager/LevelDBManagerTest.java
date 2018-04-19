package io.nuls.db.manager;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.NulsConstant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static io.nuls.db.manager.LevelDBManager.*;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class LevelDBManagerTest {

    private String area;
    private String key;
    @Before
    public void before() {
        area = "pierre-test";
        key = "testkey";
        createArea(area);
    }

    @Test
    public void test() throws UnsupportedEncodingException {
        testPut_1();
        testPut_2();
        testPut_3();
        testGet();
        testDelete();
        testListArea();
        testFullCreateArea();
    }

    public void testFullCreateArea() {
        for(int i = 0; i < 30; i++) {
            createArea(area + "-" + i);
        }
        Assert.assertEquals(MAX, listArea().length);
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
        if(areas.length < MAX) {
            String testArea = "testListArea";
            createArea(testArea);
            areas = listArea();
            boolean exist = false;
            for(String area : areas) {
                if(area.equals(testArea)) {
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

    @After
    public void after() {
        close();
    }
}
