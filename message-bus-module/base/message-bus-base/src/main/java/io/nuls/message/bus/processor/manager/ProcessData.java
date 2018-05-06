package io.nuls.message.bus.processor.manager;

import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class ProcessData<T extends BaseMessage> {

    private final T data;

    private String nodeId;

    public ProcessData(T data){
        this.data = data;
    }

    public ProcessData(T data, String nodeId){
        this.data = data;
        this.nodeId = nodeId;
    }

    public T getData(){
        return data;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
