package io.nuls.core.constant;

/**
 * Created by win10 on 2017/10/30.
 */
public interface TransactionConstant {

    int TX_TYPE_COINBASE = 1;
    int TX_TYPE_TRANSFER = 2;
    int TX_TYPE_LOCK = 3;
    int TX_TYPE_SMALL_CHANGE = 4;
    int TX_TYPE_JOIN_CONSENSUS = 5;
    int TX_TYPE_EXIT_CONSENSUS = 6;
    int TX_TYPE_YELLOW_PUNISH = 7;
    int TX_TYPE_RED_PUNISH = 8;
    int TX_TYPE_SET_ALIAS = 9;
    int TX_TYPE_CHANGE_ALIAS = 10;
    int TX_TYPE_CREATE_SUBCHAIN = 11;
    int TX_TYPE_DEPOSIT = 12;
    int TX_TYPE_UNLOCK = 13;
}
