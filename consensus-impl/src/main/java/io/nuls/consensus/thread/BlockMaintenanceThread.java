package io.nuls.consensus.thread;

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;

/**
 *
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread implements Runnable {

    private static BlockMaintenanceThread instance ;

    private BlockService blockService;

    private BlockMaintenanceThread() {
    }

    public static synchronized BlockMaintenanceThread getInstance(){
        if(instance==null){
            instance = new BlockMaintenanceThread();
        }
        return instance;
    }

    @Override
    public void run() {
        checkGenesisBlock();
        syncBlock();
    }

    private void syncBlock() {
    }

    private void checkGenesisBlock() {
        Block genesisBlock = this.blockService.getGengsisBlock();


    }
}
