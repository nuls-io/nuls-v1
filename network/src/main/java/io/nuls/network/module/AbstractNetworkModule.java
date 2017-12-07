package io.nuls.network.module;


import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.constant.NetworkConstant;

/**
 * @author vivi
 * @date 2017/11/21
 */
public abstract class AbstractNetworkModule extends BaseNulsModule {

    public static int ExternalPort;

    protected AbstractNetworkModule() {
        super(NulsConstant.MODULE_ID_NETWORK);
        ExternalPort = ConfigLoader.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_EXTER_PORT, 8632);
    }
}
