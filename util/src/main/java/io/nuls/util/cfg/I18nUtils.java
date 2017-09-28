package io.nuls.util.cfg;

import io.nuls.exception.InchainException;
import io.nuls.util.constant.ErrorCode;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public class I18nUtils {

    private static final Map<String,Properties> AllMapping = new HashMap<>();

    private static Properties nowMapping;
    //default language is English
    private static String key = "en";
    //default properties file folder
    private static final String folder = "languages";

    static{
        //load all language properties
        try {
            URL furl = I18nUtils.class.getClassLoader().getResource(folder);
            File folderFile = new File(furl.getPath());
            for(File file :folderFile.listFiles()){
                InputStream is = new FileInputStream(file);
                Properties prop = new Properties();
                prop.load(is);
                String key = file.getName().replace(".properties","");
                AllMapping.put(key,prop);
            }
        } catch (IOException e) {
            throw new InchainException(e);
        }
    }

    public static void setLanguage(String _key){
        if(StringUtils.isEmpty(_key)){
            throw new InchainException(ErrorCode.LANGUAGECANNOTSETTONULL);
        }
        key = _key;
        nowMapping = AllMapping.get(_key);
    }

    public static String get(int id){
         if(nowMapping==null){
            nowMapping = AllMapping.get(key);
        }
        return nowMapping.getProperty(id+"");
    }
}
