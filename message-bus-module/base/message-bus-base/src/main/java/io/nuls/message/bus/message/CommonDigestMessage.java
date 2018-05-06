package io.nuls.message.bus.message;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.message.bus.constant.MessageConstant;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.NoticeData;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class CommonDigestMessage extends BaseMessage<NulsDigestData> {
    public CommonDigestMessage() {
        super(NulsConstant.MODULE_ID_EVENT_BUS, MessageConstant.MSG_TYPE_COMMON_MSG_HASH_MSG);
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
