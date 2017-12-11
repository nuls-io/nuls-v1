package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import org.spongycastle.util.Times;

/**
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread implements Runnable {

    public static final String THREAD_NAME = "block-maintenance";

    private static BlockMaintenanceThread instance;

    private BlockService blockService;

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

    public void syncBlock() {
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
            int netBestHeight = this.getBestHeightFromNet();
            if (netBestHeight > localBestBlock.getHeader().getHeight()) {
                doit = true;
                break;
            }
        } while (false);
        if (doit) {
            downloadBlocks();
        }

    }

    private int getBestHeightFromNet() {
        //todo
        return 0;
    }

    private void downloadBlocks() {
        //todo
    }

    private void checkGenesisBlock() {
//        Block genesisBlock = this.blockService.getGengsisBlock();
        //todo

    }
}