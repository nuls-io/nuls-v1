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
package io.nuls.db.service;


import io.nuls.db.model.Entry;
import io.nuls.db.model.ModelWrapper;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Result;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/4
 */
public interface DBService {

    Result createArea(String areaName);
    Result createArea(String areaName, Long cacheSize);
    Result createArea(String areaName, Comparator<byte[]> comparator);
    Result createArea(String areaName, Long cacheSize, Comparator<byte[]> comparator);

    String[] listArea();

    Result put(String area, byte[] key, byte[] value);

    Result put(String area, String key, String value);

    Result put(String area, byte[] key, String value);

    <T> Result putModel(String area, String key, ModelWrapper<T> value);

    <T> Result putModel(String area, byte[] key, ModelWrapper<T> value);

    Result delete(String area, String key);

    Result delete(String area, byte[] key);

    byte[] get(String area, String key);

    byte[] get(String area, byte[] key);

    <T> ModelWrapper<T> getModel(String area, String key);

    <T> ModelWrapper<T> getModel(String area, byte[] key);

    Set<String> keySet(String area);

    List<String> keyList(String area);

    Set<Entry<String, byte[]>> entrySet(String area);

    List<Entry<String, byte[]>> entryList(String area);

}
