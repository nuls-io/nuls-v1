/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.storage.service.impl;

import io.nuls.consensus.poc.storage.service.OrphanStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;

import java.io.IOException;

/**
 * Created by ln on 2018/5/8.
 */
@Component
public class OrphanStorageServiceImpl implements OrphanStorageService, InitializingBean {

    private final String DB_NAME = "orphan";

    @Autowired
    private DBService dbService;

    @Override
    public boolean save(Block block) {

        assert (block != null);

        Result result = null;
        try {
            result = dbService.put(DB_NAME, block.getHeader().getHash().getDigestBytes(), block.serialize());
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        return result.isSuccess();
    }

    @Override
    public Block get(NulsDigestData key) {

        assert (key != null);

        byte[] content = dbService.get(DB_NAME, key.getDigestBytes());
        Block block = new Block();
        try {
            block.parse(content);
        } catch (NulsException e) {
            Log.error(e);
        }

        return block;
    }

    @Override
    public boolean delete(NulsDigestData key) {

        assert (key != null);

        Result result = dbService.delete(DB_NAME, key.getDigestBytes());

        return result.isSuccess();
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.createArea(DB_NAME);
    }
}