package io.nuls.event.bus.processor.manager;

import io.nuls.core.event.BaseEvent;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class ProcessData<E extends BaseEvent> {

    private final E data;

    private String nodeId;

    public ProcessData(E data){
        this.data = data;
    }

    public ProcessData(E data, String nodeId) {
        this.data = data;
        this.nodeId = nodeId;
    }

    public E getData() {
        return data;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
