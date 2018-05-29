package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.process.BlockMonitorProcess;
import io.nuls.core.tools.log.Log;

/**
 * @author Niels
 * @date 2018/5/18
 */
public class BlockMonitorProcessTask implements Runnable {

    private final BlockMonitorProcess process;

    public BlockMonitorProcessTask(BlockMonitorProcess process) {
        this.process = process;
    }

    @Override
    public void run() {
        try {
//            process.doProcess();
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
