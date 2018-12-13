/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.service;

import io.nuls.contract.dto.ContractResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;

import java.util.LinkedList;
import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/22
 */
public interface ContractTxService {
    /**
     * 创建生成智能合约的交易
     *
     * @param sender          交易创建者
     * @param gasLimit        最大gas消耗
     * @param price           执行合约单价
     * @param contractCode    合约代码
     * @param args            参数列表
     * @param password        账户密码
     * @param remark          备注
     * @return
     */
    Result contractCreateTx(String sender, Long gasLimit, Long price,
                            byte[] contractCode, String[][] args, String password, String remark);

    Result validateContractCreateTx(Long gasLimit, Long price,
                                    byte[] contractCode, String[][] args);

    LinkedList<Map<String, String>> getLocalUnconfirmedCreateContractTransaction(String sender);

    void removeLocalUnconfirmedCreateContractTransaction(String sender, String contractAddress, ContractResult contractResult);

    void removeLocalUnconfirmedCreateContractTransaction(String sender, String contractAddress);

    void removeLocalFailedUnconfirmedCreateContractTransaction(String sender, String contractAddress);

    /**
     * 预创建生成智能合约的交易
     * 用于测试合约是否能正确创建
     *
     * @param sender          交易创建者
     * @param gasLimit        最大gas消耗
     * @param price           执行合约单价
     * @param contractCode    合约代码
     * @param args            参数列表
     * @param password        账户密码
     * @param remark          备注
     * @return
     */
    Result contractPreCreateTx(String sender, Long gasLimit, Long price,
                               byte[] contractCode, String[][] args, String password, String remark);

    /**
     * 创建调用智能合约的交易
     *
     * @param sender          交易创建者
     * @param value           交易附带的货币量
     * @param gasLimit        最大gas消耗
     * @param price           执行合约单价
     * @param contractAddress 合约地址
     * @param methodName      方法名
     * @param methodDesc      方法签名，如果方法名不重复，可以不传
     * @param args            参数列表
     * @param password        账户密码
     * @param remark          备注
     * @return
     */
    Result contractCallTx(String sender, Na value, Long gasLimit, Long price, String contractAddress,
                          String methodName, String methodDesc, String[][] args, String password, String remark);

    Result validateContractCallTx(String sender, Long value, Long gasLimit, Long price, String contractAddress,
                                  String methodName, String methodDesc, String[][] args);

    /**
     * 计算交易手续费
     *
     * @param sender          交易创建者
     * @param value           交易附带的货币量
     * @param gasLimit        最大gas消耗
     * @param price           执行合约单价
     * @param contractAddress 合约地址
     * @param methodName      方法名
     * @param methodDesc      方法签名，如果方法名不重复，可以不传
     * @param args            参数列表
     * @param remark          备注
     * @return
     */
    Result transferFee(String sender, Na value, Long gasLimit, Long price, String contractAddress,
                              String methodName, String methodDesc, String[][] args, String remark);

    /**
     * 创建删除智能合约的交易
     *
     * @param sender          交易创建者
     * @param contractAddress 合约地址
     * @param password        账户密码
     * @param remark          备注
     * @return
     */
    Result contractDeleteTx(String sender, String contractAddress, String password, String remark);

    Result validateContractDeleteTx(String sender, String contractAddress);
}
