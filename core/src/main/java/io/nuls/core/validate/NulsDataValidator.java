package io.nuls.core.validate;

import io.nuls.core.chain.entity.BaseNulsData;

/**
 * Created by Niels on 2017/11/16.
 */
public interface NulsDataValidator<T extends BaseNulsData> {

    ValidateResult validate(T data);
}
