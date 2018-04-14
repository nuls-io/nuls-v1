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
 */

package io.nuls.consensus.poc.manager;

import io.nuls.consensus.poc.BaseTestCase;
import io.nuls.consensus.poc.cache.CacheLoader;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.entity.Agent;
import io.nuls.consensus.poc.entity.Deposit;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by ln on 2018/4/14.
 */
public class CacheManagerTest extends BaseTestCase {

    private ChainManager chainManager;
    private CacheManager cacheManager;
    private RoundManager roundManager;

    @Before
    public void init() {
        chainManager = new ChainManager();
        roundManager = new RoundManager(chainManager);
        cacheManager = new CacheManager(chainManager);

        CacheLoader cacheLoader = new CacheLoader() {
            public List<Block> loadBlocks(int size) {
                return blockList;
            }
            public List<BlockHeader> loadBlockHeaders(int size) {
                return blockHeaderList;
            }
            public List<Agent> loadAgents() {
                return agentList;
            }
            public List<Deposit> loadDepositList() {
                return depositList;
            }
        };

        cacheManager.setCacheLoader(cacheLoader);
    }

    @Test
    public void testLoad() {
        cacheManager.load();

        List<ChainContainer> chains = chainManager.getChains();
        assert(chains.size() == 0);

        assert(chainManager.getMasterChain() != null);

        long bestHeight = chainManager.getBestBlockHeight();
        assert(bestHeight == 1l);

    }
}
