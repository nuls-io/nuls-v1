package io.nuls.message.bus.handler;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.message.GetMessageBodyMessage;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.message.bus.service.impl.MessageCacheService;
import io.nuls.network.model.Node;

/**
 * 普通消息处理器
 * Common message handler.
 *
 * @author: Charlie
 * @date: 2018/5/8
 */
public class CommonDigestHandler extends AbstractMessageHandler<CommonDigestMessage> {

    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    @Override
    public void onMessage(CommonDigestMessage message, Node fromNode) throws NulsException {
        if (messageCacheService.kownTheMessage(message.getMsgBody())) {
            Log.info("discard:{}," + (message).getMsgBody(), fromNode.getId());
            return;
        } else if (messageCacheService.kownTheMessage(message.getHash())) {
            Log.info("discard2:{}," + message.getClass(), fromNode.getId());
            return;
        } else {
            messageCacheService.cacheRecievedMessageHash(message.getHash());
        }
        GetMessageBodyMessage getMessageBodyMessage = new GetMessageBodyMessage();
        getMessageBodyMessage.setMsgBody(message.getMsgBody());
        messageBusService.sendToNode(getMessageBodyMessage, fromNode, false);
    }
}
