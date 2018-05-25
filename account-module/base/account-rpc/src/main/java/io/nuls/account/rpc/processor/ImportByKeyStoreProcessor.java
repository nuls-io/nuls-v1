package io.nuls.account.rpc.processor;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.rpc.model.AccountKeyStoreDto;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;

import java.io.*;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
@Cmd
@Component
public class ImportByKeyStoreProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "importkeystore";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<path> The path to the AccountKeystore file ")
                .newLine("\t[password] the password is between 8 and 20 inclusive of numbers and letters, not encrypted by default");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "importkeystore <path> [password] -- Import accounts according to AccountKeystore files";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 2 || length > 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (length == 3 && !StringUtils.validPassword(args[2])) {
            return false;
        }
        return true;
    }

    @Override

    public Result execute(String[] args) {
        String path = args[1];
        String password = args.length == 3 ? args[2] : null;
        Result result = getAccountKeystoreDto(path);
        if(result.isFailed()){
            return result;
        }
        AccountKeyStoreDto accountKeyStoreDto = (AccountKeyStoreDto)result.getData();
        //todo 调RPC

        return null;
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
                Log.error(e);
                return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
            } catch (IOException e) {
                Log.error(e);
                return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
            } catch (Exception e) {
                Log.error(e);
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
