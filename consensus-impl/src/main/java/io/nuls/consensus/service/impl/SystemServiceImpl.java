package io.nuls.consensus.service.impl;

import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.service.intf.DownloadService;
import io.nuls.consensus.service.intf.SystemService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.network.service.NetworkService;

/**
 * Created by ln on 2018/4/11.
 */
public class SystemServiceImpl implements SystemService {

    private static final long INTERVAL_TIME = 60000L;

    private long lastResetTime;

    /**
     * 重置系统，包括重置网络、同步、共识
     * Reset the system, including resetting the network, synchronization, consensus
     *
     * @return boolean
     */
    @Override
    public boolean resetSystem(String reason) {
        if ((TimeService.currentTimeMillis() - lastResetTime) <= INTERVAL_TIME) {
            Log.info("system reset interrupt!");
            return true;
        }
        Log.info("---------------reset start----------------");
        Log.info("Received a reset system request, reason: 【" + reason + "】");

        NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
        networkService.reset();

        DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);
        downloadService.reset();

        ConsensusManager.getInstance().clearCache();

        Log.info("---------------reset end----------------");

        this.lastResetTime = TimeService.currentTimeMillis();

        return true;
    }
}
