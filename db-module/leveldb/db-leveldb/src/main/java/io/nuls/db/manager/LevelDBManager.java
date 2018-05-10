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
package io.nuls.db.manager;

import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.model.ModelWrapper;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.impl.BatchOperationImpl;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.Result;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class LevelDBManager {

    private static int max;

    private static final ConcurrentHashMap<String, DB> AREAS = new ConcurrentHashMap<>();

    private static final Map<Class, RuntimeSchema> SCHEMA_MAP = new ConcurrentHashMap<>();

    private static final String BASE_DB_NAME = "leveldb";
    private static final String BASE_AREA_NAME = "base";

    private static volatile boolean isInit = false;

    private static String dataPath;

    public static int getMax() {
        return max;
    }

    public static String getBaseAreaName() {
        return BASE_AREA_NAME;
    }

    public static synchronized void init() throws Exception {
        if (!isInit) {
            isInit = true;
            File dir = loadDataPath();
            dataPath = dir.getPath();
            Log.info("LevelDBManager dataPath is " + dataPath);

            initSchema();
            initBaseDB(dataPath);

            File[] areaFiles = dir.listFiles();
            DB db = null;
            String dbPath = null;
            for (File areaFile : areaFiles) {
                if (BASE_AREA_NAME.equals(areaFile.getName())) {
                    continue;
                }
                if (!areaFile.isDirectory()) {
                    continue;
                }
                try {
                    dbPath = areaFile.getPath() + File.separator + BASE_DB_NAME;
                    db = initOpenDB(dbPath);
                    if (db != null) {
                        AREAS.put(areaFile.getName(), db);
                    }
                } catch (Exception e) {
                    Log.warn("load area failed, areaName: " + areaFile.getName() + ", dbPath: " + dbPath, e);
                }

            }
        }
    }

    private static void initSchema() {
        RuntimeSchema schema = RuntimeSchema.createFrom(ModelWrapper.class);
        SCHEMA_MAP.put(ModelWrapper.class, schema);
    }


    /**
     * 优先初始化BASE_AREA
     * 存放基础数据，比如Area的自定义比较器，下次启动数据库时获取并装载它，否则，若Area自定义了比较器，下次重启数据库时，此Area会启动失败，失败异常：java.lang.IllegalArgumentException: Expected user comparator leveldb.BytewiseComparator to match existing database comparator
     * 比如Area的自定义cacheSize，下次启动数据库时获取并装载它，否则，下次启动已存在的Area时会丢失之前的cacheSize设置。
     * Prioritize BASE_AREA.
     * Based data storage, for example, Area of custom comparator, the next time you start the database access and loaded it, otherwise, if the Area custom the comparator, the next time you restart the database, this Area will start failure, failure exception: java.lang.IllegalArgumentException: Expected user comparator leveldb. BytewiseComparator to match existing database comparator
     * the custom cacheSize of the Area will be retrieved and loaded next time the database is started, otherwise, the previous cacheSize setting will be lost when the existing Area is started.
     *
     * @param dataPath
     */
    private static void initBaseDB(String dataPath) {
        if (AREAS.get(BASE_AREA_NAME) == null) {
            String baseAreaPath = dataPath + File.separator + BASE_AREA_NAME;
            File dir = new File(baseAreaPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String filePath = baseAreaPath + File.separator + BASE_DB_NAME;
            try {
                DB db = openDB(filePath, true, null, null);
                AREAS.put(BASE_AREA_NAME, db);
            } catch (IOException e) {
                Log.error(e);
            }
        }
    }

    private static File loadDataPath() throws Exception {
        Properties properties = ConfigLoader.loadProperties("db_config.properties");
        String path = properties.getProperty("leveldb.datapath", "./data/kv");
        String max_str = properties.getProperty("leveldb.area.max", "20");
        try {
            max = Integer.parseInt(max_str);
        } catch (Exception e) {
            //skip it
            max = 20;
        }
        File dir = null;
        String pathSeparator = System.getProperty("path.separator");
        String unixPathSeparator = ":";
        String rootPath;
        if (unixPathSeparator.equals(pathSeparator)) {
            rootPath = "/";
            if (path.startsWith(rootPath)) {
                dir = new File(path);
            } else {
                dir = new File(genAbsolutePath(path));
            }
        } else {
            rootPath = "^[c-zC-Z]:.*";
            if (path.matches(rootPath)) {
                dir = new File(path);
            } else {
                dir = new File(genAbsolutePath(path));
            }
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static String genAbsolutePath(String path) {
        String[] paths = path.split("/|\\\\");
        URL resource = ClassLoader.getSystemClassLoader().getResource(".");
        String classPath = resource.getPath();
        File file = new File(classPath);
        String resultPath = null;
        boolean isFileName = false;
        for (String p : paths) {
            if (StringUtils.isBlank(p)) {
                continue;
            }
            if (!isFileName) {
                if ("..".equals(p)) {
                    file = file.getParentFile();
                } else if (".".equals(p)) {
                    continue;
                } else {
                    isFileName = true;
                    resultPath = file.getPath() + File.separator + p;
                }
            } else {
                resultPath += File.separator + p;
            }
        }
        return resultPath;
    }

    public static Result createArea(String areaName) {
        return createArea(areaName, null, null);
    }

    public static Result createArea(String areaName, Long cacheSize) {
        return createArea(areaName, cacheSize, null);
    }

    public static Result createArea(String areaName, Comparator<byte[]> comparator) {
        return createArea(areaName, null, comparator);
    }

    public static Result createArea(String areaName, Long cacheSize, Comparator<byte[]> comparator) {
        // prevent too many areas
        if (AREAS.size() > (max - 1)) {
            return new Result(false, "KV_AREA_CREATE_ERROR");
        }
        if (StringUtils.isBlank(areaName)) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        if (AREAS.containsKey(areaName)) {
            return Result.getFailed(DBErrorCode.DB_AREA_EXIST);
        }
        if (StringUtils.isBlank(dataPath) || !checkPathLegal(areaName)) {
            return new Result(false, "KV_AREA_CREATE_ERROR");
        }
        Result result;
        try {
            File dir = new File(dataPath + File.separator + areaName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String filePath = dataPath + File.separator + areaName + File.separator + BASE_DB_NAME;
            DB db = openDB(filePath, true, cacheSize, comparator);
            AREAS.put(areaName, db);
            result = Result.getSuccess();
        } catch (Exception e) {
            Log.error("error create area: " + areaName, e);
            result = new Result(false, "KV_AREA_CREATE_ERROR");
        }
        return result;
    }

    public static DB getArea(String areaName) {
        return AREAS.get(areaName);
    }

    public static Result destroyArea(String areaName) {
        if (!baseCheckArea(areaName)) {
            return new Result(false, "KV_AREA_NOT_EXISTS");
        }
        if (StringUtils.isBlank(dataPath) || !checkPathLegal(areaName)) {
            return new Result(false, "KV_AREA_PATH_ERROR");
        }
        Result result;
        try {
            File dir = new File(dataPath + File.separator + areaName);
            if (!dir.exists()) {
                return new Result(false, "KV_AREA_NOT_EXISTS");
            }
            String filePath = dataPath + File.separator + areaName + File.separator + BASE_DB_NAME;
            destroyDB(filePath);
            AREAS.remove(areaName);
            delete(BASE_AREA_NAME, areaName);
            result = Result.getSuccess();
        } catch (Exception e) {
            Log.error("error destroy area: " + areaName, e);
            result = new Result(false, "KV_AREA_DESTROY_ERROR");
        }
        return result;
    }

    private static void destroyDB(String dbPath) throws IOException {
        File file = new File(dbPath);
        Options options = new Options();
        DBFactory factory = Iq80DBFactory.factory;
        factory.destroy(file, options);
    }

    /**
     * close all area
     * 关闭所有数据区域
     */
    public static void close() {
        Set<Map.Entry<String, DB>> entries = AREAS.entrySet();
        for (Map.Entry<String, DB> entry : entries) {
            try {
                AREAS.remove(entry.getKey());
                entry.getValue().close();
            } catch (Exception e) {
                Log.warn("close leveldb error", e);
            }
        }
    }

    /**
     * close a area
     * 关闭所有数据区域
     */
    public static void closeArea(String area) {
        try {
            DB db = AREAS.remove(area);
            db.close();
        } catch (IOException e) {
            Log.warn("close leveldb area error:" + area, e);
        }
    }


    /**
     * @param dbPath
     * @return
     * @throws IOException
     */
    private static DB initOpenDB(String dbPath) throws IOException {
        File file = new File(dbPath);
        if (!file.exists()) {
            return null;
        }
        Options options = new Options().createIfMissing(false);

        /*
         * Area的自定义比较器，启动数据库时获取并装载它，否则，若Area自定义了比较器，重启数据库时，此Area会启动失败，失败异常：java.lang.IllegalArgumentException: Expected user comparator leveldb.BytewiseComparator to match existing database comparator
         * Area的自定义cacheSize，启动数据库时获取并装载它，否则，启动已存在的Area时会丢失之前的cacheSize设置。
         * Area of custom comparator, you start the database access and loaded it, otherwise, if the Area custom the comparator, this time you restart the database, this Area will start failure, failure exception: java.lang.IllegalArgumentException: Expected user comparator leveldb. BytewiseComparator to match existing database comparator
         * the custom cacheSize of the Area will be retrieved and loaded on the database is started, otherwise, the previous cacheSize setting will be lost when the existing Area is started.
         */
        String areaName = getAreaNameFromDbPath(dbPath);
        DBComparator dbComparator = getModel(BASE_AREA_NAME, areaName + "-comparator", DBComparator.class);
        if (dbComparator != null) {
            options.comparator(dbComparator);
        }
        Long cacheSize = getModel(BASE_AREA_NAME, areaName + "-cacheSize", Long.class);
        if (cacheSize != null) {
            options.cacheSize(cacheSize);
        }
        DBFactory factory = Iq80DBFactory.factory;
        return factory.open(file, options);
    }

    /**
     * 装载数据库
     * 如果Area自定义了比较器，保存area的自定义比较器，下次启动数据库时获取并装载它，否则，下次重启数据库时，此Area会启动失败，失败异常：java.lang.IllegalArgumentException: Expected user comparator leveldb.BytewiseComparator to match existing database comparator
     * 如果Area自定义了cacheSize，保存area的自定义cacheSize，下次启动数据库时获取并装载它，否则，启动已存在的Area时会丢失之前的cacheSize设置。
     * load database
     * If the area custom comparator, save area define the comparator, the next time you start the database access and loaded it, otherwise, the next time you restart the database, this area will start failure, failure exception: java.lang.IllegalArgumentException: Expected user comparator leveldb.BytewiseComparator to match existing database comparator
     * If the area custom cacheSize, save the area's custom cacheSize, get and load it the next time you start the database, or you'll lose the cacheSize setting before starting the existing area.
     *
     * @param dbPath
     * @param createIfMissing
     * @param cacheSize
     * @param comparator
     * @return
     * @throws IOException
     */
    private static DB openDB(String dbPath, boolean createIfMissing, Long cacheSize, Comparator<byte[]> comparator) throws IOException {
        File file = new File(dbPath);
        String areaName = getAreaNameFromDbPath(dbPath);
        Options options = new Options().createIfMissing(createIfMissing);
        if (cacheSize != null) {
            putModel(BASE_AREA_NAME, areaName + "-cacheSize", cacheSize);
            options.cacheSize(cacheSize);
        }
        if (comparator != null) {
            DBComparator dbComparator = new DBComparator() {
                public int compare(byte[] key1, byte[] key2) {
                    return comparator.compare(key1, key2);
                }

                public String name() {
                    return areaName + "-key-comparator";
                }

                public byte[] findShortestSeparator(byte[] start, byte[] limit) {
                    return start;
                }

                public byte[] findShortSuccessor(byte[] key) {
                    return key;
                }
            };
            putModel(BASE_AREA_NAME, areaName + "-comparator", dbComparator);
            options.comparator(dbComparator);
        }
        DBFactory factory = Iq80DBFactory.factory;
        return factory.open(file, options);
    }

    private static String getAreaNameFromDbPath(String dbPath) {
        int end = dbPath.lastIndexOf(File.separator);
        int start = dbPath.lastIndexOf(File.separator, end - 1) + 1;
        return dbPath.substring(start, end);
    }

    private static boolean checkPathLegal(String areaName) {
        if (StringUtils.isBlank(areaName)) {
            return false;
        }
        String regex = "^[a-zA-Z0-9_\\-]+$";
        return areaName.matches(regex);
    }

    private static boolean baseCheckArea(String areaName) {
        if (StringUtils.isBlank(areaName) || !AREAS.containsKey(areaName)) {
            return false;
        }
        return true;
    }

    @Deprecated
    private static byte[] bytes_(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return str.getBytes(NulsConfig.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return null;
        }
    }

    @Deprecated
    private static String asString_(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, NulsConfig.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return null;
        }
    }

    public static String[] listArea() {
        int i = 0;
        Enumeration<String> keys = AREAS.keys();
        String[] areas = new String[AREAS.size()];
        int length = areas.length;
        while (keys.hasMoreElements()) {
            areas[i++] = keys.nextElement();
            // thread safe, prevent java.lang.ArrayIndexOutOfBoundsException
            if (i == length) {
                break;
            }
        }
        return areas;
    }

    public static Result put(String area, byte[] key, byte[] value) {
        if (!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if (key == null || value == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.put(key, value);
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result put(String area, String key, String value) {
        if (!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.put(bytes(key), bytes(value));
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result put(String area, byte[] key, String value) {
        if (!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if (key == null || StringUtils.isBlank(value)) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.put(key, bytes(value));
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    public static <T> Result putModel(String area, String key, T value) {
        return putModel(area, bytes(key), value);
    }

    public static <T> Result putModel(String area, byte[] key, T value) {
        if (!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if (key == null || value == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            if (SCHEMA_MAP.get(ModelWrapper.class) == null) {
                RuntimeSchema schema = RuntimeSchema.createFrom(ModelWrapper.class);
                SCHEMA_MAP.put(ModelWrapper.class, schema);
            }
            RuntimeSchema schema = SCHEMA_MAP.get(ModelWrapper.class);
            ModelWrapper modelWrapper = new ModelWrapper(value);
            byte[] bytes = ProtostuffIOUtil.toByteArray(modelWrapper, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            DB db = AREAS.get(area);
            db.put(key, bytes);
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result delete(String area, String key) {
        if (!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if (StringUtils.isBlank(key)) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.delete(bytes(key));
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result delete(String area, byte[] key) {
        if (!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if (key == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.delete(key);
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    public static byte[] get(String area, String key) {
        if (!baseCheckArea(area)) {
            return null;
        }
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            DB db = AREAS.get(area);
            return db.get(bytes(key));
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] get(String area, byte[] key) {
        if (!baseCheckArea(area)) {
            return null;
        }
        if (key == null) {
            return null;
        }
        try {
            DB db = AREAS.get(area);
            return db.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getModel(String area, String key) {
        return getModel(area, bytes(key));
    }

    public static Object getModel(String area, byte[] key) {
        return getModel(area, key, null);
    }

    public static <T> T getModel(String area, String key, Class<T> clazz) {
        return getModel(area, bytes(key), clazz);
    }

    public static <T> T getModel(String area, byte[] key, Class<T> clazz) {
        if (!baseCheckArea(area)) {
            return null;
        }
        if (key == null) {
            return null;
        }
        try {
            DB db = AREAS.get(area);
            byte[] bytes = db.get(key);
            if (bytes == null) {
                return null;
            }
            RuntimeSchema schema = SCHEMA_MAP.get(ModelWrapper.class);
            ModelWrapper model = new ModelWrapper();
            ProtostuffIOUtil.mergeFrom(bytes, model, schema);
            if (clazz != null && model.getT() != null) {
                return clazz.cast(model.getT());
            }
            return (T) model.getT();
        } catch (Exception e) {
            return null;
        }
    }

    public static Set<String> keySet(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        Set<String> keySet = null;
        try {
            DB db = AREAS.get(area);
            keySet = new HashSet<>();
            iterator = db.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                keySet.add(asString(iterator.peekNext().getKey()));
            }
            return keySet;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }
    }

    public static List<String> keyList(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<String> keyList = null;
        try {
            DB db = AREAS.get(area);
            keyList = new ArrayList<>();
            iterator = db.iterator();
            Set<String> keySet = new HashSet<>();
            String key;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                key = asString(iterator.peekNext().getKey());
                if (keySet.add(key)) {
                    keyList.add(key);
                }
            }
            keySet.clear();
            keySet = null;
            return keyList;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }
    }

    public static Set<Entry<String, byte[]>> entrySet(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        Set<Entry<String, byte[]>> entrySet = null;
        try {
            DB db = AREAS.get(area);
            entrySet = new HashSet<>();
            Set<String> keySet = new HashSet<>();
            iterator = db.iterator();
            String key;
            byte[] bytes;
            Map.Entry<byte[], byte[]> entry;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                entry = iterator.peekNext();
                key = asString(entry.getKey());
                if (keySet.add(key)) {
                    bytes = entry.getValue();
                    entrySet.add(new Entry<String, byte[]>(key, bytes));
                }
            }
            keySet.clear();
            keySet = null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }
        return entrySet;
    }

    public static List<Entry<String, byte[]>> entryList(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<Entry<String, byte[]>> entryList = null;
        try {
            DB db = AREAS.get(area);
            entryList = new ArrayList<>();
            iterator = db.iterator();
            Set<String> keySet = new HashSet<>();
            String key;
            byte[] bytes;
            Map.Entry<byte[], byte[]> entry;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                entry = iterator.peekNext();
                key = asString(entry.getKey());
                if (keySet.add(key)) {
                    bytes = entry.getValue();
                    entryList.add(new Entry<String, byte[]>(key, bytes));
                }
            }
            keySet.clear();
            keySet = null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }
        return entryList;
    }

    public static <T> List<Entry<String, T>> entryList(String area, Class<T> clazz) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<Entry<String, T>> entryList = null;
        try {
            DB db = AREAS.get(area);
            entryList = new ArrayList<>();
            iterator = db.iterator();
            Set<String> keySet = new HashSet<>();
            String key;
            byte[] bytes;
            Map.Entry<byte[], byte[]> entry;
            T t = null;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                t = null;
                entry = iterator.peekNext();
                key = asString(entry.getKey());
                if (keySet.add(key)) {
                    t = getModel(area, entry.getKey(), clazz);
                    entryList.add(new Entry<String, T>(key, t));
                }
            }
            keySet.clear();
            keySet = null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }
        return entryList;
    }

    public static <T> List<T> values(String area, Class<T> clazz) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<T> list = null;
        try {
            DB db = AREAS.get(area);
            list = new ArrayList<>();
            iterator = db.iterator();
            Set<String> keySet = new HashSet<>();
            String key;
            byte[] bytes;
            Map.Entry<byte[], byte[]> entry;
            T t = null;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                t = null;
                entry = iterator.peekNext();
                key = asString(entry.getKey());
                if (keySet.add(key)) {
                    t = getModel(area, entry.getKey(), clazz);
                    list.add(t);
                }
            }
            keySet.clear();
            keySet = null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }
        return list;
    }
}
