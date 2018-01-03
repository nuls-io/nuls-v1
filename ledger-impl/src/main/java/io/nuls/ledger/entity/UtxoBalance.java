package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.intf.NulsCloneable;

import java.io.Serializable;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class UtxoBalance extends Balance {

    private List<UtxoOutput> unSpends;

    public UtxoBalance() {
        super();
    }

    public UtxoBalance(Na useable, Na locked, List<UtxoOutput> unSpends) {
        super(useable, locked);
    }

    public List<UtxoOutput> getUnSpends() {
        return unSpends;
    }

    public void setUnSpends(List<UtxoOutput> unSpends) {
        this.unSpends = unSpends;
    }
}
