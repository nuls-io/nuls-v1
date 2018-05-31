package io.nuls.message.bus.model;

import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class ProcessData<T extends BaseMessage> {

    private final T data;

    private Node node;

    public ProcessData(T data){
        this.data = data;
    }

    public ProcessData(T data,  Node node){
        this.data = data;
        this.node = node;
    }

    public T getData(){
        return data;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
