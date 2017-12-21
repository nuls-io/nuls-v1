package io.nuls.account.entity.event;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;

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
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    protected AliasTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        AliasTransaction tx = new AliasTransaction();
        tx.parse(byteBuffer);
        return tx;
    }
}
