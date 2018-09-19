package io.nuls.contract.util;


import io.nuls.contract.entity.BlockHeaderDto;
import io.nuls.contract.ledger.service.ContractUtxoService;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.service.BlockService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/2
 */
@Component
public class VMContext {

    @Autowired
    private BlockService blockService;

    @Autowired
    private ContractUtxoService contractUtxoService;

    public static Map<String, ProgramMethod> NRC20_METHODS = null;

    /**
     * @param hash
     * @return
     * @throws NulsException
     * @throws IOException
     */
    public BlockHeaderDto getBlockHeader(String hash) throws NulsException, IOException {
        if(StringUtils.isBlank(hash)) {
            return null;
        }
        NulsDigestData nulsDigestData = NulsDigestData.fromDigestHex(hash);
        Result<BlockHeader> blockHeaderResult = blockService.getBlockHeader(nulsDigestData);
        if(blockHeaderResult == null || blockHeaderResult.getData() == null) {
            return null;
        }
        BlockHeaderDto header = new BlockHeaderDto(blockHeaderResult.getData());
        return header;
    }

    /**
     * @param height
     * @return
     * @throws NulsException
     * @throws IOException
     */
    public BlockHeaderDto getBlockHeader(long height) throws NulsException, IOException {
        if(height < 0L) {
            return null;
        }
        Result<BlockHeader> blockHeaderResult = blockService.getBlockHeader(height);
        if(blockHeaderResult == null || blockHeaderResult.getData() == null) {
            return null;
        }
        BlockHeaderDto header = new BlockHeaderDto(blockHeaderResult.getData());
        return header;
    }

    /**
     * get the newest block header
     * @return
     * @throws IOException
     */
    public BlockHeaderDto getNewestBlockHeader() throws IOException {
        return new BlockHeaderDto(NulsContext.getInstance().getBestBlock().getHeader());
    }

    /**
     * @param address
     */
    public BigInteger getBalance(byte[] address) {
        Result<BigInteger> result = contractUtxoService.getBalance(address);
        if(result.isSuccess()) {
            //Log.info("=====*************============contract - " + AddressTool.getStringAddressByBytes(address) + ", balance: " + result.getData().toString());
            return result.getData();
        }
        //Log.info("=====*************============contract - " + AddressTool.getStringAddressByBytes(address) + ", balance: 0");
        return BigInteger.ZERO;
    }

    public static Map<String, ProgramMethod> getNrc20Methods() {
        return NRC20_METHODS;
    }

    public static void setNrc20Methods(Map<String, ProgramMethod> nrc20Methods) {
        NRC20_METHODS = nrc20Methods;
    }
}
