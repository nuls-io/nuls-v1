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
package io.nuls.kernel.module.thread;

import java.util.concurrent.ThreadFactory;

/**
 * @author Niels
 */
public class ModuleProcessFactory implements ThreadFactory {

    private static final String POOL_NAME = "Process";

    @Override
    public ModuleProcess newThread(Runnable r) {
        if (null == r) {
            throw new RuntimeException("runnable cannot be null!");
        }
        if (!(r instanceof ModuleRunner)) {
            throw new RuntimeException("unkown runnable!");
        }
        ModuleProcess process = new ModuleProcess((ModuleRunner) r);
        process.setModuleId((short) 0);
        process.setName(POOL_NAME + "-" + ((ModuleRunner) r).getModuleKey());
        process.setPoolName(POOL_NAME);
        process.setDaemon(false);
        return process;
    }
}
