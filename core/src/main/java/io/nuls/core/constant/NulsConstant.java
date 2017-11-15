package io.nuls.core.constant;

/**
 * Created by Niels on 2017/9/26.
 *
 */
public interface NulsConstant {



    String USER_CONFIG_FILE = "nuls.ini";
    String SYSTEM_CONFIG_FILE = "sys.properties";
    //TODO VALUE OF MAIN CHAIN(NULS)
    long NULS_MAGIC_NUMBER = 8123;

    /**----[ System] ----*/
    String CFG_SYSTEM_SECTION = "System";
    String CFG_SYSTEM_LANGUAGE = "language";
    String CFG_SYSTEM_CHARSET = "charset";
    String DEFAULT_ENCODING = "GBK";


    /**----[ Bootstrap] ----*/
//    String CFG_BOOTSTRAP_RPC_SERVER_MODULE = "RPC";
//    String CFG_BOOTSTRAP_DB_MODULE = "DB";
//    String CFG_BOOTSTRAP_QUEUE_MODULE = "MQ";
//    String CFG_BOOTSTRAP_P2P_MODULE = "P2P";
//    String CFG_BOOTSTRAP_ACCOUNT_MODULE = "ACCOUNT";
//    String CFG_BOOTSTRAP_EVENT_BUS_MODULE = "EB";
//    String CFG_BOOTSTRAP_CACHE_MODULE = "CACHE";
}
