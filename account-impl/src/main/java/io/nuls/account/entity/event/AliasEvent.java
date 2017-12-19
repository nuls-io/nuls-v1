package io.nuls.account.entity.event;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasEvent extends BaseNulsEvent<AliasTransaction> {
    public AliasEvent() {
        super(NulsConstant.MODULE_ID_ACCOUNT, AccountConstant.EVENT_TYPE_ALIAS);
    }

    @Override
    public Object copy() {
        return null;
    }

    @Override
    protected AliasTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        return null;
    }
}
