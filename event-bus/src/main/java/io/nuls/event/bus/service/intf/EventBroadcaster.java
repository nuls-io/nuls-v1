package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseEvent;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public interface EventBroadcaster {

    /**
     * broadcast a message that need to be passed
     *
     * @param event
     * @return
     */
    List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf);


    List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId);

    /**
     * broadcast to nodes except "excludeNodeId"
     *
     * @param event
     * @param excludeNodeId
     * @return
     */
    List<String> broadcastAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId);

    /**
     * broadcast msg ,no need to pass the message
     *
     * @param event
     */
    List<String> broadcastAndCache(BaseEvent event, boolean needToSelf);

    /**
     * send msg to one node
     *
     * @param event
     * @param nodeId
     */
    boolean sendToNode(BaseEvent event, String nodeId);

}
