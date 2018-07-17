package io.nuls.network.util;

import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author Niels
 * @date 2018/7/10
 */
public class MessageContainer {

    private BaseMessage message;

    private Node node;

    public MessageContainer(BaseMessage message,Node node){
        this.message = message;
        this.node = node;
    }

    public BaseMessage getMessage() {
        return message;
    }

    public Node getNode() {
        return node;
    }
}
