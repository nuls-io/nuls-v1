package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.process.BlockMonitorProcess;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.protocol.service.DownloadService;

/**
 * @author Niels
 * @date 2018/5/18
 */
public class BlockMonitorProcessTask implements Runnable {

    private final BlockMonitorProcess process;
    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    public BlockMonitorProcessTask(BlockMonitorProcess process) {
        this.process = process;
    }

    @Override
    public void run() {
        try {
            if(!downloadService.isDownloadSuccess().isSuccess()) {
                return;
            }
            process.doProcess();
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
