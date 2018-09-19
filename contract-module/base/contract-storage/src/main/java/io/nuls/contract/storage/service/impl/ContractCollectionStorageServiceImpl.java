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
package io.nuls.contract.storage.service.impl;

import io.nuls.contract.storage.constant.ContractStorageConstant;
import io.nuls.contract.storage.po.ContractCollectionInfoPo;
import io.nuls.contract.storage.service.ContractCollectionStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/24
 */
@Component
public class ContractCollectionStorageServiceImpl implements ContractCollectionStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(ContractStorageConstant.DB_NAME_CONTRACT_COLLECTION);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveContractAddress(byte[] contractAddressBytes, ContractCollectionInfoPo contractCollectionPo) {
        if (contractAddressBytes == null || contractCollectionPo == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        Result result = dbService.putModel(ContractStorageConstant.DB_NAME_CONTRACT_COLLECTION, contractAddressBytes, contractCollectionPo);
        return result;
    }

    @Override
    public Result<ContractCollectionInfoPo> getContractAddress(byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        ContractCollectionInfoPo po = dbService.getModel(ContractStorageConstant.DB_NAME_CONTRACT_COLLECTION, contractAddressBytes, ContractCollectionInfoPo.class);
        return Result.getSuccess().setData(po);
    }

    @Override
    public Result deleteContractAddress(byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        Result result = dbService.delete(ContractStorageConstant.DB_NAME_CONTRACT_COLLECTION, contractAddressBytes);
        return result;
    }

    @Override
    public Result<List<ContractCollectionInfoPo>> getContractAddressList() {
        List<ContractCollectionInfoPo> list = dbService.values(ContractStorageConstant.DB_NAME_CONTRACT_COLLECTION, ContractCollectionInfoPo.class);
        if(list == null || list.size() ==0) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        Result<List<ContractCollectionInfoPo>> result = Result.getSuccess();
        result.setData(list);
        return result;
    }
}
