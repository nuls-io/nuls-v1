package io.nuls.event.bus.handler;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.network.message.ReplyNotice;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class ReplyNoticeHandler extends AbstractEventHandler<ReplyNotice> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();

    @Override
    public void onEvent(ReplyNotice event, String fromId) {
        // todo auto-generated method stub(niels)
        NulsDigestData digestData = new NulsDigestData(event.getEventBody().getVal());
        BaseEvent confrimEvent = eventCacheService.getEvent(digestData.getDigestHex());
        if (!(confrimEvent.getEventBody() instanceof Transaction)) {
            return;
        }
        Transaction tx = (Transaction) confrimEvent.getEventBody();
        //todo 交易已完成广播确认，需要的处理如下：
    }
}
