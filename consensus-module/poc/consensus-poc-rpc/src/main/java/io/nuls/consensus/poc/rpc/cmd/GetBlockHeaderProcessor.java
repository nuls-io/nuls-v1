package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

@Cmd
@Component
public class GetBlockHeaderProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getblockheader";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<hash> | <height> get block header by hash or block height - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getblockheader <hash> | <height>--get the block header with hash or height";
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
    public Result execute(String[] args) {
        String hash = null;
        long height = 0;

        if (StringUtils.isBlank(args[1])) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR.getMsg());
        }

        try {
            height = Long.parseLong(args[1]);
        } catch (Exception e) {
            hash = args[1];
        }

        Result<BlockHeader> result = null;
        if (hash != null) {
            result = restFul.get("/block/header/hash/" + hash, null);
        } else {
            result = restFul.get("/block/header/height/" + height, null);
        }
        return result;
    }
}
