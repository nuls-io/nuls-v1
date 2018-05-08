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

package io.nuls.consensus.poc.provider;

import io.nuls.consensus.poc.BaseTest;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.customer.ConsensusDownloadServiceImpl;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Block;
import io.nuls.protocol.service.DownloadService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ln on 2018/5/5.
 */
public class BlockQueueProviderTest extends BaseTest {

    private BlockQueueProvider blockQueueProvider = BlockQueueProvider.getInstance();
    private ConsensusDownloadServiceImpl downloadService;

    @Before
    public void init() {
        downloadService = (ConsensusDownloadServiceImpl) SpringLiteContext.getBean(DownloadService.class);
        blockQueueProvider.clear();
    }

    @After
    public void destroy() {
        blockQueueProvider.clear();
    }

    @Test
    public void testPut() {
        assertNotNull(blockQueueProvider);

        assertEquals(0, blockQueueProvider.size());

        Block block = new Block();
        boolean result = blockQueueProvider.put(new BlockContainer(block, BlockContainerStatus.RECEIVED));
        assertTrue(result);

        assertEquals(1, blockQueueProvider.size());

    }

    @Test
    public void testGet() {
        assertNotNull(blockQueueProvider);

        assertEquals(0, blockQueueProvider.size());

        Block block = new Block();
        boolean result = blockQueueProvider.put(new BlockContainer(block, BlockContainerStatus.RECEIVED));
        assertTrue(result);

        assertEquals(1, blockQueueProvider.size());

        BlockContainer blockContainer = blockQueueProvider.get();

        assertNull(blockContainer);

        downloadService.setDownloadSuccess(true);

        blockContainer = blockQueueProvider.get();

        assertNotNull(blockContainer);

        assertEquals(blockContainer.getBlock(), block);
        assertEquals(blockContainer.getStatus(), BlockContainerStatus.DOWNLOADING);
        assertEquals(0, blockQueueProvider.size());

        block = new Block();
        result = blockQueueProvider.put(new BlockContainer(block, BlockContainerStatus.RECEIVED));
        assertTrue(result);

        blockContainer = blockQueueProvider.get();
        assertNotNull(blockContainer);
        assertEquals(blockContainer.getBlock(), block);
        assertEquals(blockContainer.getStatus(), BlockContainerStatus.RECEIVED);

    }

    @Test
    public void testSizeAndClear() {
        assertNotNull(blockQueueProvider);

        assertEquals(0, blockQueueProvider.size());

        Block block = new Block();
        boolean result = blockQueueProvider.put(new BlockContainer(block, BlockContainerStatus.RECEIVED));
        assertTrue(result);

        assertEquals(1, blockQueueProvider.size());

        blockQueueProvider.clear();

        assertEquals(0, blockQueueProvider.size());

    }
}