package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.DistributedBestHeightRequestUtils;
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

    public static DistributedBestHeightRequestUtils BEST_HEIGHT_FROM_NET;

    public static final String THREAD_NAME = "block-maintenance";

    private static BlockMaintenanceThread instance;

    private final BlockService blockService = NulsContext.getInstance().getService(BlockService.class);

    private final EventService eventService = NulsContext.getInstance().getService(EventService.class);

    private BlockMaintenanceThread() {
    }

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
        do {
            Block localBestBlock = blockService.getLocalHighestBlock();
            if (null == localBestBlock) {
                doit = true;
                break;
            }
            long interval = TimeService.currentTimeMillis() - localBestBlock.getHeader().getTime();
            if (interval < (PocConsensusConstant.BLOCK_TIME_INTERVAL * 2)) {
                doit = false;
                break;
            }
            long netBestHeight = this.getBestHeightFromNet();
            if (netBestHeight > localBestBlock.getHeader().getHeight()) {
                doit = true;
                break;
            }
        } while (false);
        if (doit) {
            downloadBlocks(null);
        }

    }

    private synchronized long getBestHeightFromNet() {

        BEST_HEIGHT_FROM_NET = new DistributedBestHeightRequestUtils();
        BEST_HEIGHT_FROM_NET.request();
        return BEST_HEIGHT_FROM_NET.getHeight();
    }

    private void downloadBlocks(List<String> peerIdList) {
        //todo
    }

    private void checkGenesisBlock() {
//        Block genesisBlock = this.blockService.getGengsisBlock();
        //todo

    }
}