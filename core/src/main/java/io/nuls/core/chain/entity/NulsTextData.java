package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * author Facjas
 * date 2017/12/8.
 */
public class NulsTextData extends BaseNulsData{

    private String text="";

    public void setText(String text){
        this.text = text;
    }

    public String getText(){
        return this.text;
    }

    @Override
    public int size() {
        return text.length();
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(text.length());
        stream.write(text.getBytes());
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        long len = byteBuffer.readVarInt();
        try {
            text = new String(byteBuffer.readBytes((int) len), NulsContext.DEFAULT_ENCODING);
        }catch (Exception e){
            System.out.println("encoding error!");
        }
    }
}
