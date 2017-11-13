package io.nuls.account.entity;

import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ln
 */
public class AccountBody {

    private String subChainId;

    private List<AccountKeyValue> contents;

    public AccountBody(byte[] content) {
        parse(content);
    }

    public AccountBody(List<AccountKeyValue> contents) {
        this.contents = contents;
    }

    public AccountBody(AccountKeyValue[] contents) {
        this.contents = Arrays.asList(contents);
    }

    public static AccountBody empty() {
        AccountBody ab = null;
        try {
            ab = new AccountBody(new byte[0]);
        } catch (Exception e) {
            Log.error(e);
        }
        return ab;
    }

    public final byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if (contents != null) {
                for (AccountKeyValue keyValuePair : contents) {
                    byte[] keyValue = keyValuePair.toByte();
                    bos.write(new VarInt(keyValue.length).encode());
                    bos.write(keyValue);
                }
            }
            return bos.toByteArray();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                Log.error(e);
            } finally {
            }
        }
        return new byte[0];
    }

    public final int size(){
        int s = 0;
        for (AccountKeyValue keyValuePair : contents) {
            s += keyValuePair.toByte().length;
        }
        return s;
    }


    public void parse(byte[] content) {
        if (content == null || content.length == 0) {
            return;
        }
        int cursor = 0;
        contents = new ArrayList<AccountKeyValue>();
        while (true) {
            VarInt varint = new VarInt(content, cursor);
            cursor += varint.getOriginalSizeInBytes();

            AccountKeyValue keyValuePair = new AccountKeyValue(Arrays.copyOfRange(content, cursor, cursor + (int) varint.value));
            contents.add(keyValuePair);

            cursor += varint.value;
            if (cursor >= content.length) {
                break;
            }
        }
    }

    public List<AccountKeyValue> getContents() {
        return contents;
    }

    public void setContents(List<AccountKeyValue> contents) {
        this.contents = contents;
    }

    public AccountKeyValue getAccountKeyValue(String code) {
        if (null == contents) {
            return null;
        }
        for (AccountKeyValue akv : contents) {
            if (akv.getCode().equals(code)) {
                return akv;
            }
        }
        return null;
    }

    public String getSubChainId() {
        return subChainId;
    }

    public void setSubChainId(String subChainId) {
        this.subChainId = subChainId;
    }

    @Override
    public String toString() {
        return "AccountBody [contents=" + contents + "]";
    }
}
