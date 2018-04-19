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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class LevelDBManager {

    private static final ConcurrentHashMap<String, DB> AREAS = new ConcurrentHashMap<>();

    private static String dataPath;
    static {
        URL resource = ClassLoader.getSystemClassLoader().getResource(".");
        String classPath = resource.getPath();
        File file = new File(classPath);
        String parent = file.getParent();
        dataPath = parent + "/data/kv";
        File dir = new File(dataPath);
        if(!dir.exists())
            dir.mkdirs();
    }

    public static Result createArea(String areaName) {
        // prevent too many areas
        if(AREAS.size() > 20) {
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
            String filePath = dataPath + File.separator + areaName + File.separator + "leveldb";
            File file = new File(filePath);
            Options options = new Options().createIfMissing(true);
            DBFactory factory = Iq80DBFactory.factory;
            DB db = factory.open(file, options);
            AREAS.put(areaName, db);
            result = Result.getSuccess();
        } catch (Exception e) {
            Log.error("error create area: " + areaName, e);
            result = new Result(false, "KV_AREA_CREATE_ERROR");
        }
        return result;
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
