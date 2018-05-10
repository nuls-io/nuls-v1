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

    /**
     * 创建一个数据区域
     * Create a data area
     *
     * @param areaName
     * @return
     */
    Result createArea(String areaName);

    /**
     * 弃用的方法
     * Deprecated method
     * @param areaName
     * @param cacheSize 无效字段，LevelDB未实现/cacheSize hasn't been implemented.
     * @return
     */
    @Deprecated
    Result createArea(String areaName, Long cacheSize);

    /**
     * 创建一个自定义key比较器的数据区域
     * Create a data area for the custom key comparator.
     *
     * @param areaName
     * @param comparator 自定义key比较器/Custom key comparator.
     * @return
     */
    Result createArea(String areaName, Comparator<byte[]> comparator);

    /**
     * 弃用的方法
     * Deprecated method
     * @param areaName
     * @param cacheSize 无效字段，LevelDB未实现/cacheSize hasn't been implemented by LevelDB in Java's version.
     * @param comparator
     * @return
     */
    @Deprecated
    Result createArea(String areaName, Long cacheSize, Comparator<byte[]> comparator);


    /**
     * 列出当前数据库中所有Area名称
     * Lists all Area names in the current database
     *
     * @return
     */
    String[] listArea();

    /**
     * 按字节存储key-value
     * Store key-value in bytes.
     *
     * @param area
     * @param key
     * @param value
     * @return
     */
    Result put(String area, byte[] key, byte[] value);

    /**
     * 存储对象
     * Store the object
     *
     * @param area
     * @param key
     * @param value 需要存储的对象/Objects that need to be stored.
     * @param <T>
     * @return
     */
    <T> Result putModel(String area, byte[] key, T value);

    /**
     * 根据key删除value
     * Delete value according to key.
     *
     * @param area
     * @param key
     * @return
     */
    Result delete(String area, byte[] key);

    /**
     * 根据key获取value
     * Get value from the key.
     *
     * @param area
     * @param key
     * @return
     */
    byte[] get(String area, byte[] key);

    /**
     * 根据key和对象class获取指定对象
     * 前提是这个key的存储方式是putModel，否则value为null
     * Gets the specified object from the key and object class.
     * The premise is that this key is stored in a putModel, otherwise value is null.
     *
     * @param area
     * @param key
     * @param clazz 指定对象的class/Specifies the class of the object.
     * @param <T>
     * @return
     */
    <T> T getModel(String area, byte[] key, Class<T> clazz);

    /**
     * 根据key获取Object对象
     * Get the Object of Object from the key.
     *
     * param area
     * @param key
     * @return
     */
    Object getModel(String area, byte[] key);

    /**
     * 获取数据区域的所有key的无序集合
     * Gets an unordered collection of all keys in the data area.
     *
     * @param area
     * @return
     */
    Set<byte[]> keySet(String area);

    /**
     * 获取数据区域的所有key的有序集合
     * Gets an ordered collection of all keys in the data area.
     *
     * @param area
     * @return
     */
    List<byte[]> keyList(String area);

    /**
     * 获取数据区域的所有key-value的无序集合
     * Gets an unordered collection of all key-value in the data area.
     *
     * @param area
     * @return
     */
    Set<Entry<byte[], byte[]>> entrySet(String area);

    /**
     * 获取数据区域的所有key-value的有序集合
     * Gets an ordered set of all key-values in the data area.
     *
     * @param area
     * @return
     */
    List<Entry<byte[], byte[]>> entryList(String area);

    /**
     * 获取数据区域的所有key-value的有序集合，并指定返回的value对象
     * 前提是这个数据区域的存储方式是putModel，否则value为null
     * Gets the ordered collection of all key-value in the data area and specifies the returned value object.
     * The premise is that the storage mode in this data area is the putModel, otherwise value is null.
     *
     * @param area
     * @param clazz 指定对象的class/Specifies the class of the object.
     * @param <T>
     * @return
     */
    <T> List<Entry<byte[], T>> entryList(String area, Class<T> clazz);

    /**
     * 获取数据区域的所有value的有序集合，并指定返回的value对象
     * 前提是这个数据区域的存储方式是putModel，否则value为null
     * Gets the ordered collection of all values in the data area and specifies the returned value object.
     * The premise is that the storage mode in this data area is the putModel, otherwise value is null.
     *
     * @param area
     * @param clazz 指定对象的class/Specifies the class of the object.
     * @param <T>
     * @return
     */
    <T> List<T> values(String area, Class<T> clazz);

    /**
     * 指定数据区域的批量增删改操作
     * Specifies the batch add, delete, update operations in the data area.
     *
     * @param area
     * @return
     */
    BatchOperation createWriteBatch(String area);

}
