/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.account.module;

import io.nuls.account.service.AccountService;
import io.nuls.kernel.context.NulsContext;

/**
 * @author: Niels Wang
 */
public class AccountModuleBootstrap extends AbstractAccountModuleBootstrap {
    /**
     *
     */
    @Override
    public void init() throws Exception {

    }

    /**
     * start the module
     */
    @Override
    public void start() {
        AccountService accountService = NulsContext.getServiceBean(AccountService.class);
        accountService.getAccountList();
    }

    /**
     * stop the module
     */
    @Override
    public void shutdown() {

    }

    /**
     * destroy the module
     */
    @Override
    public void destroy() {

    }

    /**
     * get all info of the module
     */
    @Override
    public String getInfo() {
        return null;
    }
}
