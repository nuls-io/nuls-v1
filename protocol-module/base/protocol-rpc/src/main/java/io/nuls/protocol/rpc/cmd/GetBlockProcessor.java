package io.nuls.protocol.rpc.cmd;

import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class GetBlockProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getblock";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<hash> | <height> get block by hash or block height - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getblock <hash> | <height> --get the block with hash or height";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 2) {
            return false;
        }
        if(!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.isNumeric(args[1])){
            if(!StringUtils.validHash(args[1])){
                return false;
            }
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String arg = args[1];
        RpcClientResult result = null;
        if(StringUtils.isNumeric(arg)){
            result = restFul.get("/block/height/" + arg, null);
        }else{
            result = restFul.get("/block/hash/" + arg, null);
        }
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
