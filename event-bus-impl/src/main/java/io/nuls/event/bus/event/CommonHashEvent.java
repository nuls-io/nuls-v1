package io.nuls.event.bus.event;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.event.bus.constant.EventConstant;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class CommonHashEvent extends BaseNulsEvent<NulsDigestData> {

    public CommonHashEvent(NulsDigestData hash) {
        super(NulsConstant.MODULE_ID_EVENT_BUS, EventConstant.EVENT_TYPE_COMMON_EVENT_HASH_EVENT);
        this.setEventBody(hash);
    }

    @Override
    protected NulsDigestData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        NulsDigestData data = new NulsDigestData();
        data.parse(byteBuffer);
        return data;
    }

    @Override
    public Object copy() {
        NulsDigestData data = new NulsDigestData(this.getEventBody().getDigestBytes());
        CommonHashEvent event = new CommonHashEvent(data);
        return event;
    }
}
