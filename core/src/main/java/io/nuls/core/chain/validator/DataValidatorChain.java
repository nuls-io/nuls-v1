package io.nuls.core.chain.validator;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niels on 2017/11/16.
 */
public class DataValidatorChain {

    private List<NulsDataValidator<NulsData>> list = new ArrayList<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    public ValidateResult startDoValidator(NulsData data) {
        index.set(-1);
        return doValidate(data);
    }

    public ValidateResult doValidate(NulsData data) {
        index.set(1 + index.get());
        if (index.get() == list.size()) {
            return null;
        }
        NulsDataValidator<NulsData> validator = list.get(index.get());
        return validator.validate(data,this);
    }

    public void addValidator(NulsDataValidator validator) {
        list.add(validator);
    }
}
