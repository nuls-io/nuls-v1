/**
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
 */
package io.nuls.contract.entity.tx;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.entity.txdata.CreateContractData;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;

import java.util.Arrays;
import java.util.List;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/4/20
 */
public class CreateContractTransaction extends Transaction<CreateContractData> implements ContractTransaction{

    private ContractResult contractResult;

    private transient Na returnNa;

    private transient BlockHeader blockHeader;

    public CreateContractTransaction() {
        super(ContractConstant.TX_TYPE_CREATE_CONTRACT);
    }

    @Override
    protected CreateContractData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new CreateContractData());
    }

    /**
     * 用于钱包显示资产变动
     *
     * 资产变动: 1. 仅有手续费
     * 此方法`getInfo`用于钱包账户，而合约地址不属于钱包账户，所以这里的入参不会是合约地址
     * toList有且仅有一个Coin(创建智能合约交易的特性)，必然是调用者自身扣了手续费后的找零
     *      这里的地址有两种情况，一是合约调用者的地址，二是合约Token转账的`from`,`to`
     *      综上，由于方法入参不会是合约地址，因此除合约调用者地址外，其他地址传入都返回 `0`
     * @param address
     * @return
     */
    @Override
    public String getInfo(byte[] address) {
        List<Coin> toList = coinData.getTo();
        int size = toList.size();
        if(size == 1) {
            if (Arrays.equals(address, toList.get(0).getAddress())) {
                return "-" + getFee().toCoinString();
            } else {
                return "0";
            }
        } else {
            return "--";
        }
    }

    @Override
    public ContractResult getContractResult() {
        return contractResult;
    }

    @Override
    public void setContractResult(ContractResult contractResult) {
        this.contractResult = contractResult;
    }

    @Override
    public void setReturnNa(Na returnNa) {
        this.returnNa = returnNa;
    }

    @Override
    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    @Override
    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }

    @Override
    public Na getFee() {
        Na resultFee = super.getFee();
        if(returnNa != null) {
            resultFee = resultFee.minus(returnNa);
        }
        return resultFee;
    }
}
