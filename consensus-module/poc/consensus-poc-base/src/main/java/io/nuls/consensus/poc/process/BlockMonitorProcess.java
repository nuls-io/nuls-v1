/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.service.impl.ConsensusPocServiceImpl;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.service.DownloadService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 */
public class BlockMonitorProcess {

    private final static long RESET_TIME_INTERVAL = PocConsensusConstant.RESET_SYSTEM_TIME_INTERVAL * 60 * 100000L;

    private final ChainManager chainManager;

    public BlockMonitorProcess(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    private NulsDigestData lastBestHash;

    public void doProcess() {
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        if (bestBlock.getHeader().getHeight() == 0) {
            return;
        }
        if (bestBlock.getHeader().getHash().equals(lastBestHash) && bestBlock.getHeader().getTime() < (TimeService.currentTimeMillis() - RESET_TIME_INTERVAL)) {
            lastBestHash = bestBlock.getHeader().getHash();
            NulsContext.getServiceBean(ConsensusPocServiceImpl.class).reset();
            return;
        }
        lastBestHash = bestBlock.getHeader().getHash();
        List<Block> blockList = chainManager.getMasterChain().getChain().getAllBlockList();
        int minCount = 3;
        if (blockList.size() < minCount) {
            return;
        }
        int count = 0;
        Set<String> addressSet = new HashSet<>();
        for (int i = blockList.size() - 1; i >= 0; i--) {
            Block block = blockList.get(i);
            addressSet.add(block.getHeader().getPackingAddressStr());
            count++;
            if (count > minCount) {
                break;
            }
        }
        DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);
        if (count > minCount && addressSet.size() == 1 && ConsensusConfig.getSeedNodeList().size() > 1) {
//            NulsContext.getServiceBean(ConsensusPocServiceImpl.class).reset();
            return;
        }
        if (downloadService.isDownloadSuccess().isSuccess() &&
                bestBlock.getHeader().getTime() < (TimeService.currentTimeMillis() - RESET_TIME_INTERVAL)) {
//            NulsContext.getServiceBean(ConsensusPocServiceImpl.class).reset();
        }
    }
}
