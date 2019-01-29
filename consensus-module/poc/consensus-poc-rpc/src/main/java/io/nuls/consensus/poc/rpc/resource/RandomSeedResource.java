package io.nuls.consensus.poc.rpc.resource;

import io.nuls.consensus.poc.rpc.model.RandomSeedDTO;
import io.nuls.consensus.poc.storage.service.RandomSeedsStorageService;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.RandomSeedCaculator;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 */
@Path("/random")
@Api(value = "/random", description = "random seed")
@Component
public class RandomSeedResource {

    @Autowired
    private RandomSeedsStorageService randomSeedService;

    private NulsContext context = NulsContext.getInstance();

    @GET
    @Path("/seed/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据高度和原始种子个数生成一个随机种子并返回")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RandomSeedDTO.class)
    })
    public RpcClientResult getSeedByCount(@ApiParam(name = "height", value = "最大高度", required = true)
                                          @QueryParam("height") long height, @ApiParam(name = "count", value = "原始种子个数", required = true)
                                          @QueryParam("count") int count, @ApiParam(name = "algorithm", value = "算法标识：SHA3...", required = true)
                                          @QueryParam("algorithm") String algorithm) {
        if (height > context.getBestHeight() || count > 128 || count <= 0 || StringUtils.isBlank(algorithm)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        List<byte[]> list = randomSeedService.getSeeds(height, count);
        if (list.size() != count) {
            return Result.getFailed().toRpcClientResult();
        }
        byte[] seed = RandomSeedCaculator.clac(list, algorithm);
        if (null == seed) {
            return Result.getFailed().toRpcClientResult();
        }
        RandomSeedDTO dto = new RandomSeedDTO();
        dto.setCount(list.size());
        dto.setAlgorithm(algorithm);
        BigInteger value = new BigInteger(seed);
        dto.setSeed(value.toString());

        return Result.getSuccess().setData(dto).toRpcClientResult();
    }

    @GET
    @Path("/seed/height")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据高度区间生成一个随机种子并返回")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RandomSeedDTO.class)
    })
    public RpcClientResult getSeedByHeight(@ApiParam(name = "startHeight", value = "起始高度", required = true)
                                           @QueryParam("startHeight") long startHeight, @ApiParam(name = "endHeight", value = "截止高度", required = true)
                                           @QueryParam("endHeight") long endHeight, @ApiParam(name = "algorithm", value = "算法标识：SHA3...", required = true)
                                           @QueryParam("algorithm") String algorithm) {
        if (endHeight > context.getBestHeight() || startHeight <= 0 || StringUtils.isBlank(algorithm)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        List<byte[]> list = randomSeedService.getSeeds(startHeight, endHeight);
        if (list.isEmpty()) {
            return Result.getFailed().toRpcClientResult();
        }
        byte[] seed = RandomSeedCaculator.clac(list, algorithm);
        if (null == seed) {
            return Result.getFailed().toRpcClientResult();
        }
        RandomSeedDTO dto = new RandomSeedDTO();
        dto.setCount(list.size());
        dto.setAlgorithm(algorithm);
        BigInteger value = new BigInteger(seed);
        dto.setSeed(value.toString());

        return Result.getSuccess().setData(dto).toRpcClientResult();
    }

    @GET
    @Path("/seeds/count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据高度查找原始种子列表并返回")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult getSeedsByCount(@ApiParam(name = "height", value = "最大高度", required = true)
                                           @QueryParam("height") long height, @ApiParam(name = "count", value = "原始种子个数", required = true)
                                           @QueryParam("count") int count) {
        if (height > context.getBestHeight() || count > 128 || count <= 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        List<byte[]> list = randomSeedService.getSeeds(height, count);
        if (list.size() != count) {
            return Result.getFailed().toRpcClientResult();
        }
        List<String> seeds = new ArrayList<>();
        for (byte[] value : list) {
            seeds.add(new BigInteger(value).toString());
        }
        return Result.getSuccess().setData(seeds).toRpcClientResult();
    }

    @GET
    @Path("/seeds/height")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据高度区间查询原始种子列表并返回")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = RandomSeedDTO.class)
    })
    public RpcClientResult getSeedsByHeight(@ApiParam(name = "startHeight", value = "起始高度", required = true)
                                            @QueryParam("startHeight") long startHeight, @ApiParam(name = "endHeight", value = "截止高度", required = true)
                                            @QueryParam("endHeight") long endHeight) {
        if (endHeight > context.getBestHeight() || startHeight <= 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        List<byte[]> list = randomSeedService.getSeeds(startHeight, endHeight);
        if (list.isEmpty()) {
            return Result.getFailed().toRpcClientResult();
        }
        List<String> seeds = new ArrayList<>();
        for (byte[] value : list) {
            seeds.add(new BigInteger(value).toString());
        }
        return Result.getSuccess().setData(seeds).toRpcClientResult();
    }
}
