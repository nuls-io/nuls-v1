package io.nuls.network.message;

/**
 *
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkMessageResult {

    private final boolean success;

    private final AbstractNetworkMessage replyMessage;

    public NetworkMessageResult(boolean success, AbstractNetworkMessage replyMessage) {
        this.success = success;
        this.replyMessage = replyMessage;
    }
}
