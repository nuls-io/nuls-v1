package io.nuls.core.validate;

import io.nuls.core.chain.entity.BaseNulsData;

/**
 * @author Niels
 * @date 2017/11/16
 */
public interface NulsDataValidator<T extends BaseNulsData> {

    /**
     * @param data
     * @return
     */
    ValidateResult validate(T data);
}
