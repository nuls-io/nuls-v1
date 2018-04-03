/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.thread;

import io.nuls.consensus.constant.MaintenanceStatus;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.block.BestCorrectBlock;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.BlockBatchDownloadUtils;
import io.nuls.consensus.utils.BlockInfo;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.LedgerService;
import sun.applet.Main;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread {

    public static DistributedBlockInfoRequestUtils BEST_HEIGHT_FROM_NET = DistributedBlockInfoRequestUtils.getInstance();

    public static final String THREAD_NAME = "block-maintenance";

    private static BlockMaintenanceThread instance = new BlockMaintenanceThread();
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private final BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private MaintenanceStatus status = MaintenanceStatus.FAILED;
    private long downloadHeight = 0L;

    private BlockMaintenanceThread() {
    }

    public static synchronized BlockMaintenanceThread getInstance() {
        return instance;
    }

    public synchronized void syncBlock() {
        this.status = MaintenanceStatus.DOWNLOADING;
        BestCorrectBlock bestCorrectBlock = checkLocalBestCorrentBlock();
        boolean doit = false;
        long startHeight = 1;
        do {
            if (null == bestCorrectBlock.getLocalBestBlock() && bestCorrectBlock.getNetBestBlockInfo() == null) {
                doit = true;
                BlockInfo blockInfo = BEST_HEIGHT_FROM_NET.request(-1);
                bestCorrectBlock.setNetBestBlockInfo(blockInfo);
                break;
            }
            startHeight = bestCorrectBlock.getLocalBestBlock().getHeader().getHeight() + 1;
            long interval = TimeService.currentTimeMillis() - bestCorrectBlock.getLocalBestBlock().getHeader().getTime();
            if (interval < (PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 2000)) {
                doit = false;
                break;
            }
            if (null == bestCorrectBlock.getNetBestBlockInfo()) {
                bestCorrectBlock.setNetBestBlockInfo(BEST_HEIGHT_FROM_NET.request(0));
            }
            if (null == bestCorrectBlock.getNetBestBlockInfo()) {
                break;
            }
            if (bestCorrectBlock.getNetBestBlockInfo().getBestHeight() > bestCorrectBlock.getLocalBestBlock().getHeader().getHeight()) {
                doit = true;
                break;
            }
        } while (false);
        if (null == bestCorrectBlock.getNetBestBlockInfo()) {
            return;
        }
        if (doit) {
            downloadBlocks(bestCorrectBlock.getNetBestBlockInfo().getNodeIdList(), startHeight, bestCorrectBlock.getNetBestBlockInfo().getBestHeight());
        } else {
            this.status = MaintenanceStatus.SUCCESS;
        }
    }


    private void downloadBlocks(List<String> nodeIdList, long startHeight, long endHeight) {
        BlockBatchDownloadUtils utils = BlockBatchDownloadUtils.getInstance();
        this.downloadHeight = endHeight;
        try {
            utils.request(nodeIdList, startHeight, endHeight);
        } catch (InterruptedException e) {
            Log.error(e);
        }
    }

    public void checkGenesisBlock() throws Exception {
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        ValidateResult result = genesisBlock.verify();
        if (result.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, result.getMessage());
        }
        Block localGenesisBlock = this.blockService.getGengsisBlock();
        if (null == localGenesisBlock) {
            for (Transaction tx : genesisBlock.getTxs()) {
                ledgerService.approvalTx(tx);
            }
            this.blockService.saveBlock(genesisBlock);
            return;
        }
        localGenesisBlock.verify();
        String logicHash = genesisBlock.getHeader().getHash().getDigestHex();
        String localHash = localGenesisBlock.getHeader().getHash().getDigestHex();
        if (!logicHash.equals(localHash)) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
    }

    private BestCorrectBlock checkLocalBestCorrentBlock() {
        BestCorrectBlock resultCorrentInfo = new BestCorrectBlock();
        Block localBestBlock = this.blockService.getLocalBestBlock();
        do {
            if (null == localBestBlock || localBestBlock.getHeader().getHeight() <= 1) {
                break;
            }
            BlockInfo netBestBlockInfo = DistributedBlockInfoRequestUtils.getInstance().request(0);
            resultCorrentInfo.setNetBestBlockInfo(netBestBlockInfo);
            if (null == netBestBlockInfo || netBestBlockInfo.getBestHash() == null) {
                break;
            }
            //same to network nodes
            if (netBestBlockInfo.getBestHeight() == localBestBlock.getHeader().getHeight() &&
                    netBestBlockInfo.getBestHash().equals(localBestBlock.getHeader().getHash())) {
                break;
            } else if (netBestBlockInfo.getBestHeight() <= localBestBlock.getHeader().getHeight()) {
                if (netBestBlockInfo.getBestHeight() == 0) {
                    break;
                }
                //local height is highest
                BlockHeader header = null;
                try {
                    header = blockService.getBlockHeader(netBestBlockInfo.getBestHeight());
                } catch (NulsException e) {
                    break;
                }

                if (null != header && header.getHash().equals(netBestBlockInfo.getBestHash())) {
                    break;
                }
                if (netBestBlockInfo.getNodeIdList().size() == 1) {
                    throw new NulsRuntimeException(ErrorCode.FAILED, "node count not enough!");
                }
                Log.warn("Rollback block start height:{},local is highest and wrong!", localBestBlock.getHeader().getHeight());
                //bifurcation
                rollbackBlock(localBestBlock.getHeader().getHeight());
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            } else {
                checkNeedRollback(localBestBlock);
                localBestBlock = this.blockService.getLocalBestBlock();
            }
        } while (false);
        resultCorrentInfo.setLocalBestBlock(localBestBlock);
        return resultCorrentInfo;
    }

    private void checkNeedRollback(Block block) {
        BlockInfo netThisBlockInfo = DistributedBlockInfoRequestUtils.getInstance().request(block.getHeader().getHeight());
        if (netThisBlockInfo.getBestHash().equals(block.getHeader().getHash())) {
            return;
        }
        if (block.getHeader().getHeight() != netThisBlockInfo.getBestHeight()) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "answer not asked!");
        }
        Log.debug("Rollback block start height:{},local has wrong blocks!", block.getHeader().getHeight());
        //bifurcation
        rollbackBlock(block.getHeader().getHeight());
    }

    private void rollbackBlock(long startHeight) {
        try {
            this.blockService.rollbackBlock(startHeight);
            long height = startHeight - 1;
            Block block = getPreBlock(height);
            NulsContext.getInstance().setBestBlock(block);
            checkNeedRollback(block);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
    }

    private Block getPreBlock(long height) {
        Block block = this.blockService.getBlock(height);
        if (null == block) {
            block = getPreBlock(height - 1);
        }
        return block;
    }

    public MaintenanceStatus getStatus() {
        if (this.status == null) {
            return MaintenanceStatus.FAILED;
        }
        if (status == MaintenanceStatus.DOWNLOADING && NulsContext.getInstance().getBestHeight() >= this.downloadHeight) {
            return MaintenanceStatus.SUCCESS;
        }


        return status;
    }

}
