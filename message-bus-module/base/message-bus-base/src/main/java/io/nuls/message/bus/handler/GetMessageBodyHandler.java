package io.nuls.message.bus.handler;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.message.GetMessageBodyMessage;
import io.nuls.message.bus.service.impl.MessageCacheService;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 获取消息体的处理器
 * The processor used to get the message body.
 *
 * @author: Charlie
 * @date: 2018/5/8
 */
public class GetMessageBodyHandler extends AbstractMessageHandler<GetMessageBodyMessage> {

    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    @Override
    public void onMessage(GetMessageBodyMessage message, Node formNode) throws NulsException {
        BaseMessage baseMessage = messageCacheService.getSendMessage(message.getMsgBody());
        if (null == baseMessage) {
            Log.warn("get message faild, node:" + formNode.getId() + ",event:" + message.getMsgBody());
        }
    }
}
