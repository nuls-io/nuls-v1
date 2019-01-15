/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.storage.service;

import io.nuls.contract.dto.ContractResult;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/24
 */
public interface ContractExecuteResultStorageService {
    /**
     * 保存合约执行结果
     *
     * @param hash
     * @param result
     * @return
     */
    Result saveContractExecuteResult(NulsDigestData hash, ContractResult result);

    /**
     * 删除合约执行结果
     *
     * @param hash
     * @return
     */
    Result deleteContractExecuteResult(NulsDigestData hash);

    /**
     * 根据地址检查是否存在这个合约执行结果
     *
     * @param hash
     * @return
     */
    boolean isExistContractExecuteResult(NulsDigestData hash);

    /**
     * 获取合约执行结果
     *
     * @param hash
     * @return
     */
    public ContractResult getContractExecuteResult(NulsDigestData hash);

}
