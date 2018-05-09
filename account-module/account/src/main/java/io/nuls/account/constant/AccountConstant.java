package io.nuls.account.constant;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.model.Na;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public interface AccountConstant extends NulsConstant {

    String ACCOUNT_LIST_CACHE = "ACCOUNT_LIST";

    Na ALIAS_NA = Na.parseNuls(1);
}
