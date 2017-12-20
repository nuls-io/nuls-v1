package io.nuls.account.constant;

import io.nuls.core.chain.entity.Na;

/**
 *
 * @author Niels
 * @date 2017/10/30
 *
 */
public interface AccountConstant {
    //version
    short ACCOUNT_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;

    int MAX_REMARK_LEN = 128;

    String ACCOUNT_LIST_CACHE = "ACCOUNT_LIST";

    short EVENT_TYPE_ALIAS = 1;

    Na ALIAS_Na = Na.parseNuls(1);
}
