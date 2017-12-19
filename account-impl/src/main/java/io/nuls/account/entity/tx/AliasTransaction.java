package io.nuls.account.entity.tx;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.validator.AliasValidator;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasTransaction extends Transaction {

    private String address;

    private String alias;

    private LockNulsTransaction nulsTx;

    public AliasTransaction(String address, String alias) {
        super(AccountConstant.TX_TYPE_ALIAS);
        this.address = address;
        this.alias = alias;

        this.registerValidator(AliasValidator.getInstance());
    }

    public AliasTransaction(String address, String alias, LockNulsTransaction nulsTx) {
        this(address, alias);
        this.nulsTx = nulsTx;
    }

    public AliasTransaction(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    @Override
    public int size() {
        int s = super.size();
        try {
            s += address.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            s += alias.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        s += nulsTx.size();
        return s;
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        try {
            this.address = new String(byteBuffer.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
            this.alias = new String(byteBuffer.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            this.nulsTx = (LockNulsTransaction) TransactionManager.getInstance(byteBuffer);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
    }


    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.writeString(address);
        stream.writeString(alias);
        nulsTx.serializeToStream(stream);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public LockNulsTransaction getNulsTx() {
        return nulsTx;
    }

    public void setNulsTx(LockNulsTransaction nulsTx) {
        this.nulsTx = nulsTx;
    }
}
