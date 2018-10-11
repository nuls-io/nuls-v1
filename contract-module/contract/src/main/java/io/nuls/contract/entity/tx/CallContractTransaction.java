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
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.entity.txdata.CallContractData;
import io.nuls.contract.service.ContractService;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.ByteArrayWrapper;
import io.nuls.kernel.utils.NulsByteBuffer;

import java.util.*;

import static io.nuls.contract.constant.ContractConstant.CONTRACT_EVENT_ADDRESS;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/4/20
 */
public class CallContractTransaction extends Transaction<CallContractData> implements ContractTransaction{

    private transient ContractService contractService = NulsContext.getServiceBean(ContractService.class);

    /**
     * 保存到全网账本中
     */
    private ContractResult contractResult;

    private transient Collection<ContractTransferTransaction> contractTransferTxs;

    private transient Na returnNa;

    private transient BlockHeader blockHeader;

    public CallContractTransaction() {
        super(ContractConstant.TX_TYPE_CALL_CONTRACT);
    }

    @Override
    protected CallContractData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new CallContractData());
    }

    /**
     * 用于钱包显示资产变动
     *
     * 资产变动: 1. 仅有手续费 2.从钱包地址向合约转账的金额、手续费
     * 此方法`getInfo`用于钱包账户，而合约地址不属于钱包账户，所以这里的入参不会是合约地址
     * 若toList只有一个Coin，则必然是调用者自身扣了手续费后的找零
     * 若toList有两个Coin，则必然有一个是从钱包地址向合约转账的金额 - 对应的是合约地址，另一个是调用者自身扣了手续费后的找零 - 对应的是调用者的地址
     *      这里的地址有三种情况，一是合约调用者的地址，二是合约转账(从合约转出)的`to`地址，三是合约Token转账的`from`,`to`
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
        } else if(size == 2) {
            Coin to1 = toList.get(0);
            Coin to2 = toList.get(1);
            boolean equals1 = Arrays.equals(address, to1.getAddress());
            boolean equals2 = Arrays.equals(address, to2.getAddress());
            if (!equals1 && !equals2) {
                return "0";
            } else if (!equals1) {
                return "-" + to1.getNa().add(getFee()).toCoinString();
            } else if (!equals2){
                return "-" + to2.getNa().add(getFee()).toCoinString();
            } else {
                return "--";
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
    public Na getFee() {
        Na resultFee = super.getFee();
        if(returnNa != null) {
            resultFee = resultFee.minus(returnNa);
        }
        return resultFee;
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
    public List<byte[]> getAllRelativeAddress() {
        if(contractResult == null && contractService != null) {
            contractResult = contractService.getContractExecuteResult(this.hash);
        }
        if(contractResult != null) {
            List<byte[]> relativeAddress = super.getAllRelativeAddress();
            if(relativeAddress == null) {
                return new ArrayList<>();
            }
            if(relativeAddress.size() == 0) {
                return relativeAddress;
            }
            HashSet<ByteArrayWrapper> addressesSet = MapUtil.createHashSet(relativeAddress.size());
            relativeAddress.stream().forEach(address -> addressesSet.add(new ByteArrayWrapper(address)));

            // 合约转账(从合约转出)
            List<ContractTransfer> transfers = contractResult.getTransfers();
            if(transfers != null && transfers.size() > 0) {
                for(ContractTransfer transfer : transfers) {
                    addressesSet.add(new ByteArrayWrapper(transfer.getFrom()));
                    addressesSet.add(new ByteArrayWrapper(transfer.getTo()));
                }
            }
            // 解析合约事件中的合约地址
            List<byte[]> parseEventContractList = this.parseEventContract(contractResult.getEvents());
            for(byte[] contract : parseEventContractList) {
                addressesSet.add(new ByteArrayWrapper(contract));
            }


            List<byte[]> resultList = new ArrayList<>();
            for(ByteArrayWrapper wrapper : addressesSet) {
                resultList.add(wrapper.getBytes());
            }
            return resultList;
        } else {
            return super.getAllRelativeAddress();
        }
    }

    private List<byte[]> parseEventContract(List<String> events) {
        List<byte[]> result = new ArrayList<>();
        if(events == null || events.isEmpty()) {
            return result;
        }
        for(String event : events) {
            try {
                Map<String, Object> eventMap = JSONUtils.json2map(event);
                String contractAddress = (String) eventMap.get(CONTRACT_EVENT_ADDRESS);
                result.add(AddressTool.getAddress(contractAddress));
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return result;
    }

    public void setContractTransferTxs(Collection<ContractTransferTransaction> contractTransferTxs) {
        this.contractTransferTxs = contractTransferTxs;
    }

    public Collection<ContractTransferTransaction> getContractTransferTxs() {
        return contractTransferTxs;
    }
}
