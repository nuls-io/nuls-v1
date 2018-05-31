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

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.NulsData;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Niels
 * @date 2018/1/26
 */
public class ValidatorManager {

    private static Map<Class, DataValidatorChain> chainMap = new ConcurrentHashMap<>();

    private static boolean success;

    public static void init() {
        List<NulsDataValidator> validatorList = null;
        try {
            validatorList = SpringLiteContext.getBeanList(NulsDataValidator.class);
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
        for (NulsDataValidator validator : validatorList) {
            Method[] methods = validator.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if ("validate".equals(method.getName())) {
                    Class paramType = method.getParameterTypes()[0];
                    if(paramType.equals(NulsData.class)){
                        continue;
                    }
                    addValidator(paramType, validator);
                    break;
                }
            }
        }
        success = true;
    }

    public static boolean isInitSuccess() {
        return success;
    }

    public static void addValidator(Class<? extends NulsData> clazz, NulsDataValidator<? extends NulsData> validator) {
        DataValidatorChain chain = chainMap.get(clazz);
        if (null == chain) {
            chain = new DataValidatorChain();
            chainMap.put(clazz, chain);
        }
        chain.addValidator(validator);
    }

    public static ValidateResult startDoValidator(NulsData data) {
        if (data == null) {
            return ValidateResult.getFailedResult(ValidatorManager.class.getName(), KernelErrorCode.NULL_PARAMETER);
        }
        DataValidatorChain chain = chainMap.get(data.getClass());
        if (null == chain) {
            return ValidateResult.getSuccessResult();
        }
        return chain.startDoValidator(data);
    }

}

