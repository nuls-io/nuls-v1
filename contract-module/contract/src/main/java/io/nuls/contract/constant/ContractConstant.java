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
package io.nuls.contract.constant;

import io.nuls.kernel.constant.NulsConstant;

public interface ContractConstant extends NulsConstant {

    short MODULE_ID_CONTRACT = 10;

    /**
     * CONTRACT
     */
    int TX_TYPE_CREATE_CONTRACT = 100;
    int TX_TYPE_CALL_CONTRACT = 101;
    int TX_TYPE_DELETE_CONTRACT = 102;

    /**
     * contract transfer
     */
    int TX_TYPE_CONTRACT_TRANSFER = 103;
    long CONTRACT_TRANSFER_GAS_COST = 1000;

    String BALANCE_TRIGGER_METHOD_NAME = "_payable";
    String BALANCE_TRIGGER_METHOD_DESC = "() return void";

    String CONTRACT_CONSTRUCTOR = "<init>";




    String CALL = "call";
    String CREATE = "create";
    String DELETE = "delete";

    String GET = "get";

    String SEND_BACK_REMARK = "Contract execution failed, return funds.";

    long CONTRACT_CONSTANT_GASLIMIT = 1000000;
    long CONTRACT_CONSTANT_PRICE = 1;

    long MAX_GASLIMIT = 10000000;

    long CONTRACT_MINIMUM_PRICE = 27;

    /**
     *
     */
    String CONTRACT_EVENT = "event";
    String CONTRACT_EVENT_DATA = "payload";

    /**
     * NRC20
     */
    String NRC20_METHOD_NAME = "name";
    String NRC20_METHOD_SYMBOL = "symbol";
    String NRC20_METHOD_DECIMALS = "decimals";
    String NRC20_METHOD_TOTAL_SUPPLY = "totalSupply";
    String NRC20_METHOD_BALANCE_OF = "balanceOf";
    String NRC20_METHOD_TRANSFER = "transfer";
    String NRC20_METHOD_TRANSFER_FROM = "transferFrom";
    String NRC20_METHOD_APPROVE = "approve";
    String NRC20_METHOD_ALLOWANCE = "allowance";
    String NRC20_EVENT_TRANSFER = "TransferEvent";
    String NRC20_EVENT_APPROVAL = "ApprovalEvent";

}
