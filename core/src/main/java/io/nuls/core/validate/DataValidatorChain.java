package io.nuls.core.validate;

import io.nuls.core.chain.entity.NulsData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niels on 2017/11/16.
 */
public class DataValidatorChain {

    private List<NulsDataValidator<NulsData>> list = new ArrayList<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    public ValidateResult startDoValidator(NulsData data) {
        if (list.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        index.set(-1);
        ValidateResult result = doValidate(data);
        if (index.get() == list.size()) {
            return result;
        }
        return ValidateResult.getFaildResult("The Validators not fully executed`");
    }

    private ValidateResult doValidate(NulsData data) {
        index.set(1 + index.get());
        if (index.get() == list.size()) {
            return null;
        }
        NulsDataValidator<NulsData> validator = list.get(index.get());
        ValidateResult result = validator.validate(data);
        if (!result.isSeccess()) {
            return result;
        }
        return this.doValidate(data);
    }

    public void addValidator(NulsDataValidator validator) {
        list.add(validator);
    }
}
