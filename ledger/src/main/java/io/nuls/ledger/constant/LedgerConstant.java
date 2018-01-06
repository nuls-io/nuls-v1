package io.nuls.ledger.constant;

/**
 * @author Niels
 * @date 2017/11/8
 */
public interface LedgerConstant {
    //version
    int LEDGER_MODULE_VERSION = 0;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;

    int SMALL_CHANGE_COUNT = 500;
    String STANDING_BOOK = "STANDING_BOOK";

    short EVENT_TYPE_TRANSFER = 1;
    short EVENT_TYPE_LOCK_NULS = 2;
    short EVENT_TYPE_UNLOCK_NULS = 3;
    short EVENT_TYPE_SMALL_CHANGE = 4;

}
