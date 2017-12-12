package io.nuls.event.bus.handler;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractLocalNulsEventHandler;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.network.message.ReplyEvent;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class ReplyEventHandler extends AbstractLocalNulsEventHandler<ReplyEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();

    @Override
    public void onEvent(ReplyEvent event, String fromId) throws NulsException {
        // todo auto-generated method stub(niels)
        NulsDigestData digestData = new NulsDigestData(event.getEventBody().getVal());
        BaseNulsEvent confrimEvent = eventCacheService.getEvent(digestData.getDigestHex());
        if (!(confrimEvent.getEventBody() instanceof Transaction)) {
            return;
        }
        Transaction tx = (Transaction) confrimEvent.getEventBody();
        //todo 交易已完成广播确认，需要的处理如下：
    }
}
