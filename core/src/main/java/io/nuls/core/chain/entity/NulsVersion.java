package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

/**
 * @author vivi
 * @date 2017/11/23.
 */
public class NulsVersion {

    /**
     * The version number is 2 bytes
     */
    private short version;

    public static final short MAX_VERSION = 32767;

    public static final short MAX_MAIN_VERSION = 15;

    public static final short MAX_SUB_VERSION = 2047;

    public NulsVersion(short version) {
        setVersion(version);
    }

    public NulsVersion(short mainVersion, short subVersion) {
        setVersionBy(mainVersion, subVersion);
    }

    public void setVersion(short version) {
        if (version > MAX_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        this.version = version;
    }

    public short getVersion() {
        return version;
    }

    /**
     * version = mainVersion << 11 + subVersion
     *
     * @param main
     * @param sub
     */
    public void setVersionBy(short main, short sub) {
        if (main <= 0 || main > MAX_MAIN_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        if (main <= 0 || sub > MAX_SUB_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        main = (short) (main << 11);
        version = (short) (main + sub);
    }

    public short getMainVersion() {
        String str = Integer.toBinaryString(version);
        String mainStr = str.substring(0, str.length() - 11);
        return Short.valueOf(mainStr, 2);

    }

    public short getSubVersion() {
        String str = Integer.toBinaryString(version);
        String subStr = str.substring(str.length() - 11);
        return Short.valueOf(subStr, 2);
    }

    public String getStringVersion() {
        return getMainVersion() + "." + getSubVersion();
    }
}
