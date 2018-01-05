package io.nuls.account.module.intf;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseModuleBootstrap;

/**
 *
 * @author Niels
 * @date 2017/10/30
 *
 */
public abstract class AbstractAccountModule extends BaseModuleBootstrap {
    public AbstractAccountModule() {
        super(NulsConstant.MODULE_ID_ACCOUNT);
    }
}
