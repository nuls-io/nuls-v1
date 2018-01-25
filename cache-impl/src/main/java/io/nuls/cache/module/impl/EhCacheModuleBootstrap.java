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
package io.nuls.cache.module.impl;

import io.nuls.cache.constant.EhCacheConstant;
import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.module.AbstractCacheModule;
import io.nuls.cache.service.impl.EhCacheServiceImpl;
import io.nuls.cache.service.intf.CacheService;

/**
 *
 * @author Niels
 * @date 2017/10/27
 *
 */
public class EhCacheModuleBootstrap extends AbstractCacheModule {

    private EhCacheManager cacheManager = EhCacheManager.getInstance();

    @Override
    public void init() {
    }

    @Override
    public void start() {

        this.registerService(EhCacheServiceImpl.getInstance());
    }

    @Override
    public void shutdown() {
        cacheManager.close();
    }

    @Override
    public void destroy() {
        cacheManager.close();
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return EhCacheConstant.CACHE_MODULE_VERSION;
    }

}
