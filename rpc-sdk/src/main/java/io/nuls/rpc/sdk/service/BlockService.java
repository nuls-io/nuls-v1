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
import io.nuls.rpc.sdk.entity.AccountDto;
import io.nuls.rpc.sdk.entity.BlockDto;
import io.nuls.rpc.sdk.entity.BlockNa2NulsDto;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private RpcClientResult getBlockBase(String hash) {
        try {
            AssertUtil.canNotEmpty(hash, "block hash cannot null!");
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.get("/block/hash/" + hash, null);
    }
    public RpcClientResult getBlock(String hash) {
        RpcClientResult result = getBlockBase(hash);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), true));
        }
        return result;
    }
    public RpcClientResult getBlockNa2Nuls(String hash) {
        RpcClientResult result = getBlockBase(hash);
        if (result.isSuccess()) {
            result.setData(new BlockNa2NulsDto((Map<String, Object>) result.getData(), true));
        }
        return result;
    }


    /**
     * @param height
     * @return
     */
    private RpcClientResult getBlockBase(int height) {
        if(height < 0){
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        return restFul.get("/block/height/" + height, null);
    }
    public RpcClientResult getBlock(int height) {
        RpcClientResult result = getBlockBase(height);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), true));
        }
        return result;
    }
    public RpcClientResult getBlockNa2Nuls(int height) {
        RpcClientResult result = getBlockBase(height);
        if (result.isSuccess()) {
            result.setData(new BlockNa2NulsDto((Map<String, Object>) result.getData(), true));
        }
        return result;
    }

    /**
     * @param hash
     * @return
     */
    private RpcClientResult getBlockHeaderBase(String hash) {
        try {
            AssertUtil.canNotEmpty(hash, "block hash cannot null!");
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.get("/block/header/hash/" + hash, null);
    }
    public RpcClientResult getBlockHeader(String hash) {
        RpcClientResult result = getBlockHeaderBase(hash);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }
    public RpcClientResult getBlockHeaderNa2Nuls(String hash) {
        RpcClientResult result = getBlockHeaderBase(hash);
        if (result.isSuccess()) {
            result.setData(new BlockNa2NulsDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }

    /**
     * @param height
     * @return
     */
    private RpcClientResult getBlockHeaderBase(int height) {
        if(height < 0){
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        return restFul.get("/block/header/height/" + height, null);
    }
    public RpcClientResult getBlockHeader(int height) {
        RpcClientResult result = getBlockHeaderBase(height);
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }
    public RpcClientResult getBlockHeaderNa2Nuls(int height) {
        RpcClientResult result = getBlockHeaderBase(height);
        if (result.isSuccess()) {
            result.setData(new BlockNa2NulsDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }


    /**
     * @return
     */
    private RpcClientResult getBestBlockHeaderBase() {
        return restFul.get("/block/newest", null);
    }
    public RpcClientResult getBestBlockHeader() {
        RpcClientResult result = getBestBlockHeaderBase();
        if (result.isSuccess()) {
            result.setData(new BlockDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }
    public RpcClientResult getBestBlockHeaderNa2Nuls() {
        RpcClientResult result = getBestBlockHeaderBase();
        if (result.isSuccess()) {
            result.setData(new BlockNa2NulsDto((Map<String, Object>) result.getData(), false));
        }
        return result;
    }

    /**
     * @param pageNumber
     * @param pageSize
     * @return
     */
    private RpcClientResult listBlockHeaderBase(int pageNumber, int pageSize) {
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
        return restFul.get("/block/list", param);
    }
    public RpcClientResult listBlockHeader(int pageNumber, int pageSize) {
        RpcClientResult result = listBlockHeaderBase(pageNumber, pageSize);
        if (result.isSuccess()) {
            Map<String, Object> dataMap = (Map<String, Object>)result.getData();
            if(dataMap != null) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) dataMap.get("list");
                List<BlockDto> blockDtoList = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    blockDtoList.add(new BlockDto(map, false));
                }
                result.setData(blockDtoList);
            }
        }
        return result;
    }
    public RpcClientResult listBlockHeaderNa2Nuls(int pageNumber, int pageSize) {
        RpcClientResult result = listBlockHeaderBase(pageNumber, pageSize);
        if (result.isSuccess()) {
            Map<String, Object> dataMap = (Map<String, Object>)result.getData();
            if(dataMap != null) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) dataMap.get("list");
                List<BlockNa2NulsDto> blockNa2NulsDtoList = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    blockNa2NulsDtoList.add(new BlockNa2NulsDto(map, false));
                }
                result.setData(blockNa2NulsDtoList);
            }
        }
        return result;
    }

}
