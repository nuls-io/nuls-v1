package io.nuls.ledger.rpc.cmd;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.rpc.model.TransactionDto;
import io.nuls.ledger.service.LedgerService;

@Cmd
@Component
public class GetTxProcessor implements CommandProcessor {

    @Autowired
    private LedgerService ledgerService;

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
        NulsDigestData txHash = null;
        try {
            txHash = NulsDigestData.fromDigestHex(args[1]);
        } catch (NulsException e) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR);
        }

        Transaction tx = ledgerService.getTx(txHash);
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.DATA_NOT_FOUND);
        }
        TransactionDto dto = new TransactionDto(tx);
        Result result = Result.getSuccess();
        result.setData(dto);

        return result;
    }
}
