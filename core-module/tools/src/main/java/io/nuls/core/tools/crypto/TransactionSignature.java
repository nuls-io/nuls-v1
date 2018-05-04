/*
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
 *
 */

package io.nuls.core.tools.crypto;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * A TransactionSignature wraps an {@link io.nuls.core.tools.crypto.ECKey.ECDSASignature} and adds methods for handling
 * the additional SIGHASH mode byte that is used.
 */
public class TransactionSignature extends ECKey.ECDSASignature {
    public final int sighashFlags;

    /** Constructs a signature with the given components and SIGHASH_ALL. */
//    public TransactionSignature(BigInteger r, BigInteger s) {
//        this(r, s, Transaction.SigHash.ALL.value);
//    }

    /**
     * Constructs a signature with the given components and raw sighash flag bytes (needed for rule compatibility).
     */
    public TransactionSignature(BigInteger r, BigInteger s, int sighashFlags) {
        super(r, s);
        this.sighashFlags = sighashFlags;
    }

//    /** Constructs a transaction signature based on the ECDSA signature. */
//    public TransactionSignature(ECKey.ECDSASignature signature, Transaction.SigHash mode) {
//        super(signature.r, signature.s);
//        sighashFlags = calcSigHashValue(mode);
//    }
//
//    public static int calcSigHashValue(Transaction.SigHash mode) {
//        Utils.checkState(SigHash.ALL == mode || SigHash.NONE == mode);
//        int sighashFlags = mode.value;
//        return sighashFlags;
//    }
//
//    public Transaction.SigHash sigHashMode() {
//        final int mode = sighashFlags & 0x1f;
//        if (mode == Transaction.SigHash.NONE.value)
//            return Transaction.SigHash.NONE;
//        else if (mode == Transaction.SigHash.SING_INPUT.value)
//            return Transaction.SigHash.SING_INPUT;
//        else
//            return Transaction.SigHash.ALL;
//    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream bos = derByteStream();
            bos.write(sighashFlags);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TransactionSignature decode(byte[] bytes) {
        ECKey.ECDSASignature sig;
        try {
            sig = ECKey.ECDSASignature.decodeFromDER(bytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        return new TransactionSignature(sig.r, sig.s, bytes[bytes.length - 1]);
    }

//    @Override
//    public ECKey.ECDSASignature toCanonicalised() {
//        return new TransactionSignature(super.toCanonicalised(), sigHashMode());
//    }
}
