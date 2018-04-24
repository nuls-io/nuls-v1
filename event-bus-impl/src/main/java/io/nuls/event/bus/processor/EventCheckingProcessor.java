package io.nuls.event.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.core.constant.NulsConstant;
import io.nuls.event.bus.constant.EventConstant;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;
import io.nuls.protocol.constant.ProtocolEventType;
import io.nuls.protocol.event.base.BaseEvent;

/**
 * @author: Niels Wang
 * @date: 2018/4/24
 */
public class EventCheckingProcessor<E extends BaseEvent> implements EventHandler<DisruptorEvent<ProcessData<E>>> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();

    @Override
    public void onEvent(DisruptorEvent<ProcessData<E>> processDataDisruptorEvent, long l, boolean b) throws Exception {
        BaseEvent event = processDataDisruptorEvent.getData().getData();
        if (null == event || event.getHeader() == null) {
            return;
        }
        String eventHash = event.getHash().getDigestHex();

        boolean commonDigestTx = event.getHeader().getEventType() == EventConstant.EVENT_TYPE_COMMON_EVENT_HASH_EVENT &&
                event.getHeader().getModuleId() == NulsConstant.MODULE_ID_EVENT_BUS;
        boolean specialTx = commonDigestTx || (event.getHeader().getEventType() == ProtocolEventType.NEW_TX_EVENT &&
                event.getHeader().getModuleId() == NulsConstant.MODULE_ID_PROTOCOL);
        specialTx = specialTx || (event.getHeader().getEventType() == ProtocolEventType.NEW_BLOCK &&
                event.getHeader().getModuleId() == NulsConstant.MODULE_ID_PROTOCOL);
        if (!specialTx) {
            eventCacheService.cacheRecievedEventHash(eventHash);
            return;
        }
        if (commonDigestTx && eventCacheService.kownTheEvent(((CommonDigestEvent) event).getEventBody().getDigestHex())) {
            processDataDisruptorEvent.setStoped(true);
        } else if (eventCacheService.kownTheEvent(eventHash)) {
            processDataDisruptorEvent.setStoped(true);
        } else {
            eventCacheService.cacheRecievedEventHash(eventHash);
        }
    }
}
