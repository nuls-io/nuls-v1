/**
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.manager;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.util.DBUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * rocksdb数据库连接管理、数据存储、查询、删除操作.
 * Rocksdb database connection management, data storage, query, delete operation
 *
 * @author qinyf
 * @date 2018/10/10
 */
public class RocksDBManager {

    static {
        RocksDB.loadLibrary();
    }

    /**
     * 数据库已打开的连接缓存.
     */
    private static final ConcurrentHashMap<String, RocksDB> AREAS = new ConcurrentHashMap<>();

    /**
     * 数据表基础文件夹名.
     */
    private static final String BASE_DB_NAME = "rocksdb";

    /**
     * 数据库是否已经初始化.
     */
    private static volatile boolean isInit = false;

    /**
     * 数据操作同步锁.
     */
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * 数据库根目录绝对路径.
     */
    private static String dataPath;

    /**
     * 根据传入的数据库路径将已存在的数据库连接打开，并缓存DB连接.
     * 如果有数据表连接被关闭需要重新打开连接也可以，执行初始化连接
     *
     * @param path 数据库地址
     * @throws Exception 数据库打开连接异常
     */
    public static void init(final String path) throws Exception {
        synchronized (RocksDBManager.class) {
            //if (isInit) {
            isInit = true;
            File dir = DBUtils.loadDataPath(path);
            dataPath = dir.getPath();
            Log.info("RocksDBManager dataPath is " + dataPath);
            File[] AREAFiles = dir.listFiles();
            RocksDB db;
            String dbPath = null;
            for (File AREAFile : AREAFiles) {
                //缓存中已存在的数据库连接不再重复打开
                if (!AREAFile.isDirectory() || AREAS.get(AREAFile.getName()) != null) {
                    continue;
                }
                try {
                    dbPath = AREAFile.getPath() + File.separator + BASE_DB_NAME;
                    db = initOpenDB(dbPath);
                    if (db != null) {
                        AREAS.put(AREAFile.getName(), db);
                    }
                } catch (Exception e) {
                    Log.warn("load AREA failed, AREAName: " + AREAFile.getName() + ", dbPath: " + dbPath, e);
                    throw e;
                }
            }
            //}
        }

    }

    /**
     * @param dbPath 数据库地址
     * @return RocksDB 数据库连接对象
     * @throws RocksDBException 数据库连接异常
     */
    private static RocksDB initOpenDB(final String dbPath) throws RocksDBException {
        File checkFile = new File(dbPath + File.separator + "CURRENT");
        if (!checkFile.exists()) {
            return null;
        }

        Options options = getCommonOptions(false);
        return RocksDB.open(options, dbPath);
    }

    /**
     * 装载数据库.
     * load database
     *
     * @param dbPath          数据库地址
     * @param createIfMissing 数据库不存在时是否默认创建
     * @return RocksDB
     * @throws RocksDBException 数据库连接异常
     */
    private static RocksDB openDB(final String dbPath, final boolean createIfMissing) throws RocksDBException {
        Options options = getCommonOptions(createIfMissing);
        return RocksDB.open(options, dbPath);
    }

