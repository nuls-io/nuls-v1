package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;


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

    public static final short MAX_SUB_VERSION = 4095;

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
     * version = mainVersion << 12 + subVersion
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
        version = (short) (main << 12 | sub);
    }

    public short getMainVersion() {
        return (short) ((version >> 12) & 0xF);
    }

    public short getSubVersion() {
        return (short) (version & 0xFFF);
    }

    public String getStringVersion() {
        return getMainVersion() + "." + getSubVersion();
    }

    public static void main(String[] args) {
        NulsVersion v1 = new NulsVersion((short) 1, (short) 0004);
        System.out.println(v1.getVersion());
        byte[] bytes = new VarInt(v1.getVersion()).encode();
        short b = (short) new NulsByteBuffer(bytes).readVarInt();
        System.out.println(b);
    }

}
