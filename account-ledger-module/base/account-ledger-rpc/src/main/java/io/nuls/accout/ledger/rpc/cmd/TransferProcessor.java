/*
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
 *
 */

package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.account.model.Address;
import io.nuls.accout.ledger.rpc.form.TransferForm;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class TransferProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    private ThreadLocal<TransferForm> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "transfer";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\tsource address - Required")
                .newLine("\t<toaddress> \treceiving address - Required")
                .newLine("\t<amount> \t\tamount, you can have up to 8 valid digits after the decimal point - Required")
                .newLine("\t[remark] \t\tremark - ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transfer <address> <toAddress> <amount> [remark] --transfer";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result;
        do {
            int length = args.length;
            if (length != 4 && length != 5) {
                result = false;
                break;
            }
            if (!CommandHelper.checkArgsIsNull(args)) {
                result = false;
                break;
            }
            if (!Address.validAddress(args[1]) || !Address.validAddress(args[2])) {
                result = false;
                break;
            }
            if (!StringUtils.isNuls(args[3])) {
                result = false;
                break;
            }
            TransferForm form = getTransferForm(args);
            if(null == form){
                result = false;
                break;
            }
            paramsData.set(form);
            result = StringUtils.isNotBlank(form.getToAddress());
            if (!result) {
                break;
            }
            result = form.getAmount() > 0;
        } while (false);
        return result;
    }

    private TransferForm getTransferForm(String[] args) {
        TransferForm form = null;
        Long amount = null;
        try {
            Na na = Na.parseNuls(args[3]);
            if (na != null) {
                amount = na.getValue();
                form = new TransferForm();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        switch (args.length) {
            case 4:
                form.setAddress(args[1]);
                form.setToAddress(args[2]);
                form.setAmount(amount);
                break;
            case 5:
                form.setAddress(args[1]);
                form.setToAddress(args[2]);
                form.setAmount(amount);
                form.setRemark(args[4]);
                break;
        }
        return form;
    }

    @Override
    public CommandResult execute(String[] args) {
        TransferForm form = paramsData.get();
        if (null == form) {
            form = getTransferForm(args);
        }
        String address = form.getAddress();
        RpcClientResult res = CommandHelper.getPassword(address, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", form.getAddress());
        parameters.put("toAddress", form.getToAddress());
        parameters.put("password", password);
        parameters.put("amount", form.getAmount());
        parameters.put("remark", form.getRemark());
        RpcClientResult result = restFul.post("/accountledger/transfer", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));
    }
}
