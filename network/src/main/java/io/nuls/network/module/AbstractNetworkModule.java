package io.nuls.network.module;


import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 *
 * @author vivi
 * @date 2017/11/21
 */
public abstract class AbstractNetworkModule extends BaseNulsModule {

    public static final short networkModuleId = 4;
    protected AbstractNetworkModule(){
        super(NulsConstant.MODULE_ID_NETWORK);
    }
}
