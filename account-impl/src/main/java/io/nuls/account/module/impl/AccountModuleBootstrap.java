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
package io.nuls.account.module.impl;

import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.event.*;
import io.nuls.account.module.intf.AbstractAccountModule;
import io.nuls.account.service.impl.AccountServiceImpl;
import io.nuls.account.service.intf.AccountService;
import io.nuls.account.service.tx.AliasTxService;
import io.nuls.ledger.entity.listener.CoinDataTxService;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.manager.EventManager;
import io.nuls.protocol.utils.TransactionManager;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class AccountModuleBootstrap extends AbstractAccountModule {

    private AccountService accountService;

    @Override
    public void init() {
        EventManager.putEvent(AccountCreateNotice.class);
        EventManager.putEvent(AccountImportedNotice.class);
        EventManager.putEvent(DefaultAccountChangeNotice.class);
        EventManager.putEvent(PasswordChangeNotice.class);
        EventManager.putEvent(SetAliasNotice.class);
        this.registerService(AccountServiceImpl.class);
        accountService = NulsContext.getServiceBean(AccountService.class);
        this.registerService(AliasTxService.class);
        TransactionManager.putTx(TransactionConstant.TX_TYPE_SET_ALIAS, AliasTransaction.class, AliasTxService.class);
    }


    @Override
    public void start() {
        accountService.start();
    }

    @Override
    public void shutdown() {
        accountService.shutdown();
    }

    @Override
    public void destroy() {
        accountService.destroy();
    }

    @Override
    public String getInfo() {
        return "account module is " + this.getStatus();
    }
}
