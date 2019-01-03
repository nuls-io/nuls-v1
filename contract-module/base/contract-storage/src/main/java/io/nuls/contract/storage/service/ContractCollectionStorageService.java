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

import io.nuls.contract.storage.po.ContractCollectionInfoPo;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/15
 */
public interface ContractCollectionStorageService {
    /**
     * 保存合约地址信息
     *
     * @param contractAddressBytes
     * @param contractCollectionPo
     * @return
     */
    Result saveContractAddress(byte[] contractAddressBytes, ContractCollectionInfoPo contractCollectionPo);

    /**
     * 根据合约地址获取收藏的合约地址信息
     *
     * @param contractAddressBytes
     * @return
     */
    Result<ContractCollectionInfoPo> getContractAddress(byte[] contractAddressBytes);

    /**
     * 删除收藏的合约地址
     *
     * @param contractAddressBytes
     * @return
     */
    Result deleteContractAddress(byte[] contractAddressBytes);

    /**
     * 获取收藏的合约地址信息列表
     *
     * @return
     */
    Result<List<ContractCollectionInfoPo>> getContractAddressList();
}
