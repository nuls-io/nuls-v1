package io.nuls.core.chain.intf;

/**
 * @author Niels
 * @date 2017/12/12
 */
public interface NulsCloneable extends Cloneable {
    /**
     * deep clone
     *
     * @return
     */
    Object clone();
}
