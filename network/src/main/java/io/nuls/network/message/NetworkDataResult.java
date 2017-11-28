package io.nuls.network.message;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkDataResult {

    private final boolean success;

    private final BaseNetworkData replyMessage;

    public NetworkDataResult(boolean success, BaseNetworkData replyMessage) {
        this.success = success;
        this.replyMessage = replyMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public BaseNetworkData getReplyMessage() {
        return replyMessage;
    }
}
