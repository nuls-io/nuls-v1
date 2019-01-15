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
package io.nuls.contract.storage.service.impl;

import io.nuls.contract.storage.constant.ContractStorageConstant;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.storage.service.ContractTokenTransferStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Component
public class ContractTokenTransferStorageServiceImpl implements ContractTokenTransferStorageService, InitializingBean {
    @Autowired
    private DBService dbService;
    private String area;

    @Override
    public void afterPropertiesSet() {
        this.area = ContractStorageConstant.DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER;
        Result result = dbService.createArea(this.area);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveTokenTransferInfo(byte[] infoKey, ContractTokenTransferInfoPo infoPo) {
        return dbService.putModel(this.area, infoKey, infoPo);
    }

    @Override
    public List<ContractTokenTransferInfoPo> getTokenTransferInfoListByAddress(byte[] address) {
        List<ContractTokenTransferInfoPo> infoPoList = new ArrayList<>();
        List<byte[]> keyList = dbService.keyList(this.area);
        if (keyList == null || keyList.isEmpty()) {
            return infoPoList;
        }

        ContractTokenTransferInfoPo tokenTransferInfoPo;
        for (byte[] key : keyList) {
            if (isAddressEquals(key, address)) {
                tokenTransferInfoPo = dbService.getModel(this.area, key, ContractTokenTransferInfoPo.class);
                infoPoList.add(tokenTransferInfoPo);
            }
        }
        return infoPoList;
    }

    private boolean isAddressEquals(byte[] key, byte[] address) {
        int length = Address.ADDRESS_LENGTH;
        for(int i = 0; i < length; i++) {
            if(key[i] != address[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ContractTokenTransferInfoPo> getTokenTransferInfoListByAddress(byte[] address, byte[] txHash) {
        List<ContractTokenTransferInfoPo> infoPoList = new ArrayList<>();
        List<byte[]> keyList = dbService.keyList(this.area);
        if (keyList == null || keyList.isEmpty()) {
            return infoPoList;
        }

        ContractTokenTransferInfoPo tokenTransferInfoPo;
        for (byte[] key : keyList) {
            if (isAddressAndHashEquals(key, address, txHash)) {
                tokenTransferInfoPo = dbService.getModel(this.area, key, ContractTokenTransferInfoPo.class);
                infoPoList.add(tokenTransferInfoPo);
            }
        }
        return infoPoList;
    }

    private boolean isAddressAndHashEquals(byte[] key, byte[] address, byte[] txHash) {
        int length = Address.ADDRESS_LENGTH + txHash.length;
        for(int i = 0, k = 0; i < length; i++) {
            if(i < Address.ADDRESS_LENGTH) {
                if(key[i] != address[i]) {
                    return false;
                }
            } else {
                if(key[i] != txHash[k++]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Result deleteTokenTransferInfo(byte[] infoKey) {
        return dbService.delete(this.area, infoKey);
    }

    @Override
    public Result<ContractTokenTransferInfoPo> getTokenTransferInfo(byte[] infoKey) {
        ContractTokenTransferInfoPo tokenTransferInfoPo = dbService.getModel(this.area, infoKey, ContractTokenTransferInfoPo.class);
        Result<ContractTokenTransferInfoPo> result = Result.getSuccess();
        result.setData(tokenTransferInfoPo);
        return result;
    }
}
