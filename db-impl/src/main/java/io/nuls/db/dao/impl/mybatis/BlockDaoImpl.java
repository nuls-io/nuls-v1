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
package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.dto.Page;
import io.nuls.db.dao.BlockHeaderService;
import io.nuls.db.dao.impl.mybatis.mapper.BlockHeaderMapper;
import io.nuls.db.dao.impl.mybatis.params.BlockSearchParams;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author v.chou
 * @date 2017/9/29
 */
@DbSession(transactional = PROPAGATION.NONE)
public class BlockDaoImpl extends BaseDaoImpl<BlockHeaderMapper, String, BlockHeaderPo> implements BlockHeaderService {

    public BlockDaoImpl() {
        super(BlockHeaderMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new BlockSearchParams(params);
    }

    @Override
    public BlockHeaderPo getHeader(long height) {
        Map<String, Object> params = new HashMap<>();
        params.put(BlockSearchParams.SEARCH_FIELD_HEIGHT, height);
        List<BlockHeaderPo> list = this.getList(params);
        if (null == list || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public long getBestHeight() {
        Long value = this.getMapper().getMaxHeight();
        if (null == value) {
            return 0;
        }
        return value.longValue();
    }

    @Override
    public BlockHeaderPo getBestBlockHeader() {
        return getHeader(getBestHeight());
    }

    @Override
    public BlockHeaderPo getHeader(String hash) {
        return getMapper().selectByPrimaryKey(hash);
    }


    @Override
    public List<BlockHeaderPo> getHeaderList(long startHeight, long endHeight) {
        Map<String, Object> map = new HashMap<>();
        map.put(BlockSearchParams.SEARCH_FIELD_HEIGHT_START, startHeight);
        map.put(BlockSearchParams.SEARCH_FIELD_HEIGHT_END, endHeight);
        return this.getList(map);
    }

    @Override
    public List<BlockHeaderPo> getHashList(long startHeight, long endHeight, long split) {
        Map<String, Object> params = new HashMap<>();
        params.put("startHeight", startHeight);
        params.put("endHeight", endHeight);
        params.put("split", split);
        return getMapper().getSplitHashList(params);
    }

    @Override
    public Page<BlockHeaderPo> getBlockListByAddress(String nodeAddress, int type, int pageNumber, int pageSize) {
        Searchable searchable = new Searchable();
        if (type == 1) {
            searchable.addCondition("a.agent_address", SearchOperator.eq, nodeAddress);
        } else {
            searchable.addCondition("a.packing_address", SearchOperator.eq, nodeAddress);
        }

        PageHelper.startPage(pageNumber, pageSize);
        PageHelper.orderBy("b.height desc");
        List<BlockHeaderPo> blockList = getMapper().getBlockByAddress(searchable);
        PageInfo<BlockHeaderPo> pageInfo = new PageInfo<>(blockList);
        Page<BlockHeaderPo> page = new Page<>();
        page.setTotal(pageInfo.getTotal());
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setPages(pageInfo.getPages());
        page.setList(blockList);
        return page;
    }

    @Override
    public Page<BlockHeaderPo> getBlockHeaderList(int pageNumber, int pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        PageHelper.orderBy("height desc");
        List<BlockHeaderPo> blockList = getMapper().selectList(new Searchable());
        PageInfo<BlockHeaderPo> pageInfo = new PageInfo<>(blockList);
        Page<BlockHeaderPo> page = new Page<>();
        page.setTotal(pageInfo.getTotal());
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setPages(pageInfo.getPages());
        page.setList(blockList);
        return page;
    }

    @Override
    public long getCount(String address, long roundStart, long roundEnd) {
        Map<String, Object> map = new HashMap<>();
        map.put(BlockSearchParams.SEARCH_FIELD_ADDRESS, address);
        if (roundEnd >= 0) {
            map.put(BlockSearchParams.SEARCH_FIELD_ROUND_START, roundStart);
        }
        if (roundStart >= 0) {
            map.put(BlockSearchParams.SEARCH_FIELD_ROUND_END, roundEnd);
        }
        return getCount(map);
    }

    @Override
    public List<Long> getListOfRoundIndexOfYellowPunish(String address, long startRoundIndex, long endRoundIndex) {
        Map<String, Object> params = new HashMap<>();
        params.put("address", address);
        params.put("startRoundIndex", startRoundIndex);
        params.put("endRoundIndex", endRoundIndex);
        params.put("txType", TransactionConstant.TX_TYPE_YELLOW_PUNISH);
        return this.getMapper().getSumOfRoundIndexOfYellowPunish(params);
    }

}
