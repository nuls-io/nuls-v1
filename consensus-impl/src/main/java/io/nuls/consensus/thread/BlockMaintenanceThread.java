package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.DistributedBestHeightRequestUtils;
import io.nuls.consensus.utils.DistributedBlockDownloadUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.service.intf.EventService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread implements Runnable {

    public static DistributedBestHeightRequestUtils BEST_HEIGHT_FROM_NET = DistributedBestHeightRequestUtils.getInstance();

    public static final String THREAD_NAME = "block-maintenance";

    private static BlockMaintenanceThread instance;

    private final BlockService blockService = NulsContext.getInstance().getService(BlockService.class);

    private final EventService eventService = NulsContext.getInstance().getService(EventService.class);

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
                Log.error(e);
            }
        }
    }

    public synchronized void syncBlock() {
        boolean doit = false;
        long startHeight = 0;
        do {
            Block localBestBlock = blockService.getLocalHighestBlock();
            if (null == localBestBlock) {
                doit = true;
                BEST_HEIGHT_FROM_NET.request();
                break;
            }
            startHeight = localBestBlock.getHeader().getHeight() + 1;
            long interval = TimeService.currentTimeMillis() - localBestBlock.getHeader().getTime();
            if (interval < (PocConsensusConstant.BLOCK_TIME_INTERVAL * 2)) {
                doit = false;
                break;
            }
            BEST_HEIGHT_FROM_NET.request();
            if (BEST_HEIGHT_FROM_NET.getHeight() > localBestBlock.getHeader().getHeight()) {
                doit = true;
                break;
            }
        } while (false);
        if (doit) {
            downloadBlocks(BEST_HEIGHT_FROM_NET.getPeerIdList(), startHeight, BEST_HEIGHT_FROM_NET.getHeight(), BEST_HEIGHT_FROM_NET.getHash().getDigestHex());
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

    private void checkGenesisBlock() {
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
}