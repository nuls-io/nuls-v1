package io.nuls.account.storage.constant;

import io.nuls.core.tools.crypto.Hex;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public interface AccountStorageConstant {

    /**
     * 账户表的名称
     * The name of the account table
     */
    String DB_AREA_ACCOUNT = "ACCOUNT";

    /**
     * 默认账户的数据库key
     * The name of the key account table
     */
    byte[] DEFAULT_ACCOUNT_KEY = Hex.decode("DEFAULT_ACCOUNT");
    /**
     * 别名表的名称
     * The name of the account table
     */
    String DB_AREA_ALIAS = "ALIAS";

}
