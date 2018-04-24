package io.nuls.event.bus.processor;

import com.lmax.disruptor.EventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;
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
        String eventHash = event.getHash().getDigestHex();
        if (eventCacheService.kownTheEvent(eventHash)) {
            processDataDisruptorEvent.setStoped(true);
        } else {
            eventCacheService.cacheRecievedEventHash(eventHash);
        }

    }
}
