/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.ledger.base.module.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.ledger.base.manager.BalanceManager;
import io.nuls.account.ledger.module.AbstractAccountLedgerModule;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.protocol.constant.ProtocolConstant;


/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/8
 */
public class AccountLedgerModuleBootstrap extends AbstractAccountLedgerModule {


    @Override
    public void init() {
    }

    @Override
    public void start() {
        this.waitForDependencyRunning(AccountConstant.MODULE_ID_ACCOUNT, ProtocolConstant.MODULE_ID_PROTOCOL);
        BalanceManager balanceManager = NulsContext.getServiceBean(BalanceManager.class);
        balanceManager.initAccountBalance();
    }

    @Override
    public void shutdown() {
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        return null;
    }
}
