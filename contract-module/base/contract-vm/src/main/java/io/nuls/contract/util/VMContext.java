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
package io.nuls.contract.util;


import io.nuls.contract.entity.BlockHeaderDto;
import io.nuls.contract.ledger.module.ContractBalance;
import io.nuls.contract.ledger.service.ContractUtxoService;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
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
    public BlockHeaderDto getNewestBlockHeader()  {
        return new BlockHeaderDto(NulsContext.getInstance().getBestBlock().getHeader());
    }

    /**
     * @param address
     */
    public BigInteger getBalance(byte[] address, Long blockHeight) {
        Result<ContractBalance> result = contractUtxoService.getBalance(address, blockHeight);
        if(result.isSuccess()) {
            ContractBalance balance = result.getData();
            // pierre test comment out
            return BigInteger.valueOf(balance.getRealUsable().getValue());
        }
        return BigInteger.ZERO;
    }

    /**
     * @param address
     */
    public BigInteger getTotalBalance(byte[] address, Long blockHeight) {
        Result<ContractBalance> result = contractUtxoService.getBalance(address, blockHeight);
        if(result.isSuccess()) {
            ContractBalance balance = result.getData();
            return BigInteger.valueOf(balance.getBalance().getValue());
        }
        return BigInteger.ZERO;
    }

    public static Map<String, ProgramMethod> getNrc20Methods() {
        return NRC20_METHODS;
    }

    public static void setNrc20Methods(Map<String, ProgramMethod> nrc20Methods) {
        NRC20_METHODS = nrc20Methods;
    }
}
