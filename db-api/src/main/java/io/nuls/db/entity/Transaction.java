package io.nuls.db.entity;

import java.util.List;

/**
 * Nuls交易实体定义
 * Created by zhouwei on 2017/10/17.
 */
public class Transaction {

    private String hash;

    private Integer type;

    private List<TransactionInput> inputs;

    private List<TransactionOutput> outputs;

    private String remark;

    private String blockHash;
}
