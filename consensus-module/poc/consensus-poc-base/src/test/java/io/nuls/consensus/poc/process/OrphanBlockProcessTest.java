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
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.container.ChainContainer;
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
public class OrphanBlockProcessTest extends BaseChainTest {

    private OrphanBlockProcess orphanBlockProcess;
    private ChainManager chainManager;
    private OrphanBlockProvider orphanBlockProvider;

    @Before
    public void init() {
        initChain();
        chainManager = new ChainManager();
        chainManager.setMasterChain(new ChainContainer(chain));
        orphanBlockProvider = new OrphanBlockProvider();
        orphanBlockProcess = new OrphanBlockProcess(chainManager, orphanBlockProvider);
    }

    @Test
    public void testProcess() throws IOException {
        assertNotNull(orphanBlockProcess);

        assertEquals(chainManager.getOrphanChains().size() , 0);

        Block block = createBlock();
        BlockContainer blockContainer = new BlockContainer(block, BlockContainerStatus.RECEIVED);
        orphanBlockProcess.process(blockContainer);

        assertEquals(chainManager.getOrphanChains().size() , 1);


        block = createBlock();
        block.getHeader().setHeight(block.getHeader().getHeight() + PocConsensusConstant.MAX_ISOLATED_BLOCK_COUNT + 1L);
        blockContainer = new BlockContainer(block, BlockContainerStatus.RECEIVED);
        orphanBlockProcess.process(blockContainer);

        assertEquals(chainManager.getOrphanChains().size() , 1);
    }
}