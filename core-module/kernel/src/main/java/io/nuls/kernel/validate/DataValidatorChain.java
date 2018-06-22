/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.kernel.validate;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.NulsData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class DataValidatorChain {

    private List<NulsDataValidator<NulsData>> list = new ArrayList<>();
    private Set<Class> classSet = new HashSet<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    public ValidateResult startDoValidator(NulsData data) {
        if (list.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        index.set(-1);
        ValidateResult result;
        try {
            result = doValidate(data);
        } catch (Exception e) {
            Log.error(e);
            result = ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        boolean b = index.get() == list.size();
        index.remove();
        if (!b && result.isSuccess()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.VALIDATORS_NOT_FULLY_EXECUTED);
        }
        return result;
    }

    private ValidateResult doValidate(NulsData data) {
        index.set(1 + index.get());
        if (index.get() == list.size()) {
            return ValidateResult.getSuccessResult();
        }
        NulsDataValidator validator = list.get(index.get());
        ValidateResult result = null;
        try {
            result = validator.validate(data);
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getErrorCode(), e.getMessage());
        }
        if (null == result) {
            Log.error(validator.getClass() + " has null result!");
        }
        if (!result.isSuccess()) {
            return result;
        }
        return this.doValidate(data);
    }

    public void addValidator(NulsDataValidator validator) {
        if (null == validator) {
            return;
        }

        if (classSet.add(validator.getClass())) {
            list.add(validator);
        }
    }
}
