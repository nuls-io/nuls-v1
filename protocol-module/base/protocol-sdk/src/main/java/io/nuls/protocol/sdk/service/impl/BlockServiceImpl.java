package io.nuls.protocol.sdk.service.impl;

import io.nuls.protocol.sdk.model.*;
import io.nuls.protocol.sdk.service.BlockService;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.constant.KernelErrorCode;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.utils.JSONUtils;
import io.nuls.sdk.utils.RestFulUtils;
import io.nuls.sdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/6/13
 */
public class BlockServiceImpl implements BlockService {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getNewestBlockHeader() {
        Result result = restFul.get("/block/newest", null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map)result.getData();
        BlockHeaderDto blockHeaderDto = new BlockHeaderDto(map);
        return result.setData(blockHeaderDto);
    }

    @Override
    public Result getBlockHeader(int height) {
        if(height < 0){
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/header/height/" + height, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map)result.getData();
        BlockHeaderDto blockHeaderDto = new BlockHeaderDto(map);
        return result.setData(blockHeaderDto);
    }

    @Override
    public Result getBlockHeader(String hash) {
        if(StringUtils.isBlank(hash)){
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/header/hash/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map)result.getData();
        BlockHeaderDto blockHeaderDto = new BlockHeaderDto(map);
        return result.setData(blockHeaderDto);
    }

    @Override
    public Result getBlock(int height) {
        if(height < 0){
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/height/" + height, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map)result.getData();
        return result.setData(assembleBlockDto(map));
    }

    @Override
    public Result getBlock(String hash) {
        if(StringUtils.isBlank(hash)){
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/hash/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map)result.getData();
        return result.setData(assembleBlockDto(map));
    }

    private BlockDto assembleBlockDto(Map<String, Object> map){
        List<Map<String, Object>> txMapList = (List<Map<String, Object>>)map.get("txList");
        List<TransactionDto> txList = new ArrayList<>();
        for (Map<String, Object> txMap : txMapList) {
            //重新组装input
            List<Map<String, Object>> inputMaps = (List<Map<String, Object>>) txMap.get("inputs");
            List<InputDto> inputs = new ArrayList<>();
            for (Map<String, Object> inputMap : inputMaps) {
                InputDto inputDto = new InputDto(inputMap);
                inputs.add(inputDto);
            }
            txMap.put("inputs", inputs);
            //重新组装output
            List<Map<String, Object>> outputMaps = (List<Map<String, Object>>) txMap.get("outputs");
            List<OutputDto> outputs = new ArrayList<>();
            for (Map<String, Object> outputMap : outputMaps) {
                OutputDto outputDto = new OutputDto(outputMap);
                outputs.add(outputDto);
            }
            txMap.put("outputs", outputs);
            TransactionDto transactionDto = new TransactionDto(txMap);
            txList.add(transactionDto);
        }
        map.put("txList", txList);
        return new BlockDto(map);
    }

    public static void main(String[] args) {
        SDKBootstrap.sdkStart();
        BlockService bs = new BlockServiceImpl();
        try {
//            System.out.println(JSONUtils.obj2json(bs.getBlockHeader(4)));
//            System.out.println(JSONUtils.obj2json(bs.getBlockHeader("00207380c6fca01cbbecba8ad24dba57659713b96cac5ff90d8d844e4be97f6625ad")));
            System.out.println(JSONUtils.obj2json(bs.getNewestBlockHeader()));
//            System.out.println(JSONUtils.obj2json(bs.getBlock(1884)));
//            System.out.println(JSONUtils.obj2json(bs.getBlock("00209bbcd98110b57f1ecd66c9d94d1a2381e6c03c3b9aa77db25b6eb5955bb658d3")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
