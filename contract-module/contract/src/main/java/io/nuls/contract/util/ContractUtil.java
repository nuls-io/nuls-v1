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
package io.nuls.contract.util;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.*;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/25
 */
public class ContractUtil {

    /**
     * 此长度来源于BlockExtendsData中定长变量的字节总数
     */
    private static final int BLOCK_EXTENDS_DATA_FIX_LENGTH = 28;

    public static String[][] twoDimensionalArray(Object[] args) {
        if (args == null) {
            return null;
        } else {
            int length = args.length;
            String[][] two = new String[length][];
            Object arg;
            for (int i = 0; i < length; i++) {
                arg = args[i];
                if(arg instanceof String) {
                    two[i] = new String[]{(String) arg};
                } else if(arg.getClass().isArray()) {
                    int len = Array.getLength(arg);
                    String[] result = new String[len];
                    for(int k = 0; k < len; k++) {
                        result[k] = String.valueOf(Array.get(arg, k));
                    }
                    two[i] = result;
                } else if(arg instanceof ArrayList) {
                    ArrayList resultArg = (ArrayList) arg;
                    int size = resultArg.size();
                    String[] result = new String[size];
                    for(int k = 0; k < size; k++) {
                        result[k] = String.valueOf(resultArg.get(k));
                    }
                    two[i] = result;
                } else {
                    two[i] = new String[]{String.valueOf(arg)};
                }
            }
            return two;
        }
    }

    public static ContractTokenTransferInfoPo convertJsonToTokenTransferInfoPo(String event) {
        if(StringUtils.isBlank(event)) {
            return null;
        }
        ContractTokenTransferInfoPo po = null;
        try {
            Map<String, Object> eventMap = JSONUtils.json2map(event);
            String eventName = (String) eventMap.get(CONTRACT_EVENT);
            if(NRC20_EVENT_TRANSFER.equals(eventName)) {
                po = new ContractTokenTransferInfoPo();
                Map<String, Object> data = (Map<String, Object>) eventMap.get(CONTRACT_EVENT_DATA);
                Collection<Object> values = data.values();
                int i = 0;
                String transferEventdata;
                byte[] addressBytes;
                for(Object object : values) {
                    transferEventdata = (String) object;
                    if(i == 0 || i == 1) {
                        if(AddressTool.validAddress(transferEventdata)) {
                            addressBytes = AddressTool.getAddress(transferEventdata);
                            if(i == 0) {
                                po.setFrom(addressBytes);
                            } else {
                                po.setTo(addressBytes);
                            }
                        }
                    }
                    if(i == 2) {
                        po.setValue(StringUtils.isBlank(transferEventdata) ? BigInteger.ZERO : new BigInteger(transferEventdata));
                        break;
                    }
                    i++;
                }
                return po;
            }
            return null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static boolean isContractTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
        int txType = tx.getType();
        if(txType == ContractConstant.TX_TYPE_CREATE_CONTRACT
                || txType == ContractConstant.TX_TYPE_CALL_CONTRACT
                || txType == ContractConstant.TX_TYPE_DELETE_CONTRACT
                || txType == ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
            return true;
        }
        return false;
    }

    public static boolean isLockContract(long blockHeight) {
        if (blockHeight > 0) {
            long bestBlockHeight = NulsContext.getInstance().getBestHeight();
            long confirmCount = bestBlockHeight - blockHeight;
            if (confirmCount < 7) {
                return true;
            }
        }
        return false;
    }

    public static byte[] getStateRoot(BlockHeader blockHeader) {
        if(blockHeader == null || blockHeader.getExtend() == null) {
            return null;
        }
        byte[] stateRoot = blockHeader.getStateRoot();
        if(stateRoot != null && stateRoot.length > 0) {
            return stateRoot;
        }
        try {
            byte[] extend = blockHeader.getExtend();
            if(extend.length > BLOCK_EXTENDS_DATA_FIX_LENGTH) {
                VarInt varInt = new VarInt(extend, BLOCK_EXTENDS_DATA_FIX_LENGTH);
                int lengthFieldSize = varInt.getOriginalSizeInBytes();
                int stateRootlength = (int) varInt.value;
                stateRoot = new byte[stateRootlength];
                System.arraycopy(extend, BLOCK_EXTENDS_DATA_FIX_LENGTH + lengthFieldSize, stateRoot, 0, stateRootlength);
                blockHeader.setStateRoot(stateRoot);
                return stateRoot;
            }
        } catch (Exception e) {
            Log.error("parse stateRoot error.", e);
        }
        return null;
    }

    public static String bigInteger2String(BigInteger bigInteger) {
        if(bigInteger == null) {
            return null;
        }
        return bigInteger.toString();
    }

    public static String simplifyErrorMsg(String errorMsg) {
        if(StringUtils.isBlank(errorMsg)) {
            return null;
        }
        if(errorMsg.contains("Exception:")) {
            String[] msgs = errorMsg.split("Exception:", 2);
            return msgs[1].trim();
        }
        return errorMsg;
    }

    public static boolean isStopContract(int status) {
        return ContractConstant.STOP == status;
    }
}
