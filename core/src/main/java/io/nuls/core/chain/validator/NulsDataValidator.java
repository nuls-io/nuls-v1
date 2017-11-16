package io.nuls.core.chain.validator;

import io.nuls.core.chain.entity.NulsData;

/**
 * Created by Niels on 2017/11/16.
 */
public interface NulsDataValidator<T extends NulsData> {

    ValidateResult validate(T data, DataValidatorChain dataValidatorChain);
}
