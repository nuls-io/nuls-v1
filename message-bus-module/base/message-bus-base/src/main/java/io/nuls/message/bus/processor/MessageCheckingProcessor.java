package io.nuls.message.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.message.bus.constant.MessageConstant;
import io.nuls.message.bus.message.CommonDigestMessage;
import io.nuls.message.bus.processor.manager.ProcessData;
import io.nuls.message.bus.service.impl.MessageCacheService;
import io.nuls.protocol.constant.ProtocolEventType;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageCheckingProcessor<E extends BaseMessage> implements EventHandler<DisruptorData<ProcessData<E>>> {

    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    @Override
    public void onEvent(DisruptorData<ProcessData<E>> processDataDisruptorMessage, long l, boolean b) throws Exception {
        try {
            BaseMessage message = processDataDisruptorMessage.getData().getData();
            if (null == message || message.getHeader() == null) {
                return;
            }
            String eventHash = message.getHash().getDigestHex();

            boolean commonDigestTx = message.getHeader().getMsgType() == MessageConstant.MSG_TYPE_COMMON_MSG_HASH_MSG &&
                    message.getHeader().getModuleId() == NulsConstant.MODULE_ID_EVENT_BUS;
            boolean specialTx = commonDigestTx || (message.getHeader().getMsgType() == ProtocolEventType.NEW_TX_EVENT &&
                    message.getHeader().getModuleId() == NulsConstant.MODULE_ID_PROTOCOL);
            specialTx = specialTx || (message.getHeader().getMsgType() == ProtocolEventType.NEW_BLOCK &&
                    message.getHeader().getModuleId() == NulsConstant.MODULE_ID_PROTOCOL);
            if (!specialTx) {
                MessageCacheService.cacheRecievedEventHash(eventHash);
                return;
            }
            if (commonDigestTx && MessageCacheService.kownTheEvent(((CommonDigestMessage) message).getMsgBody().getDigestHex())) {
                processDataDisruptorMessage.setStoped(true);
            } else if (MessageCacheService.kownTheEvent(eventHash)) {
                processDataDisruptorMessage.setStoped(true);
            } else {
                MessageCacheService.cacheRecievedEventHash(eventHash);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
