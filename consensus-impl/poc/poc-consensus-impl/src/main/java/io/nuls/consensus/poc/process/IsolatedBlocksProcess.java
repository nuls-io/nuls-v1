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

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.event.GetBlockRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by ln on 2018/4/14.
 */
public class IsolatedBlocksProcess {

    private ChainManager chainManager;

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    public IsolatedBlocksProcess(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public void process(BlockContainer blockContainer) {

        Block block = blockContainer.getBlock();

        // 只处理本地误差100个块以内的孤块
        long bestBlockHeight = chainManager.getBestBlockHeight();
        if(Math.abs(bestBlockHeight - block.getHeader().getHeight()) > 100) {
            return;
        }

        // Checks if the current orphaned block is connected to an existing orphaned chain
        // 检查当前孤立块是否和已经存在的孤立链连接
        boolean success = chainManager.checkIsBeforeIsolatedChainAndAdd(block);
        if(success) {
            // Connect to the current isolated chain and continue to find the previous block
            // 和当前的孤立链连接，继续寻找上一区块
            foundPreviousBlock(blockContainer);
            return;
        }
        success = chainManager.checkIsAfterIsolatedChainAndAdd(block);
        if(success) {
            // Successfully found and connected, no action required
            // 成功找到，并且连接上了，接下来不需要做任何操作
            return;
        }

        // Not found, then create a new isolated chain, then find the previous block
        // 没有找到，那么新建一条孤立链，接着寻找上一个区块
        chainManager.newIsolatedChain(block);

        foundPreviousBlock(blockContainer);
    }

    private void foundPreviousBlock(BlockContainer blockContainer) {

        if(blockContainer.getNode() == null) {
            Log.warn("Handling an Orphan Block Error, Unknown Source Node");
            return;
        }

        BlockHeader header = blockContainer.getBlock().getHeader();

        GetBlockRequest request = new GetBlockRequest(header.getHeight()-1, 1,
                header.getPreHash(), header.getPreHash());

        networkService.sendToNode(request, blockContainer.getNode().getId(), false);
    }
}
