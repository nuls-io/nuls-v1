package io.nuls.message.bus.message;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.NoticeData;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class CommonDigestMessage extends BaseMessage<NulsDigestData> {
    public CommonDigestMessage() {
        super(MessageBusConstant.MODULE_ID_MESSAGE_BUS, MessageBusConstant.MSG_TYPE_COMMON_MSG_HASH_MSG);
    }

    public CommonDigestMessage(NulsDigestData hash) {
        this();
        this.setMsgBody(hash);
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    @Override
    public Object copy() {
        NulsDigestData data = new NulsDigestData(this.getMsgBody().getWholeBytes());
        CommonDigestMessage event = new CommonDigestMessage(data);
        return event;
    }
}
