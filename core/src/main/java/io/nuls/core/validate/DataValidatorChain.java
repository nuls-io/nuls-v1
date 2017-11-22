package io.nuls.core.validate;

import io.nuls.core.chain.entity.BaseNulsData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/16
 */
public class DataValidatorChain {

    private List<NulsDataValidator<BaseNulsData>> list = new ArrayList<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    public ValidateResult startDoValidator(BaseNulsData data) {
        if (list.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        index.set(-1);
        ValidateResult result = doValidate(data);
        boolean b = index.get() == list.size();
        index.remove();
        if (b) {
            return result;
        } else {
            return ValidateResult.getFaildResult("The Validators not fully executed`");
        }
    }

    private ValidateResult doValidate(BaseNulsData data) {
        index.set(1 + index.get());
        if (index.get() == list.size()) {
            return null;
        }
        NulsDataValidator<BaseNulsData> validator = list.get(index.get());
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
