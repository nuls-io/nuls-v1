package io.nuls.message.bus.handler;

import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.message.AbstractMessageHandler;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.message.GetMessageBodyMessage;

/**
 * 普通消息处理器
 * Common message handler.
 *
 * @author: Charlie
 * @date: 2018/5/8
 */
public class CommonDigestHandler extends AbstractMessageHandler<CommonDigestMessage> {

    @Override
    public void onMessage(CommonDigestMessage message, String formId) throws NulsException {
        GetMessageBodyMessage getMessageBodyMessage = new GetMessageBodyMessage();
        getMessageBodyMessage.setMsgBody(message.getMsgBody());
    }
}
