package io.nuls.account.constant;

import io.nuls.account.model.Account;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.model.Na;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public class AccountConstant implements NulsConstant {

    /**
     * The module id of the message-bus module
     */
    public static final short MODULE_ID_ACCOUNT = 5;

    /**
     * The name of accouts cache
     */
    public static final String ACCOUNT_LIST_CACHE = "ACCOUNT_LIST";


    public static final Na ALIAS_NA = Na.parseNuls(1);

    /**
     * 默认账户地址
     * Default account address
     */
    public static Account DEFAULT_ACCOUNT;

    /**
     * 本地账户集合
     */
    public static Set<String> LOCAL_ADDRESS_LIST = ConcurrentHashMap.newKeySet();


    /**
     * 设置账户别名的交易类型
     */
    public static final int TX_TYPE_ACCOUNT_ALIAS = 51;
}
