package io.nuls.network.message;

import io.nuls.core.event.BaseEvent;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkEventResult {

    private final boolean success;

    private final BaseEvent replyMessage;

    public NetworkEventResult(boolean success, BaseEvent replyMessage) {
        this.success = success;
        this.replyMessage = replyMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public BaseEvent getReplyMessage() {
        return replyMessage;
    }
}
