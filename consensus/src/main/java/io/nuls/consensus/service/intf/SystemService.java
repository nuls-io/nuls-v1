package io.nuls.consensus.service.intf;

/**
 * Created by ln on 2018/4/11.
 */
public interface SystemService {

    /**
     * 重置系统，包括重置网络、同步、共识
     * Reset the system, including resetting the network, synchronization, consensus
     * @param reason
     * @return boolean
     */
    boolean resetSystem(String reason);
}
