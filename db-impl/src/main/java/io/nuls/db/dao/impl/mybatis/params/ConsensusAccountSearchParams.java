/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
public class ConsensusAccountSearchParams extends Searchable {
    public static final String SEARCH_FIELD_ADDRESS = "address";
    public static final String SEARCH_FIELD_AGENT_ADDRESS = "agent_address";
    public static final String SEARCH_FIELD_DEPOSIT = "deposit";
    public static final String SEARCH_FIELD_DEPOSIT_GREAT_THAN = "deposit_gt";
    public static final String SEARCH_FIELD_DEPOSIT_LESS_THAN = "deposit_lt";

    public ConsensusAccountSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_ADDRESS));
        }
        if (params.containsKey(SEARCH_FIELD_AGENT_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_AGENT_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_AGENT_ADDRESS));
        }
        if (params.containsKey(SEARCH_FIELD_DEPOSIT)) {
            this.addCondition(SEARCH_FIELD_DEPOSIT, SearchOperator.eq, params.get(SEARCH_FIELD_DEPOSIT));
        }
        if (params.containsKey(SEARCH_FIELD_DEPOSIT_GREAT_THAN)) {
            this.addCondition(SEARCH_FIELD_DEPOSIT, SearchOperator.gt, params.get(SEARCH_FIELD_DEPOSIT_GREAT_THAN));
        }
        if (params.containsKey(SEARCH_FIELD_DEPOSIT_LESS_THAN)) {
            this.addCondition(SEARCH_FIELD_DEPOSIT, SearchOperator.lt, params.get(SEARCH_FIELD_DEPOSIT_LESS_THAN));
        }
    }
}
