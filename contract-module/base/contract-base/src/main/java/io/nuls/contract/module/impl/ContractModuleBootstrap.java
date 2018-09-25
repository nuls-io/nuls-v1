/**
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
 */
package io.nuls.contract.module.impl;


import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.contract.entity.tx.CallContractTransaction;
import io.nuls.contract.entity.tx.processor.CreateContractTxProcessor;
import io.nuls.contract.ledger.manager.ContractBalanceManager;
import io.nuls.contract.module.AbstractContractModule;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.tools.io.StringFileLoader;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.utils.TransactionManager;

import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/4/20
 */
public class ContractModuleBootstrap extends AbstractContractModule {

    private final static String NRC20_STANDARD_FILE = "contract/nrc20.json";
    /**
     * execute when the project starts.
     */
    @Override
    public void init() {
        Log.debug("contract init");
        initERC20Standard();
    }

    private void initERC20Standard() {
        String json = null;
        try {
            json = StringFileLoader.read(NRC20_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC20Standard error.", e);
        }
        if(json == null) {
            return;
        }

        Map<String, ProgramMethod> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json, ProgramMethod.class);
        } catch (Exception e) {
            Log.error("init NRC20Standard map error.", e);
        }
        VMContext.setNrc20Methods(jsonMap);
    }

    /**
     * execute when the project starts.
     */
    @Override
    public void start() {
        Log.debug("contract start");
        this.waitForDependencyRunning(ConsensusConstant.MODULE_ID_CONSENSUS);
        ContractBalanceManager balanceManager = NulsContext.getServiceBean(ContractBalanceManager.class);
        balanceManager.initContractBalance();
        balanceManager.initAllTokensForAllAccounts();
    }

    @Override
    public void shutdown() {
        //TODO do something or not
    }

    @Override
    public void destroy() {
        //TODO do something or not
    }

    @Override
    public String getInfo() {
        return "contract module is " + this.getStatus();
    }
}
