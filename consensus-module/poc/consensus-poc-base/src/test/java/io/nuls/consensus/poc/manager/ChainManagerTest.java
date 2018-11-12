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

package io.nuls.consensus.poc.manager;

import io.nuls.consensus.poc.BaseTest;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.model.Chain;
import io.nuls.kernel.model.Block;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/8.
 */
public class ChainManagerTest extends BaseTest {

    private ChainManager chainManager;

    @Before
    public void init() {
        chainManager = new ChainManager();
    }

    @Test
    public void testNewOrphanChain() {

        assertNotNull(chainManager);

        Block block = createBlock();
        chainManager.newOrphanChain(block);

        assertEquals(1, chainManager.getOrphanChains().size());
    }

    @Test
    public void testGetBestBlockHeight() {
        assertNotNull(chainManager);

        Block block = createBlock();

        ChainContainer masterChain = new ChainContainer(new Chain());
        chainManager.setMasterChain(masterChain);
        masterChain.getChain().addBlock(block);

        assertEquals(0L, chainManager.getBestBlockHeight());
    }

    @Test
    public void testCheckIsBeforeOrphanChainAndAdd() {

        testGetBestBlockHeight();

        Block block = createBlock();

        Block block1 = createBlock();
        block1.getHeader().setHeight(1L);
        block1.getHeader().setPreHash(block.getHeader().getHash());

        ChainContainer orphanChain = new ChainContainer(new Chain());
        orphanChain.getChain().addBlock(block1);

        chainManager.getOrphanChains().add(orphanChain);

        assertEquals(1, chainManager.getOrphanChains().size());


        boolean success = chainManager.checkIsBeforeOrphanChainAndAdd(block);

        assertTrue(success);
    }

    @Test
    public void testCheckIsAfterOrphanChainAndAdd() {

        testGetBestBlockHeight();

        Block block = createBlock();

        Block block1 = createBlock();
        block1.getHeader().setHeight(1L);
        block1.getHeader().setPreHash(block.getHeader().getHash());

        ChainContainer orphanChain = new ChainContainer(new Chain());
        orphanChain.getChain().addBlock(block);

        chainManager.getOrphanChains().add(orphanChain);

        assertEquals(1, chainManager.getOrphanChains().size());


        boolean success = chainManager.checkIsAfterOrphanChainAndAdd(block1);

        assertTrue(success);
    }
}