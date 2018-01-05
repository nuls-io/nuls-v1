package io.nuls.db.module;


import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseModuleBootstrap;

/**
 *
 * @author zhouwei
 * @date 2017/9/26
 */
public abstract class AbstractDBModule extends BaseModuleBootstrap {

    protected AbstractDBModule() {
        super(NulsConstant.MODULE_ID_DB);
    }

}
