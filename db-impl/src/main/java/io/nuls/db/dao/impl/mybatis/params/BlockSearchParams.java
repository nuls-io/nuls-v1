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
package io.nuls.db.dao.impl.mybatis.params;

import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class BlockSearchParams extends Searchable {
    public static final String SEARCH_FIELD_HASH = "hash";
    public static final String SEARCH_FIELD_HEIGHT = "height";
    public static final String SEARCH_FIELD_ADDRESS = "consensus_address";

    public static final String SEARCH_FIELD_HEIGHT_START = "startHeight";
    public static final String SEARCH_FIELD_HEIGHT_END = "endHeight";
    public static final String SEARCH_FIELD_ROUND = "round_index";
    public static final String SEARCH_FIELD_ROUND_START = "startRound";
    public static final String SEARCH_FIELD_ROUND_END = "endRound";
    public static final String SEARCH_FIELD_PRE_HASH = "pre_Hash";
    public static final String SEARCH_FIELD_MERKLE_HASH = "merkle_hash";

    public BlockSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_HASH)) {
            this.addCondition(SEARCH_FIELD_HASH, SearchOperator.eq, params.get(SEARCH_FIELD_HASH));
        }
        if (params.containsKey(SEARCH_FIELD_HEIGHT)) {
            this.addCondition(SEARCH_FIELD_HEIGHT, SearchOperator.eq, params.get(SEARCH_FIELD_HEIGHT));
        }
        if (params.containsKey(SEARCH_FIELD_PRE_HASH)) {
            this.addCondition(SEARCH_FIELD_PRE_HASH, SearchOperator.eq, params.get(SEARCH_FIELD_PRE_HASH));
        }
        if (params.containsKey(SEARCH_FIELD_MERKLE_HASH)) {
            this.addCondition(SEARCH_FIELD_MERKLE_HASH, SearchOperator.eq, params.get(SEARCH_FIELD_MERKLE_HASH));
        }
        if (params.containsKey(SEARCH_FIELD_HEIGHT_START)) {
            this.addCondition(SEARCH_FIELD_HEIGHT, SearchOperator.gte, params.get(SEARCH_FIELD_HEIGHT_START));
        }
        if (params.containsKey(SEARCH_FIELD_HEIGHT_END)) {
            this.addCondition(SEARCH_FIELD_HEIGHT, SearchOperator.lte, params.get(SEARCH_FIELD_HEIGHT_END));
        }

        if (params.containsKey(SEARCH_FIELD_ROUND_START)) {
            this.addCondition(SEARCH_FIELD_ROUND, SearchOperator.gt, params.get(SEARCH_FIELD_ROUND_START));
        }
        if (params.containsKey(SEARCH_FIELD_ROUND_END)) {
            this.addCondition(SEARCH_FIELD_ROUND, SearchOperator.lte, params.get(SEARCH_FIELD_ROUND_END));
        }
        if (params.containsKey(SEARCH_FIELD_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_ADDRESS));
        }

    }
}
