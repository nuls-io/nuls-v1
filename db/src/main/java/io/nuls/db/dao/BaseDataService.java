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
package io.nuls.db.dao;

import java.util.List;
import java.util.Map;

/**
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BaseDataService<K, T> {

    /**
     * 单个数据存储
     *
     * @param t 持久化数据
     * @return
     */
    int save(T t);

    /**
     * 批量存储
     *
     * @param list 持久化数据集合
     * @return
     */
    int save(List<T> list);

    /**
     * 修改数据
     *
     * @param t 修改后持久化数据
     * @return
     */
    int update(T t);

    int update(List<T> list);

    /**
     * 通过键值获取数据
     *
     * @param k 数据的键值
     * @return
     */
    T get(K k);

    /**
     * 删除持久化数据
     *
     * @param k 数据的键值
     * @return
     */
    int delete(K k);

    /**
     * @return all
     */
    List<T> getList();

    /**
     * @param params
     * @return
     */
    List<T> getList(Map<String, Object> params);

    /**
     * @return
     */
    Long getCount();

    List<T> getPageList(Map<String, Object> params, int pageSize, int pageNumber, String orderBy);

}
