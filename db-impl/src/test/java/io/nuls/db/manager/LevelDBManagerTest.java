/**
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
 */
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
    public void before() throws Exception {
        init();
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
        for(int i = 0, length = getMax() + 10; i < length; i++) {
            createArea(area + "-" + i);
        }
        Assert.assertEquals(getMax(), listArea().length);
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
        if(areas.length < getMax()) {
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
