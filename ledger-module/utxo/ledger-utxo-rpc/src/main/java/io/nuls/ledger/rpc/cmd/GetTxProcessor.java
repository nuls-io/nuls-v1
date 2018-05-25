package io.nuls.ledger.rpc.cmd;

import com.sun.org.apache.regexp.internal.RE;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;
import io.nuls.ledger.constant.LedgerErrorCode;

public class GetTxProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "gettx";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<hash>   transaction hash -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "gettx <hash> --get the transaction information by txhash";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        return true;
    }

    @Override
    public Result execute(String[] args) {
        String hash = args[1];
        if(StringUtils.isBlank(hash)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/tx/hash/" + hash, null);
        return result;
    }
}
