package io.nuls.core.chain.intf;

import io.nuls.core.utils.log.Log;

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
    default Object copy() {
        try {
            this.clone();
            return null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
