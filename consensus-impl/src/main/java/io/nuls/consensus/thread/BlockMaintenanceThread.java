package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.event.AskBestBlockEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.DistributedBestHeightCalcCache;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.service.intf.EventService;
import org.spongycastle.util.Times;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread implements Runnable {

    public static DistributedBestHeightCalcCache BEST_HEIGHT_FROM_NET;

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
        BEST_HEIGHT_FROM_NET = new DistributedBestHeightCalcCache();
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
            downloadBlocks(null);
        }

    }

    private synchronized int getBestHeightFromNet() {
        AskBestBlockEvent askBestBlockEvent = new AskBestBlockEvent();
        List<String> peerIdList = this.eventService.broadcast(askBestBlockEvent);
        if(peerIdList.isEmpty()){
            Log.error("get best height from net faild!");
            return 0;
        }
        BEST_HEIGHT_FROM_NET.setPeerIdList(peerIdList);
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