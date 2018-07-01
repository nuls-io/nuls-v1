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

import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.model.ModelWrapper;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.Result;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.core.tools.str.StringUtils.bytes;
import static io.nuls.db.constant.DBConstant.BASE_AREA_NAME;

public class LevelDBManager {

    private static int max;

    private static final ConcurrentHashMap<String, DB> AREAS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Comparator<byte[]>> AREAS_COMPARATOR = new ConcurrentHashMap<>();

    private static final Map<Class, RuntimeSchema> SCHEMA_MAP = new ConcurrentHashMap<>();

    private static final String BASE_DB_NAME = "leveldb";

    private static volatile boolean isInit = false;

    private static ReentrantLock lock = new ReentrantLock();

    private static String dataPath;

    public static int getMax() {
        return max;
    }

    public static String getBaseAreaName() {
        return BASE_AREA_NAME;
    }

    public static void init() throws Exception {
        synchronized(LevelDBManager.class) {
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

    public static File loadDataPath() throws Exception {
        Properties properties = ConfigLoader.loadProperties("db_config.properties");
        String path = properties.getProperty("leveldb.datapath", "./data");
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
        lock.lock();
        try {
            // prevent too many areas
            if (AREAS.size() > (max - 1)) {
                return Result.getFailed(DBErrorCode.DB_AREA_CREATE_EXCEED_LIMIT);
            }
            if (StringUtils.isBlank(areaName)) {
                return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
            }
            if (AREAS.containsKey(areaName)) {
                return Result.getFailed(DBErrorCode.DB_AREA_EXIST);
            }
            if (StringUtils.isBlank(dataPath) || !checkPathLegal(areaName)) {
                return Result.getFailed(DBErrorCode.DB_AREA_CREATE_PATH_ERROR);
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
                result = Result.getFailed(DBErrorCode.DB_AREA_CREATE_ERROR);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public static DB getArea(String areaName) {
        return AREAS.get(areaName);
    }

    public static Result destroyArea(String areaName) {
        if (!baseCheckArea(areaName)) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (StringUtils.isBlank(dataPath) || !checkPathLegal(areaName)) {
            return Result.getFailed(DBErrorCode.DB_AREA_CREATE_PATH_ERROR);
        }
        Result result;
        try {
            DB db = AREAS.remove(areaName);
            db.close();
            File dir = new File(dataPath + File.separator + areaName);
            if (!dir.exists()) {
                return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
            }
            String filePath = dataPath + File.separator + areaName + File.separator + BASE_DB_NAME;
            destroyDB(filePath);
            AREAS_COMPARATOR.remove(areaName);
            delete(BASE_AREA_NAME, bytes(areaName + "-comparator"));
            delete(BASE_AREA_NAME, bytes(areaName + "-cacheSize"));
            result = Result.getSuccess();
        } catch (Exception e) {
            Log.error("error destroy area: " + areaName, e);
            result = Result.getFailed(DBErrorCode.DB_AREA_DESTROY_ERROR);
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
                AREAS_COMPARATOR.remove(entry.getKey());
                entry.getValue().close();
            } catch (Exception e) {
                Log.warn("close leveldb error", e);
            }
        }
    }

    /**
     * close a area
     * 关闭指定数据区域
     */
    public static void closeArea(String area) {
        try {
            AREAS_COMPARATOR.remove(area);
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
        File checkFile = new File(dbPath + File.separator + "CURRENT");
        if (!checkFile.exists()) {
            return null;
        }
        Options options = new Options().createIfMissing(false);

        /*
         * Area的自定义比较器，启动数据库时获取并装载它
         * Area的自定义cacheSize，启动数据库时获取并装载它，否则，启动已存在的Area时会丢失之前的cacheSize设置。
         * Area of custom comparator, you start the database access and loaded it
         * the custom cacheSize of the Area will be retrieved and loaded on the database is started, otherwise, the previous cacheSize setting will be lost when the existing Area is started.
         */
        String areaName = getAreaNameFromDbPath(dbPath);
        Comparator comparator = getModel(BASE_AREA_NAME, bytes(areaName + "-comparator"), Comparator.class);
        if (comparator != null) {
            AREAS_COMPARATOR.put(areaName, comparator);
        }
        Long cacheSize = getModel(BASE_AREA_NAME, bytes(areaName + "-cacheSize"), Long.class);
        if (cacheSize != null) {
            options.cacheSize(cacheSize);
        }
        File file = new File(dbPath);
        DBFactory factory = Iq80DBFactory.factory;
        return factory.open(file, options);
    }

    /**
     * 装载数据库
     * 如果Area自定义了比较器，保存area的自定义比较器，下次启动数据库时获取并装载它
     * 如果Area自定义了cacheSize，保存area的自定义cacheSize，下次启动数据库时获取并装载它，否则，启动已存在的Area时会丢失之前的cacheSize设置。
     * load database
     * If the area custom comparator, save area define the comparator, the next time you start the database access and loaded it
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
            putModel(BASE_AREA_NAME, bytes(areaName + "-cacheSize"), cacheSize);
            options.cacheSize(cacheSize);
        }
        if (comparator != null) {
            putModel(BASE_AREA_NAME, bytes(areaName + "-comparator"), comparator);
            AREAS_COMPARATOR.put(areaName, comparator);
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
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
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
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @param value
     * @return
     */
    @Deprecated
    public static Result put(String area, String key, String value) {
        if (!baseCheckArea(area)) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
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
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @param value
     * @return
     */
    @Deprecated
    public static Result put(String area, byte[] key, String value) {
        if (!baseCheckArea(area)) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
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
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> Result putModel(String area, String key, T value) {
        return putModel(area, bytes(key), value);
    }

    public static <T> Result putModel(String area, byte[] key, T value) {
        if (!baseCheckArea(area)) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
        }
        if (key == null || value == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            byte[] bytes = getModelSerialize(value);
            return put(area, key, bytes);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    public static <T> byte[] getModelSerialize(T value) {
        if (SCHEMA_MAP.get(ModelWrapper.class) == null) {
            RuntimeSchema schema = RuntimeSchema.createFrom(ModelWrapper.class);
            SCHEMA_MAP.put(ModelWrapper.class, schema);
        }
        RuntimeSchema schema = SCHEMA_MAP.get(ModelWrapper.class);
        ModelWrapper modelWrapper = new ModelWrapper(value);
        byte[] bytes = ProtostuffIOUtil.toByteArray(modelWrapper, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        return bytes;
    }

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @return
     */
    @Deprecated
    public static Result delete(String area, String key) {
        if (!baseCheckArea(area)) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
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
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    public static Result delete(String area, byte[] key) {
        if (!baseCheckArea(area)) {
            return Result.getFailed(DBErrorCode.DB_AREA_NOT_EXIST);
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
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @return
     */
    @Deprecated
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

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @return
     */
    @Deprecated
    public static Object getModel(String area, String key) {
        return getModel(area, bytes(key));
    }

    public static Object getModel(String area, byte[] key) {
        return getModel(area, key, null);
    }

    /**
     * 弃用的方法/Deprecated method
     * @param area
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    @Deprecated
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
            e.printStackTrace();
            return null;
        }
    }

    public static Set<byte[]> keySet(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        Set<byte[]> keySet = null;
        try {
            DB db = AREAS.get(area);
            keySet = new HashSet<>();
            iterator = db.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                keySet.add(iterator.peekNext().getKey());
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

    public static List<byte[]> keyList(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<byte[]> keyList = null;
        try {
            DB db = AREAS.get(area);
            keyList = new ArrayList<>();
            iterator = db.iterator();
            String key;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                keyList.add(iterator.peekNext().getKey());
            }
            Comparator<byte[]> comparator = AREAS_COMPARATOR.get(area);
            if(comparator != null) {
                keyList.sort(comparator);
            }
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

    public static Set<Entry<byte[], byte[]>> entrySet(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        Set<Entry<byte[], byte[]>> entrySet = null;
        try {
            DB db = AREAS.get(area);
            entrySet = new HashSet<>();
            iterator = db.iterator();
            byte[] key, bytes;
            Map.Entry<byte[], byte[]> entry;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                entry = iterator.peekNext();
                key = entry.getKey();
                bytes = entry.getValue();
                entrySet.add(new Entry<byte[], byte[]>(key, bytes));
            }
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

    public static List<Entry<byte[], byte[]>> entryList(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<Entry<byte[], byte[]>> entryList = null;
        try {
            DB db = AREAS.get(area);
            entryList = new ArrayList<>();
            iterator = db.iterator();
            byte[] key, bytes;
            Map.Entry<byte[], byte[]> entry;
            Comparator<byte[]> comparator = AREAS_COMPARATOR.get(area);
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                entry = iterator.peekNext();
                key = entry.getKey();
                bytes = entry.getValue();
                entryList.add(new Entry<byte[], byte[]>(key, bytes, comparator));
            }
            // 如果自定义了比较器，则执行排序
            if(comparator != null) {
                entryList.sort(new Comparator<Entry<byte[], byte[]>>() {
                    @Override
                    public int compare(Entry<byte[], byte[]> o1, Entry<byte[], byte[]> o2) {
                        return o1.compareTo(o2.getKey());
                    }
                });
            }
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

    public static <T> List<Entry<byte[], T>> entryList(String area, Class<T> clazz) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<Entry<byte[], T>> entryList = null;
        try {
            DB db = AREAS.get(area);
            entryList = new ArrayList<>();
            iterator = db.iterator();
            byte[] key, bytes;
            Map.Entry<byte[], byte[]> entry;
            Comparator<byte[]> comparator = AREAS_COMPARATOR.get(area);
            T t = null;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                t = null;
                entry = iterator.peekNext();
                key = entry.getKey();
                t = getModel(area, entry.getKey(), clazz);
                entryList.add(new Entry<byte[], T>(key, t, comparator));
            }
            // 如果自定义了比较器，则执行排序
            if(comparator != null) {
                entryList.sort(new Comparator<Entry<byte[], T>>() {
                    @Override
                    public int compare(Entry<byte[], T> o1, Entry<byte[], T> o2) {
                        return o1.compareTo(o2.getKey());
                    }
                });
            }
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
        try {
            Comparator<byte[]> comparator = AREAS_COMPARATOR.get(area);
            if(comparator == null) {
                return valuesInner(area, clazz);
            } else {
                List<Entry<byte[], T>> entryList = entryList(area, clazz);
                List<T> resultList = new ArrayList<>();
                if(entryList != null) {
                    entryList.stream().forEach(entry -> resultList.add(entry.getValue()));
                }
                return resultList;
            }
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    private static <T> List<T> valuesInner(String area, Class<T> clazz) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<T> list = null;
        try {
            DB db = AREAS.get(area);
            list = new ArrayList<>();
            iterator = db.iterator();
            byte[] key, bytes;
            Map.Entry<byte[], byte[]> entry;
            T t = null;
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                t = null;
                entry = iterator.peekNext();
                t = getModel(area, entry.getKey(), clazz);
                list.add(t);
            }
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

    public static List<byte[]> valueListInner(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        DBIterator iterator = null;
        List<byte[]> list = null;
        try {
            DB db = AREAS.get(area);
            list = new ArrayList<>();
            iterator = db.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                list.add(iterator.peekNext().getValue());
            }
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

    public static List<byte[]> valueList(String area) {
        if (!baseCheckArea(area)) {
            return null;
        }
        try {
            Comparator<byte[]> comparator = AREAS_COMPARATOR.get(area);
            if(comparator == null) {
                return valueListInner(area);
            } else {
                List<Entry<byte[], byte[]>> entryList = entryList(area);
                List<byte[]> resultList = new ArrayList<>();
                if(entryList != null) {
                    entryList.stream().forEach(entry -> resultList.add(entry.getValue()));
                }
                return resultList;
            }
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static Result clearArea(String area) {
        if (!baseCheckArea(area)) {
            return Result.getFailed();
        }
        try {
            return destroyArea(area);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }

        /*DBIterator iterator = null;
        try {
            DB db = AREAS.get(area);
            iterator = db.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
               db.delete(iterator.peekNext().getKey());
            }
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    //skip it
                }
            }
        }*/
    }
}
