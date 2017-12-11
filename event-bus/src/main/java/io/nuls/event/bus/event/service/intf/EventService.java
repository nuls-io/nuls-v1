package io.nuls.event.bus.event.service.intf;

import io.nuls.core.event.BaseNulsEvent;

/**
 * @author Niels
 * @date 2017/12/8
 */
public interface EventService {

    /**
     * broadcast a message that need to be passed to half peers
     * and get confirmation from another half peers
     *
     * @param event
     * @return
     */
    boolean broadcastSyncNeedConfirmation(BaseNulsEvent event);

    /**
     * broadcast a message that need to be passed
     *
     * @param event
     * @return
     */
    boolean broadcastHashAndCache(BaseNulsEvent event);

    void broadcast(BaseNulsEvent event, String excludePeerId);

    /**
     * broadcast msg ,no need to pass the message
     *
     * @param event
     */
    void broadcast(BaseNulsEvent event);

    /**
     * send msg to one peer
     *
     * @param event
     * @param peerId
     */
    void sendToPeer(BaseNulsEvent event, String peerId);

}
