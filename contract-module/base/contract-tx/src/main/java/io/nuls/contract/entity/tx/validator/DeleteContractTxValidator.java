/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.contract.entity.tx.validator;

import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.entity.tx.DeleteContractTransaction;
import io.nuls.contract.entity.txdata.DeleteContractData;
import io.nuls.contract.ledger.module.ContractBalance;
import io.nuls.contract.ledger.service.ContractUtxoService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2018/10/2
 */
@Component
public class DeleteContractTxValidator implements NulsDataValidator<DeleteContractTransaction> {

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private ContractUtxoService contractUtxoService;

    @Override
    public ValidateResult validate(DeleteContractTransaction tx) throws NulsException {

        DeleteContractData txData = tx.getTxData();
        byte[] sender = txData.getSender();
        byte[] contractAddressBytes = txData.getContractAddress();
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx);

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract delete error: The contract deleter is not the transaction creator.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }

        Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
        if(contractAddressInfoPoResult.isFailed()) {
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), contractAddressInfoPoResult.getErrorCode());
        }
        ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
        if(contractAddressInfoPo == null) {
            Log.error("contract delete error: The contract does not exist.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
        }
        if(!ArraysTool.arrayEquals(sender, contractAddressInfoPo.getSender())) {
            Log.error("contract delete error: The contract deleter is not the contract creator.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }

        Result<ContractBalance> result = contractUtxoService.getBalance(contractAddressBytes);
        ContractBalance balance = (ContractBalance) result.getData();
        if(balance == null) {
            Log.error("contract delete error: That balance of the contract is abnormal.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
        }

        Na totalBalance = balance.getBalance();
        if(totalBalance.compareTo(Na.ZERO) != 0) {
            Log.error("contract delete error: The balance of the contract is not 0.");
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), ContractErrorCode.CONTRACT_DELETE_BALANCE);
        }


        return ValidateResult.getSuccessResult();
    }
}
