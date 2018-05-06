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
public class GetMessageBodyMessage extends BaseMessage<NulsDigestData> {

    public GetMessageBodyMessage() {
        super(NulsConstant.MODULE_ID_EVENT_BUS, MessageConstant.MSG_TYPE_GET_MSG_BODY_MSG);
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

}
