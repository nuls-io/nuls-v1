package io.nuls.consensus.utils;

import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;

import java.io.*;

/**
 * @author Niels
 * @date 2017/12/26
 */
public class StringFileLoader {

    public static String read(String path) throws NulsException {
        AssertUtil.canNotEmpty(path, ErrorCode.NULL_PARAMETER.getMsg());
        String filePath = StringFileLoader.class.getClassLoader().getResource(path).getPath();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            Log.error(e);
            throw new NulsException(e);
        }
        StringBuilder str = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                str.append(line.trim());
            }
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return str.toString();
    }

}
