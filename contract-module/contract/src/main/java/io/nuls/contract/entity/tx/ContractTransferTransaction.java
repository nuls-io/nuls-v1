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
package io.nuls.contract.entity.tx;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.entity.txdata.ContractTransferData;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;

import java.util.Arrays;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/7
 */
public class ContractTransferTransaction extends Transaction<ContractTransferData> {

    private transient ContractTransfer transfer;

    public ContractTransferTransaction() {
        super(ContractConstant.TX_TYPE_CONTRACT_TRANSFER);
    }

    @Override
    protected ContractTransferData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new ContractTransferData());
    }

    @Override
    public String getInfo(byte[] address) {
        Coin to = coinData.getTo().get(0);
        return "+" + to.getNa().toCoinString();
    }

    @Override
    public boolean isSystemTx() {
        return true;
    }

    @Override
    public boolean needVerifySignature() {
        return false;
    }

    public ContractTransfer getTransfer() {
        return transfer;
    }

    public ContractTransferTransaction setTransfer(ContractTransfer transfer) {
        this.transfer = transfer;
        return this;
    }

    @Override
    public Na getFee() {
        ContractTransferData data = this.txData;
        byte success = data.getSuccess();
        // 合约执行成功的内部转账，手续费已从Gas中扣除，此处不在收取手续费
        if(success == 1) {
            return Na.ZERO;
        }
        // 合约执行失败，退回调用者向合约地址转入的资金，需要额外收取手续费
        Na fee = Na.ZERO;
        if (null != coinData) {
            fee = coinData.getFee();
        }
        return fee;
    }
}