    /**
     * 根据名称创建对应的数据库.
     * Create database based by name
     *
     * @param AREAName 数据库表名称
     * @return Result 创建结果
     */
    public static boolean createTable(final String AREAName) throws Exception {
        lock.lock();
        try {
            if (StringUtils.isBlank(AREAName)) {
                throw new NulsException(KernelErrorCode.NULL_PARAMETER);
            }
            if (AREAS.containsKey(AREAName)) {
                throw new NulsException(DBErrorCode.DB_AREA_EXIST);
            }
            if (StringUtils.isBlank(dataPath) || !DBUtils.checkPathLegal(AREAName)) {
                throw new NulsException(DBErrorCode.DB_AREA_CREATE_PATH_ERROR);
            }
            try {
                File dir = new File(dataPath + File.separator + AREAName);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                String filePath = dataPath + File.separator + AREAName + File.separator + BASE_DB_NAME;
                RocksDB db = openDB(filePath, true);
                AREAS.put(AREAName, db);
            } catch (Exception e) {
                Log.error("error create AREA: " + AREAName, e);
                throw new NulsException(DBErrorCode.DB_AREA_CREATE_ERROR);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据名称获得对应的数据库对象.
     * Get database objects by name
     *
     * @param AREAName 数据库表名称
     * @return RocksDB
     */
    public static RocksDB getTable(final String AREAName) {
        return AREAS.get(AREAName);
    }

    /**
     * 根据名称删除对应的数据库.
     * Delete database by name
     *
     * @param AREAName 数据库表名称
     * @return Result
     */
    public static boolean destroyTable(final String AREAName) throws NulsException {
        if (!baseCheckTable(AREAName)) {
            throw new NulsException(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (StringUtils.isBlank(dataPath) || !DBUtils.checkPathLegal(AREAName)) {
            throw new NulsException(DBErrorCode.DB_AREA_CREATE_PATH_ERROR);
        }
        try {
            RocksDB db = AREAS.remove(AREAName);
            db.close();
            File dir = new File(dataPath + File.separator + AREAName);
            if (!dir.exists()) {
                throw new NulsException(DBErrorCode.DB_AREA_NOT_EXIST);
            }
            String filePath = dataPath + File.separator + AREAName + File.separator + BASE_DB_NAME;
            destroyDB(filePath);
        } catch (Exception e) {
            Log.error("error destroy AREA: " + AREAName, e);
            throw new NulsException(DBErrorCode.DB_AREA_DESTROY_ERROR);
        }
        return true;
    }

    /**
     * 删除数据表.
     *
     * @param dbPath 数据库名称
     * @throws RocksDBException 数据库连接异常
     */
    private static void destroyDB(final String dbPath) throws RocksDBException {
        Options options = new Options();
        RocksDB.destroyDB(dbPath, options);
    }

    /**
     * 关闭所有数据库连接.
     * close all AREA
     */
    public static void close() {
        Set<Map.Entry<String, RocksDB>> entries = AREAS.entrySet();
        for (Map.Entry<String, RocksDB> entry : entries) {
            try {
                AREAS.remove(entry.getKey());
                entry.getValue().close();
            } catch (Exception e) {
                Log.warn("close rocksdb error", e);
            }
        }
    }

    /**
     * 关闭指定数据库连接.
     * close a AREA
     *
     * @param AREAName 数据库表名称
     */
    public static void closeTable(final String AREAName) {
        try {
            RocksDB db = AREAS.remove(AREAName);
            db.close();
        } catch (Exception e) {
            Log.warn("close rocksdb AREAName error:" + AREAName, e);
        }
    }

    /**
     * 数据库基本校验.
     * Basic database check
     *
     * @param AREAName 数据库表名称
     * @return boolean 校验是否成功
     */
    private static boolean baseCheckTable(final String AREAName) {
        if (StringUtils.isBlank(AREAName) || !AREAS.containsKey(AREAName)) {
            return false;
        }
        return true;
    }

    /**
     * 查询所有的数据库名称.
     * query all AREA names
     *
     * @return 所有数据表名称
     */
    public static String[] listTable() {
        int i = 0;
        Enumeration<String> keys = AREAS.keys();
        String[] AREAs = new String[AREAS.size()];
        int length = AREAs.length;
        while (keys.hasMoreElements()) {
            AREAs[i++] = keys.nextElement();
            if (i == length) {
                break;
            }
        }
        return AREAs;
    }

    /**
     * 新增或者修改数据.
     * Add or modify data to specified AREA
     *
     * @param AREA 表名
     * @param key   数据键
     * @param value 数据值
     * @return 保存是否成功
     */
    public static boolean put(final String AREA, final byte[] key, final byte[] value) throws Exception {
        if (!baseCheckTable(AREA)) {
            throw new NulsException(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (key == null || value == null) {
            throw new NulsException(DBErrorCode.NULL_PARAMETER);
        }
        try {
            RocksDB db = AREAS.get(AREA);
            db.put(key, value);
            return true;
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 删除数据.
     * delete data from specified AREA
     *
     * @param AREA 数据库表名称
     * @param key   删除标识
     * @return 删除是否成功
     */
    public static boolean delete(final String AREA, final byte[] key) throws Exception {
        if (!baseCheckTable(AREA)) {
            throw new NulsException(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (key == null) {
            throw new NulsException(DBErrorCode.NULL_PARAMETER);
        }
        try {
            RocksDB db = AREAS.get(AREA);
            db.delete(key);
            return true;
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 批量保存数据.
     * batch save data
     *
     * @param AREA 数据库表名称
     * @param kvs   保存数据的键值对
     * @return 批量保存是否成功
     */
    public static boolean batchPut(final String AREA, final Map<byte[], byte[]> kvs) throws Exception {
        if (!baseCheckTable(AREA)) {
            throw new NulsException(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (kvs == null || kvs.size() == 0) {
            throw new NulsException(DBErrorCode.NULL_PARAMETER);
        }

        try (WriteBatch writeBatch = new WriteBatch()) {
            RocksDB db = AREAS.get(AREA);
            for (Map.Entry<byte[], byte[]> entry : kvs.entrySet()) {
                writeBatch.put(entry.getKey(), entry.getValue());
            }
            db.write(new WriteOptions(), writeBatch);
            return true;
        } catch (Exception ex) {
            Log.error(ex);
            throw new NulsException(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 批量删除数据.
     * batch delete data
     *
     * @param AREA 数据库表名称
     * @param keys  批量删除标识
     * @return 批量删除是否成功
     */
    public static boolean deleteKeys(final String AREA, final List<byte[]> keys) throws Exception {
        if (!baseCheckTable(AREA)) {
            throw new NulsException(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (keys == null || keys.size() == 0) {
            throw new NulsException(DBErrorCode.NULL_PARAMETER);
        }
        try (WriteBatch writeBatch = new WriteBatch()) {
            RocksDB db = AREAS.get(AREA);
            for (byte[] key : keys) {
                writeBatch.delete(key);
            }
            db.write(new WriteOptions(), writeBatch);
            return true;
        } catch (Exception ex) {
            Log.error(ex);
            throw new NulsException(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 根据key查询数据.
     * query data in a specified AREA by key
     *
     * @param AREA 数据库表名称
     * @param key   查询关键字
     * @return 查询结果
     */
    public static byte[] get(final String AREA, final byte[] key) {
        if (!baseCheckTable(AREA)) {
            return null;
        }
        if (key == null) {
            return null;
        }
        try {
            RocksDB db = AREAS.get(AREA);
            return db.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 批量查询指定keys的Map集合.
     * batch query the Map set of the specified keys.
     *
     * @param AREA 数据库表名称
     * @param keys  批量查询关键字
     * @return 批量查询结果键值对集合
     */
    public static Map<byte[], byte[]> multiGet(final String AREA, final List<byte[]> keys) {
        if (!baseCheckTable(AREA)) {
            return null;
        }
        if (keys == null || keys.size() == 0) {
            return null;
        }
        try {
            RocksDB db = AREAS.get(AREA);
            return db.multiGet(keys);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 批量查询指定keys的List集合
     * batch query the List set of the specified keys.
     *
     * @param AREA 数据库表名称
     * @param keys  批量查询关键字
     * @return 批量查询结果值字节数组集合
     */
    public static List<byte[]> multiGetValueList(final String AREA, final List<byte[]> keys) {
        if (!baseCheckTable(AREA)) {
            return null;
        }
        if (keys == null || keys.size() == 0) {
            return null;
        }
        try {
            RocksDB db = AREAS.get(AREA);
            Map<byte[], byte[]> map = db.multiGet(keys);
            if (map != null && map.size() > 0) {
                return new ArrayList<>(map.values());
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 查询指定表的key-List集合.
     * query the key-List collection of the specified AREA
     *
     * @param AREA 数据库表名称
     * @return 该表的所有键
     */
    public static List<byte[]> keyList(final String AREA) {
        if (!baseCheckTable(AREA)) {
            return null;
        }
        List<byte[]> list = new ArrayList<>();
        try {
            RocksDB db = AREAS.get(AREA);
            try (RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    list.add(iterator.key());
                }
            }
            return list;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 查询指定表的value-List集合.
     * query the value-List collection of the specified AREA
     *
     * @param AREA 数据库表名称
     * @return 该表的所有值
     */
    public static List<byte[]> valueList(final String AREA) {
        if (!baseCheckTable(AREA)) {
            return null;
        }
        List<byte[]> list = new ArrayList<>();
        try {
            RocksDB db = AREAS.get(AREA);
            try (RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    list.add(iterator.value());
                }
            }
            return list;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 查询指定表的entry-List集合.
     * query the entry-List collection of the specified AREA
     *
     * @param AREA 数据库表名称
     * @return 该表所有键值对集合
     */
    public static List<Entry<byte[], byte[]>> entryList(final String AREA) {
        if (!baseCheckTable(AREA)) {
            return null;
        }
        List<Entry<byte[], byte[]>> entryList = new ArrayList<>();
        try {
            RocksDB db = AREAS.get(AREA);
            try (RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    entryList.add(new Entry(iterator.key(), iterator.value()));
                }
            }
            return entryList;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获得公共的数据库连接属性.
     *
     * @param createIfMissing 是否默认表
     * @return 数据库连接属性
     */
    private static synchronized Options getCommonOptions(final boolean createIfMissing) {
        Options options = new Options();
        final Filter bloomFilter = new BloomFilter(10);
        final Statistics stats = new Statistics();
        //final RateLimiter rateLimiter = new RateLimiter(10000000, 10000, 10);

        options.setCreateIfMissing(createIfMissing).setAllowMmapReads(true).setCreateMissingColumnFamilies(true)
                .setStatistics(stats).setMaxWriteBufferNumber(3).setMaxBackgroundCompactions(10);

        final BlockBasedTableConfig AREAOptions = new BlockBasedTableConfig();
        AREAOptions.setBlockCacheSize(64 * SizeUnit.KB).setFilter(bloomFilter)
                .setCacheNumShardBits(6).setBlockSizeDeviation(5).setBlockRestartInterval(10)
                .setCacheIndexAndFilterBlocks(true).setHashIndexAllowCollision(false)
                .setBlockCacheCompressedSize(64 * SizeUnit.KB)
                .setBlockCacheCompressedNumShardBits(10);

        options.setTableFormatConfig(AREAOptions);
        return options;
    }

}
