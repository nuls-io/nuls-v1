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
package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.constant.RpcCmdConstant;
import io.nuls.rpc.sdk.entity.BlockDto;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/3/25
 */
public enum BlockService {
    BLOCK_SERVICE;
    private RestFulUtils restFul = RestFulUtils.getInstance();

    /**
     * @param hash
     * @return
     */
    public RpcClientResult getBlock(String hash) {
        try {
            AssertUtil.canNotEmpty(hash, "block hash cannot null!");
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        RpcClientResult result = restFul.get("/block/hash/" + hash, null);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), true));
        }
        return result;
    }


    /**
     * @param height
     * @return
     */
    public RpcClientResult getBlock(int height) {
        if(height < 0){
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        RpcClientResult result = restFul.get("/block/height/" + height, null);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), true));
        }
        return result;
    }

    /**
     * @param hash
     * @return
     */
    public RpcClientResult getBlockHeader(String hash) {
        try {
            AssertUtil.canNotEmpty(hash, "block hash cannot null!");
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        RpcClientResult result = restFul.get("/block/header/hash/" + hash, null);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }

    /**
     * @param height
     * @return
     */
    public RpcClientResult getBlockHeader(int height) {
        if(height < 0){
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        RpcClientResult result = restFul.get("/block/header/height/" + height, null);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }


    /**
     * @return
     */
    public RpcClientResult getBestBlockHeader() {
        RpcClientResult result = restFul.get("/block/newest", null);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }

    /**
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public RpcClientResult listBlockHeader(int pageNumber, int pageSize) {
        if(pageNumber < 0 || pageSize < 0 ){
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        } else if (pageSize > 100) {
            pageSize = 100;
        }
        Map<String, String> param = new HashMap<>(2);
        param.put("pageNumber", String.valueOf(pageNumber));
        param.put("pageSize", String.valueOf(pageSize));
        RpcClientResult result = restFul.get("/block/list", param);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }

}
