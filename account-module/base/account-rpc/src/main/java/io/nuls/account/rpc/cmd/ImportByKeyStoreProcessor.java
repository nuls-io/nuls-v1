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

package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.rpc.model.AccountKeyStoreDto;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据keystore导出账户,
 * 密码用来验证(keystore), 如果keystore没有密码则可以不输
 * @author: Charlie
 * @date: 2018/5/25
 */
public class ImportByKeyStoreProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "importkeystore";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<path> The path to the AccountKeystore file ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "importkeystore <path> -- import accounts according to AccountKeystore files";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String path = args[1];
        String password = CommandHelper.getPwdOptional();
        Result rs = getAccountKeystoreDto(path);
        if(rs.isFailed()){
            return CommandResult.getFailed(rs.getMsg());
        }
        AccountKeyStoreDto accountKeyStoreDto = (AccountKeyStoreDto)rs.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accountKeyStoreDto", accountKeyStoreDto);
        parameters.put("password", password);
        parameters.put("overwrite", false);
        RpcClientResult result = restFul.post("/account/import", parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }


    /**
     * 根据文件地址获取AccountKeystoreDto对象
     * Gets the AccountKeystoreDto object based on the file address
     * @param path
     * @return
     */
    private Result<AccountKeyStoreDto> getAccountKeystoreDto(String path) {
        File file = new File(path);
        if (null != file && file.isFile()) {
            StringBuilder ks = new StringBuilder();
            BufferedReader bufferedReader = null;
            String str;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                while ((str = bufferedReader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        ks.append(str);
                    }
                }
                AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(ks.toString(), AccountKeyStoreDto.class);
                return Result.getSuccess().setData(accountKeyStoreDto);
            } catch (FileNotFoundException e) {
                return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
            } catch (IOException e) {
                return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.error(e);
                    }
                }
            }
        }
        return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
    }
}
