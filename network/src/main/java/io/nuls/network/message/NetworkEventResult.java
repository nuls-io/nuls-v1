package io.nuls.network.message;

import io.nuls.core.event.BaseNetworkEvent;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkEventResult {

    private final boolean success;

    private final BaseNetworkEvent replyMessage;

    public NetworkEventResult(boolean success, BaseNetworkEvent replyMessage) {
        this.success = success;
        this.replyMessage = replyMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public BaseNetworkEvent getReplyMessage() {
        return replyMessage;
    }
}
