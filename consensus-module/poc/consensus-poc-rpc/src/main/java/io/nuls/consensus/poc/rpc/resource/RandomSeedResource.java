package io.nuls.consensus.poc.rpc.resource;

import io.nuls.consensus.poc.rpc.model.RandomSeedDTO;
import io.nuls.consensus.poc.storage.po.RandomSeedPo;
import io.nuls.consensus.poc.storage.po.RandomSeedStatusPo;
import io.nuls.consensus.poc.storage.service.RandomSeedsStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Sha3Hash;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.swagger.annotations.*;

import javax.ws.rs.*;
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
        byte[] seed = null;
        RandomSeedDTO dto = new RandomSeedDTO();
        if ("SHA3".equals(algorithm.trim().toUpperCase())) {
            byte[] bytes = ArraysTool.concatenate(list.toArray(new byte[list.size()][]));
            seed = Sha3Hash.sha3bytes(bytes, 256);
            dto.setAlgorithm(algorithm);
        }
        BigInteger value = new BigInteger(seed);
        dto.setSeed(value.toString());

        return Result.getSuccess().setData(dto).toRpcClientResult();
    }
}
