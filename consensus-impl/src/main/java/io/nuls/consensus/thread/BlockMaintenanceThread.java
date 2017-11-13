package io.nuls.consensus.thread;

import io.nuls.consensus.module.impl.POCConsensusModuleImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.NulsThread;

/**
 * Created by Niels on 2017/11/10.
 * nuls.io
 */
public class BlockMaintenanceThread extends NulsThread {

    private static BlockMaintenanceThread instance ;

    private BlockService blockService;

    private BlockMaintenanceThread() {
        super(NulsContext.getInstance().getModule(POCConsensusModuleImpl.class), "block maintenance thread");
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
