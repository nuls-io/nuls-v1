package io.nuls.message.bus.message;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class CommonDigestMessage extends BaseMessage<NulsDigestData> {
    public CommonDigestMessage() {
        super(MessageBusConstant.MODULE_ID_MESSAGE_BUS, MessageBusConstant.MSG_TYPE_COMMON_MSG_HASH_MSG);
    }

    @Override
    protected NulsDigestData parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readHash();
    }

    public CommonDigestMessage(NulsDigestData hash) {
        this();
        this.setMsgBody(hash);
    }
}
