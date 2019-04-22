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

package io.nuls.account.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 对消息签名进行验签
 *
 * @author: EdwardChan
 *
 * @since Mar. 25th 2019
 *
 *
 */
public class VerifyMessageSignatureProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "verifymessagesignature";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address of the account - Required")
                .newLine("\t<message> the The original message  - Required")
                .newLine("\t<signatureBase64> the signature  - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "verifymessagesignature <address> <message> <signatureBase64> < --verify the signature";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 4) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!AddressTool.validAddress(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String message = args[2];
        String signatureBase64 = args[3];
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("message", message);
        parameters.put("signatureBase64", signatureBase64);
        RpcClientResult rs = restFul.post("/account/verifyMessageSignature", parameters);
        if (!rs.isSuccess()) {
            return CommandResult.getFailed(rs);
        }
        return CommandResult.getResult(rs);
    }
}
