package io.nuls.ledger.entity;

public enum OutPutStatusEnum {

    UTXO_CONFIRMED_UNSPENT,

    UTXO_CONFIRMED_TIME_LOCK,

    UTXO_CONFIRMED_CONSENSUS_LOCK,

    UTXO_CONFIRMED_SPENT,

    UTXO_SPENT
}
