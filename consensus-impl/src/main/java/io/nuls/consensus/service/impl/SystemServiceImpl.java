package io.nuls.consensus.service.impl;

import io.nuls.consensus.service.intf.DownloadService;
import io.nuls.consensus.service.intf.SystemService;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.util.List;

/**
 * Created by ln on 2018/4/11.
 */
public class SystemServiceImpl implements SystemService {

    /**
     * 重置系统，包括重置网络、同步、共识
     * Reset the system, including resetting the network, synchronization, consensus
     * @param reason
     * @return boolean
     */
    @Override
    public boolean resetSystem(String reason) {

        Log.info("---------------reset start----------------");
        Log.info("Received a reset system request, reason: 【" + reason + "】");

        NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
        List<Node> nodeList = networkService.getAvailableNodes();
        for(Node node:nodeList){
            networkService.removeNode(node.getId());
        }

        DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);
        downloadService.reset();

        ConsensusMeetingRunner consensusMeetingRunner = ConsensusMeetingRunner.getInstance();
        consensusMeetingRunner.resetConsensus();

        Log.info("---------------reset end----------------");

        return true;
    }
}
