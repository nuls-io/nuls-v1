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

package io.nuls.protocol.cache;

import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDataType;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.protocol.model.SmallBlock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/5/7
 */
public class TemporaryCacheManagerTest {

    private TemporaryCacheManager manager;

    @Before
    public void init() {
        manager = TemporaryCacheManager.getInstance();
    }

    @Test
    public void cacheSmallBlock() {
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setDataType(NulsDataType.TRANSACTION);
        BlockHeader header = new BlockHeader();
        header.setHash(NulsDigestData.calcDigestData("abcdefg".getBytes()));
        smallBlock.setHeader(header);
        manager.cacheSmallBlock(smallBlock);

        SmallBlock sb = manager.getSmallBlock(NulsDigestData.calcDigestData("abcdefg".getBytes()));
        assertEquals(sb.getDataType(),smallBlock.getDataType());
    }

    @Test
    public void getSmallBlock() {
    }

    @Test
    public void cacheTx() {
    }

    @Test
    public void getTx() {
    }

    @Test
    public void remove() {
    }

    @Test
    public void clear() {
    }

    @Test
    public void destroy() {
    }
}