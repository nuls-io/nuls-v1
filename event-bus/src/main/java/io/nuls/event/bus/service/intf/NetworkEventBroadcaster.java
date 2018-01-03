package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseNetworkEvent;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public interface NetworkEventBroadcaster {

    /**
     * broadcast a message that need to be passed
     *
     * @param event
     * @return
     */
    List<String> broadcastHashAndCache(BaseNetworkEvent event);


    List<String> broadcastHashAndCache(BaseNetworkEvent event, String excludePeerId);

    /**
     * broadcast to peers except "excludePeerId"
     * @param event
     * @param excludePeerId
     * @return
     */
    List<String> broadcastAndCache(BaseNetworkEvent event, String excludePeerId);

    /**
     * broadcast msg ,no need to pass the message
     * @param event
     */
    List<String> broadcastAndCache(BaseNetworkEvent event);

    /**
     * send msg to one peer
     * @param event
     * @param peerId
     */
    boolean sendToPeer(BaseNetworkEvent event, String peerId);

}
