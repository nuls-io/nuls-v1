package io.nuls.account.rpc.processor;

import io.nuls.account.rpc.model.AccountKeyStoreDto;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.json.JSONUtils;
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
        return null;
    }

    private static String testPath1 = "/Users/lichao/Documents/工作文档/测试数据/AccountKeystore.ks";
    private static String testPath2 = "/Users/lichao/Downloads/AccountKeystore.ks";

    public static void main(String[] args) {
        getAccountKeystoreDto(testPath2);
    }

    private static Result getAccountKeystoreDto(String path) {
        File file = new File(path);
        System.out.println(file);
        if (null != file && file.isFile()) {
            StringBuilder ks = new StringBuilder();
            BufferedReader reader = null;
            String str = null;

            try {
                reader = new BufferedReader(new FileReader(file));
                while ((str = reader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        ks.append(str);
                    }
                }
                //String jsonStr = ks.toString();
                //ystem.out.println(jsonStr);
                AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(ks.toString(), AccountKeyStoreDto.class);
                //System.out.println(JSONUtils.obj2json(accountKeyStoreDto));
                Result.getSuccess().setData(accountKeyStoreDto);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
