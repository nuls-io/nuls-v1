package io.nuls.account.constant;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.model.Na;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public interface AccountConstant extends NulsConstant {

    /**
     * The module id of the message-bus module
     */
   short MODULE_ID_ACCOUNT = 5;

    /**
     * The name of accouts cache
     */
    String ACCOUNT_LIST_CACHE = "ACCOUNT_LIST";


    Na ALIAS_NA = Na.parseNuls(1);



    /**
     * 设置账户别名的交易类型
     * Set the transaction type of account alias.
     */
    int TX_TYPE_ACCOUNT_ALIAS = 51;

    /**
     * 账户解锁时间最大值(单位:秒)
     * Account unlock time maximum (unit: second)
     */
    int ACCOUNT_MAX_UNLOCK_TIME = 600;
}
