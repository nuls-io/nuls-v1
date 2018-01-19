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

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class NodeSearchParams extends Searchable {
    public static final String SEARCH_FIELD_IP = "ip";
    public static final String SEARCH_FIELD_MAGIC_NUM = "magic_num";
    public static final String SEARCH_RANDOM = "ROWNUM()";
    public static final String SEARCH_FAIL_TIME = "last_fail_time";

    public NodeSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_IP)) {
            this.addCondition(SEARCH_FIELD_IP, SearchOperator.eq, params.get(SEARCH_FIELD_IP));
        }
        if (params.containsKey(SEARCH_FIELD_MAGIC_NUM)) {
            this.addCondition(SEARCH_FIELD_MAGIC_NUM, SearchOperator.eq, params.get(SEARCH_FIELD_MAGIC_NUM));
        }
        if(params.containsKey(SEARCH_RANDOM)) {
            this.addCondition(SEARCH_RANDOM, SearchOperator.in, params.get(SEARCH_RANDOM));
        }
        if(params.containsKey(SEARCH_FAIL_TIME)) {
            this.addCondition(SEARCH_FAIL_TIME, SearchOperator.lt, params.get(SEARCH_FAIL_TIME));
        }
    }
}
