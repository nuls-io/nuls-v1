package io.nuls.core.constant;

/**
 * @author Niels
 * @date 2017/10/30
 */
public interface TransactionConstant {

    int TX_TYPE_COIN_BASE = 1;
    int TX_TYPE_TRANSFER = 2;
    int TX_TYPE_LOCK = 3;
    int TX_TYPE_UNLOCK = 4;
    int TX_TYPE_SMALL_CHANGE = 5;
    /**
     * Account
     */
    int TX_TYPE_SET_ALIAS = 11;
    int TX_TYPE_CHANGE_ALIAS = 12;

    /**
     * CONSENSUS
     */
    int TX_TYPE_REGISTER_AGENT = 90;
    int TX_TYPE_JOIN_CONSENSUS = 91;
    int TX_TYPE_EXIT_CONSENSUS = 92;
    int TX_TYPE_YELLOW_PUNISH = 93;
    int TX_TYPE_RED_PUNISH = 94;
}
