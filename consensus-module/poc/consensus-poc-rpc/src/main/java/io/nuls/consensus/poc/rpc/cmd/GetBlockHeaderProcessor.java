package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.protocol.service.BlockService;

@Cmd
@Component
public class GetBlockHeaderProcessor implements CommandProcessor {

    @Autowired
    private BlockService blockService;

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
    public RpcClientResult execute(String[] args) {
        NulsDigestData blockHash = null;
        long blockHeight = 0;

        try {
            blockHeight = Long.parseLong(args[1]);
        } catch (Exception e) {
            try {
                blockHash = NulsDigestData.fromDigestHex(args[1]);
            } catch (NulsException e1) {
                e1.printStackTrace();
            }
        }

        Result<BlockHeader> result = null;
        if(blockHash != null) {
            result = blockService.getBlockHeader(blockHash);
        }else {
            result = blockService.getBlockHeader(blockHeight);
        }

        return null;
    }
}
