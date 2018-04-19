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
package io.nuls.db.manager;

import com.alibaba.druid.filter.FilterManager;
import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.model.Result;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class LevelDBManager {

    public static final int MAX = 20;

    private static final ConcurrentHashMap<String, DB> AREAS = new ConcurrentHashMap<>();

    private static final String BASE_DB_NAME = "leveldb";

    private static volatile boolean isInit = false;

    private static String dataPath;

    public static synchronized void init() throws UnsupportedEncodingException {
        if(!isInit) {
            isInit = true;
            URL resource = ClassLoader.getSystemClassLoader().getResource(".");
            String classPath = resource.getPath();
            File file = new File(classPath);
            String parent = file.getParent();
            dataPath = parent + "/data/kv";
            File _dir = new File(dataPath);
            if(!_dir.exists())
                _dir.mkdirs();

            File dir = new File(dataPath);
            File[] areaFiles = dir.listFiles();
            DB db = null;
            for(File areaFile : areaFiles) {
                if(!areaFile.isDirectory())
                    continue;
                try {
                    db = openDB(areaFile.getPath() + File.separator + BASE_DB_NAME, false);
                    AREAS.put(areaFile.getName(), db);
                } catch (Exception e) {
                    Log.warn("load area failed, areaName: " + areaFile.getName(), e);
                }

            }


            String area = "pierre-test";
            String key = "testkey";
            createArea(area);
            String value = "testvalue_1";
            put(area, key, value);
        }
    }

    public static Result createArea(String areaName) {
        // prevent too many areas
        if(AREAS.size() > (MAX -1)) {
            return new Result(false, "KV_AREA_CREATE_ERROR");
        }
        if(StringUtils.isBlank(areaName)) {
            return Result.getFailed(ErrorCode.NULL_PARAMETER);
        }
        if (AREAS.containsKey(areaName)) {
            return new Result(true, "KV_AREA_EXISTS");
        }
        //TODO 特殊字符校验
        if(!checkPathLegal(areaName)) {
            return new Result(false, "KV_AREA_CREATE_ERROR");
        }
        Result result;
        try {
            File dir = new File(dataPath + File.separator + areaName);
            if(!dir.exists()) {
                dir.mkdir();
            }
            String filePath = dataPath + File.separator + areaName + File.separator + BASE_DB_NAME;
            DB db = openDB(filePath, true);
            AREAS.put(areaName, db);
            result = Result.getSuccess();
        } catch (Exception e) {
            Log.error("error create area: " + areaName, e);
            result = new Result(false, "KV_AREA_CREATE_ERROR");
        }
        return result;
    }

    public static void close() {
        Collection<DB> dbs = AREAS.values();
        for(DB db : dbs) {
            try {
                db.close();
            } catch (IOException e) {
                Log.warn("close leveldb error", e);
            }
        }
    }

    private static DB openDB(String dbPath, boolean createIfMissing) throws IOException {
        File file = new File(dbPath);
        Options options = new Options().createIfMissing(createIfMissing);
        DBFactory factory = Iq80DBFactory.factory;
        return factory.open(file, options);
    }

    private static boolean checkPathLegal(String areaName) {
        return true;
    }

    private static boolean baseCheckArea(String areaName) {
        if(StringUtils.isBlank(areaName) || !AREAS.containsKey(areaName)) {
            return false;
        }
        return true;
    }

    private static byte[] str2bytes(String str) {
        if(StringUtils.isBlank(str))
            return null;
        try {
            return str.getBytes(NulsConfig.DEFAULT_ENCODING);
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
            if(i == length)
                break;
        }
        return areas;
    }

    public static Result put(String area, byte[] key, byte[] value) {
        if(!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if(key == null || value == null) {
            return Result.getFailed(ErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.put(key, value);
            return Result.getSuccess();
        } catch (Exception e) {
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result put(String area, String key, String value) {
        if(!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if(StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return Result.getFailed(ErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.put(str2bytes(key), str2bytes(value));
            return Result.getSuccess();
        } catch (Exception e) {
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result put(String area, byte[] key, String value) {
        if(!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if(key == null || StringUtils.isBlank(value)) {
            return Result.getFailed(ErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.put(key, str2bytes(value));
            return Result.getSuccess();
        } catch (Exception e) {
            return Result.getFailed(e.getMessage());
        }
    }

    public static Result delete(String area, String key) {
        if(!baseCheckArea(area)) {
            return new Result(true, "KV_AREA_NOT_EXISTS");
        }
        if(StringUtils.isBlank(key)) {
            return Result.getFailed(ErrorCode.NULL_PARAMETER);
        }
        try {
            DB db = AREAS.get(area);
            db.delete(str2bytes(key));
            return Result.getSuccess();
        } catch (Exception e) {
            return Result.getFailed(e.getMessage());
        }
    }

    public static byte[] get(String area, String key) {
        if(!baseCheckArea(area)) {
            return null;
        }
        if(StringUtils.isBlank(key)) {
            return null;
        }
        try {
            DB db = AREAS.get(area);
            return db.get(str2bytes(key));
        } catch (Exception e) {
            return null;
        }
    }

}
