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

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.BaseChainTest;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.provider.OrphanBlockProvider;
import io.nuls.kernel.model.Block;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/8.
 */
public class BlockProcessTest extends BaseChainTest {

    private BlockProcess blockProcess;
    private ChainManager chainManager;
    private OrphanBlockProvider orphanBlockProvider;

    @Before
    public void init() {
        initChain();
        chainManager = new ChainManager();
        chainManager.setMasterChain(chainContainer);
        orphanBlockProvider = new OrphanBlockProvider();
        blockProcess = new BlockProcess(chainManager, orphanBlockProvider);
    }

    @Test
    public void testAddBlock() throws IOException {

        assertNotNull(orphanBlockProvider);
        assertNotNull(blockProcess);

        assertEquals(orphanBlockProvider.size(), 0);

        Block block = createBlock();
        BlockContainer blockContainer = new BlockContainer(block, BlockContainerStatus.RECEIVED);

        boolean success = blockProcess.addBlock(blockContainer);
        assertFalse(success);

        assertEquals(orphanBlockProvider.size(), 1);

        Block bestBlock = chainManager.getBestBlock();
        Block newBlock = newBlock(bestBlock);

        success = blockProcess.addBlock(new BlockContainer(newBlock, BlockContainerStatus.RECEIVED));
        assertTrue(success);

        assertEquals(chainManager.getMasterChain().getChain().getBlockList().size(), 2);
    }
}