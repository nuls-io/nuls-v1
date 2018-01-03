package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.BlockInfo;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.consensus.utils.DistributedBlockDownloadUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread implements Runnable {

    public static DistributedBlockInfoRequestUtils BEST_HEIGHT_FROM_NET = DistributedBlockInfoRequestUtils.getInstance();

    public static final String THREAD_NAME = "block-maintenance";

    private static BlockMaintenanceThread instance;

    private final BlockService blockService = BlockServiceImpl.getInstance();

    private final NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);

    public static synchronized BlockMaintenanceThread getInstance() {
        if (instance == null) {
            instance = new BlockMaintenanceThread();
        }
        return instance;
    }

    @Override
    public void run() {
        checkGenesisBlock();
        while (true) {
            try {
                syncBlock();
            } catch (Exception e) {
                Log.error(e.getMessage());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e1) {
                    Log.error(e1);
                }
            }

        }
    }


    public synchronized void syncBlock() {
        Block localBestBlock = getLocalBestCorrectBlock();
        boolean doit = false;
        long startHeight = 0;
        BlockInfo blockInfo = null;
        do {
            if (null == localBestBlock) {
                doit = true;
                blockInfo = BEST_HEIGHT_FROM_NET.request(-1);
                break;
            }
            startHeight = localBestBlock.getHeader().getHeight() + 1;
            long interval = TimeService.currentTimeMillis() - localBestBlock.getHeader().getTime();
            if (interval < (PocConsensusConstant.BLOCK_TIME_INTERVAL * 2)) {
                doit = false;
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                break;
            }
            blockInfo = BEST_HEIGHT_FROM_NET.request(-1);
            if (blockInfo.getHeight() > localBestBlock.getHeader().getHeight()) {
                doit = true;
                break;
            }
        } while (false);
        if (null == blockInfo) {
            throw new NulsRuntimeException(ErrorCode.NET_MESSAGE_ERROR, "cannot get best block info!");
        }
        if (doit) {
            downloadBlocks(blockInfo.getPeerIdList(), startHeight, blockInfo.getHeight(), blockInfo.getHash().getDigestHex());
        }
    }


    private void downloadBlocks(List<String> peerIdList, long startHeight, long endHeight, String endHash) {
        DistributedBlockDownloadUtils utils = DistributedBlockDownloadUtils.getInstance();
        try {
            utils.request(peerIdList, startHeight, endHeight, endHash);
        } catch (InterruptedException e) {
            Log.error(e);
        }
    }

    public void checkGenesisBlock() {
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        genesisBlock.verify();
        Block localGenesisBlock = this.blockService.getGengsisBlockFromDb();
        if (null == localGenesisBlock) {
            this.blockService.save(genesisBlock);
            return;
        }
        localGenesisBlock.verify();
        if (!localGenesisBlock.equals(genesisBlock)) {
            this.blockService.clearLocalBlocks();
            this.blockService.save(genesisBlock);
        }
    }

    private Block getLocalBestCorrectBlock() {
        Block localBestBlock = this.blockService.getLocalBestBlock();
        do {
            if (null == localBestBlock || localBestBlock.getHeader().getHeight() <= 1) {
                break;
            }
            BlockInfo blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(localBestBlock.getHeader().getHeight());
            if (null == blockInfo || blockInfo.getHash() == null) {
                //本地高度最高，查询网络最新高度，并回退
                rollbackBlock(localBestBlock.getHeader().getHeight());
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            }
            if (!blockInfo.getHash().equals(localBestBlock.getHeader().getHash())) {
                //本地分叉，回退
                rollbackBlock(blockInfo.getHeight());
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            }
        } while (false);
        return localBestBlock;
    }

    private void rollbackBlock(long startHeight) {
        try {
            this.blockService.rollback(startHeight);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
        long height = startHeight - 1;
        if (height < 1) {
            throw new NulsRuntimeException(ErrorCode.NET_MESSAGE_ERROR, "Block data error!");
        }
        BlockInfo blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(height);
        Block localBlock = this.blockService.getBlockByHeight(height);
        boolean previousRb = false;
        if (null == blockInfo || blockInfo.getHash() == null || localBlock == null || localBlock.getHeader().getHash() == null) {
            previousRb = true;
        } else if (!blockInfo.getHash().getDigestHex().equals(localBlock.getHeader().getHash().getDigestHex())) {
            previousRb = true;
        }
        if (previousRb) {
            rollbackBlock(height);
        }
    }
}