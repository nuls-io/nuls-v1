package io.nuls.message.bus.handler;

import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.message.GetMessageBodyMessage;
import io.nuls.network.entity.Node;

/**
 * 普通消息处理器
 * Common message handler.
 *
 * @author: Charlie
 * @date: 2018/5/8
 */
public class CommonDigestHandler extends AbstractMessageHandler<CommonDigestMessage> {

    @Override
    public void onMessage(CommonDigestMessage message, Node formNode) throws NulsException {
        GetMessageBodyMessage getMessageBodyMessage = new GetMessageBodyMessage();
        getMessageBodyMessage.setMsgBody(message.getMsgBody());
    }
}
