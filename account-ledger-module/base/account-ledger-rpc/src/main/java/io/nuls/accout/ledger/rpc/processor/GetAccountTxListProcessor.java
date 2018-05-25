package io.nuls.accout.ledger.rpc.processor;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.accout.ledger.rpc.dto.TransactionInfoDto;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.ledger.service.LedgerService;

import java.util.ArrayList;
import java.util.List;

@Cmd
@Component
public class GetAccountTxListProcessor implements CommandProcessor {

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public String getCommand() {
        return "gettxlist";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   address -required")
                .newLine("\t[txType]    transaction type -default 0")
                .newLine("\t<start>     start -required")
                .newLine("\t<limit>     limit -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "gettxlist <address> <start> <limit> --get the transaction information list by address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (!(length == 4 || length == 5)) {
            return false;
        }


        if (args.length == 4) {
            try {
                Integer.parseInt(args[2]);
                Integer.parseInt(args[3]);
            } catch (Exception e) {
                return false;
            }
        } else {
            try {
                Integer.parseInt(args[2]);
                Integer.parseInt(args[3]);
                Integer.parseInt(args[4]);
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Result execute(String[] args) {
        Result<List<TransactionInfoDto>> dtoResult = Result.getSuccess();
        byte[] addressBytes = null;
        int type = 0;
        int start = 0;
        int limit = 0;

//        try {
//            addressBytes = Base58.decode(args[1]);
//        } catch (Exception e) {
//            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
//        }

        if (args.length == 4) {
            start = Integer.parseInt(args[2]);
            limit = Integer.parseInt(args[3]);
        } else {
            type = Integer.parseInt(args[2]);
            start = Integer.parseInt(args[3]);
            limit = Integer.parseInt(args[4]);
        }

        Result<List<TransactionInfo>> rawResult = accountLedgerService.getTxInfoList(addressBytes);
//        if (rawResult.isFailed()) {
//            dtoResult.setSuccess(false);
//            dtoResult.setErrorCode(rawResult.getErrorCode());
//            return dtoResult;
//        }
        List<TransactionInfo> infoList = rawResult.getData();
        if (type != 0) {
            for (int i = infoList.size() - 1; i >= 0; i--) {
                if (infoList.get(i).getTxType() != type) {
                    infoList.remove(i);
                }
            }
        }

//        if (start > infoList.size() || infoList.size() == 0) {
//            return dtoResult;
//        }
        int end = start + limit;
        if (end > infoList.size()) {
            end = infoList.size();
        }

        infoList = infoList.subList(start, end);
        List<TransactionInfoDto> infoDtoList = new ArrayList<>();
        for (TransactionInfo info : infoList) {

            Transaction tx = ledgerService.getTx(info.getTxHash());
            if (tx == null) {
                tx = accountLedgerService.getUnconfirmedTransaction(info.getTxHash()).getData();
            }
            info.setInfo(tx.getInfo(addressBytes));
            infoDtoList.add(new TransactionInfoDto(info));
        }

        dtoResult.setData(infoDtoList);
//        return dtoResult;
        return null;
    }
}
