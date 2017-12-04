package io.nuls.core.constant;

/**
 * SYSTEM CONSTANT
 * @author Niels
 * @date 2017/9/26
 */
public interface NulsConstant {

    String USER_CONFIG_FILE = "nuls.ini";
    String SYSTEM_CONFIG_FILE = "sys.properties";

    /**----[ System] ----*/
    String CFG_SYSTEM_SECTION = "System";
    String CFG_SYSTEM_LANGUAGE = "language";
    String CFG_SYSTEM_DEFAULT_ENCODING = "language";

    /**----[ Bootstrap] ----*/
//    String CFG_BOOTSTRAP_RPC_SERVER_MODULE = "RPC";
//    String CFG_BOOTSTRAP_DB_MODULE = "DB";
//    String CFG_BOOTSTRAP_QUEUE_MODULE = "MQ";
//    String CFG_BOOTSTRAP_P2P_MODULE = "P2P";
//    String CFG_BOOTSTRAP_ACCOUNT_MODULE = "ACCOUNT";
//    String CFG_BOOTSTRAP_EVENT_BUS_MODULE = "EB";
//    String CFG_BOOTSTRAP_CACHE_MODULE = "CACHE";

    /**----[ Module Id] ----*/
    short MODULE_ID_MQ = 1;
    short MODULE_ID_DB = 2;
    short MODULE_ID_CACHE = 3;
    short MODULE_ID_NETWORK = 4;
    short MODULE_ID_ACCOUNT = 5;
    short MODULE_ID_EVENT_BUS = 6;
    short MODULE_ID_CONSENSUS = 7;
    short MODULE_ID_LEDGER = 8;
    short MODULE_ID_RPC = 9;
}
