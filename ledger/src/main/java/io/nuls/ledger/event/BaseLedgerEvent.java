package io.nuls.ledger.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.module.AbstractLedgerModule;

/**
 *
 * @author Niels
 * @date 2017/11/16
 */
public class BaseLedgerEvent<T extends BaseNulsData> extends BaseNulsEvent<T>{


    public BaseLedgerEvent(short eventType) {
        this.setHeader(new NulsEventHeader(ModuleService.getInstance().getModuleId(AbstractLedgerModule.class),eventType ));
    }

    @Override
    protected T parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }
}
