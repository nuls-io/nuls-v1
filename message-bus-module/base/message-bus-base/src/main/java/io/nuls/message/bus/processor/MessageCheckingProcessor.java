package io.nuls.message.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.log.Log;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.processor.manager.ProcessData;
import io.nuls.message.bus.service.impl.MessageCacheService;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.SmallBlockMessage;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageCheckingProcessor<E extends BaseMessage> implements EventHandler<DisruptorData<ProcessData<E>>> {

    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    @Override
    public void onEvent(DisruptorData<ProcessData<E>> processDataDisruptorMessage, long l, boolean b) {
        try {
            BaseMessage message = processDataDisruptorMessage.getData().getData();
            if (null == message || message.getHeader() == null) {
                return;
            }
            messageCacheService.cacheRecievedMessageHash(message.getHash());
            //todo test
            if (message.getHeader().getModuleId() == ProtocolConstant.MODULE_ID_PROTOCOL && message.getHeader().getMsgType() == ProtocolConstant.MESSAGE_TYPE_NEW_BLOCK) {
                SmallBlockMessage smallBlockMessage = (SmallBlockMessage) message;
                Log.warn("rcv-msg:" + smallBlockMessage.getMsgBody().getHeader().getHash().toString());
            }

            boolean commonDigestTx = message.getHeader().getMsgType() == MessageBusConstant.MSG_TYPE_COMMON_MSG_HASH_MSG &&
                    message.getHeader().getModuleId() == MessageBusConstant.MODULE_ID_MESSAGE_BUS;

            if (!commonDigestTx) {
                return;
            }
            if (messageCacheService.kownTheMessage(((CommonDigestMessage) message).getMsgBody())) {
                Log.info("discard:{}," + ((CommonDigestMessage) message).getMsgBody(), processDataDisruptorMessage.getData().getNode().getId());
                processDataDisruptorMessage.setStoped(true);
            } else if (messageCacheService.kownTheMessage(message.getHash())) {
                Log.info("discard2:{}," + message.getClass(), processDataDisruptorMessage.getData().getNode().getId());
                processDataDisruptorMessage.setStoped(true);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
