package io.nuls.account.module.intf;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 *
 * @author Niels
 * @date 2017/10/30
 *
 */
public abstract class AbstractAccountModule extends BaseNulsModule {
    public AbstractAccountModule() {
        super(NulsConstant.MODULE_ID_ACCOUNT);
    }
}
