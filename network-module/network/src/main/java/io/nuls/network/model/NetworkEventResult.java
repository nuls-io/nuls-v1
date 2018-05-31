package io.nuls.network.model;

import io.nuls.kernel.model.BaseNulsData;

public class NetworkEventResult {

    private boolean success;

    private BaseNulsData replyMessage;

    public NetworkEventResult(boolean success, BaseNulsData replyMessage) {
        this.success = success;
        this.replyMessage = replyMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public BaseNulsData getReplyMessage() {
        return replyMessage;
    }

    public void setReplyMessage(BaseNulsData replyMessage) {
        this.replyMessage = replyMessage;
    }
}
