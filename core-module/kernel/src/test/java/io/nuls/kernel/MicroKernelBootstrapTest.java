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

package io.nuls.kernel;

import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.validate.ValidatorManager;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 */
public class MicroKernelBootstrapTest {

    private MicroKernelBootstrap bootstrap = MicroKernelBootstrap.getInstance();

    @Test
    public void init() {
        bootstrap.init();
        assertNotNull(NulsConfig.MODULES_CONFIG);
        assertNotNull(NulsConfig.NULS_CONFIG);
        assertTrue(SpringLiteContext.isInitSuccess());
        assertNotNull(NulsConfig.VERSION);
        assertNotNull(ValidatorManager.isInitSuccess());
    }

    @Test
    public void test(){
        getInfo();
        start();
        shutdown();
        destroy();
    }

    public void getInfo() {
        this.init();
        String info = bootstrap.getInfo();
        System.out.println(info);
        assertTrue(true);
    }

    public void start() {
        bootstrap.start();
        assertTrue(true);
    }

    public void shutdown() {
        bootstrap.shutdown();
        assertTrue(true);
    }

    public void destroy() {
        bootstrap.destroy();
        assertTrue(true);
    }

}