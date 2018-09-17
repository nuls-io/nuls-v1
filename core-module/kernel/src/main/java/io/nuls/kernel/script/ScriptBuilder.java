/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nuls.kernel.script;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedBytes;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.utils.SerializeUtils;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.nuls.kernel.script.ScriptOpCodes.*;

/**
 * <p>Tools for the construction of commonly used script types. You don't normally need this as it's hidden behind
 * convenience methods on {@link io.nuls.kernel.model}, but they are useful when working with the
 * protocol at a lower level.</p>
 */
public class ScriptBuilder {
    private List<ScriptChunk> chunks;               //命令列表

    /**
     * Creates a fresh ScriptBuilder with an empty program.
     */
    public ScriptBuilder() {
        chunks = Lists.newLinkedList();
    }

    /**
     * Creates a fresh ScriptBuilder with the given program as the starting point.
     */
    public ScriptBuilder(Script template) {
        chunks = new ArrayList<ScriptChunk>(template.getChunks());
    }

    /**
     * Adds the given chunk to the end of the program
     */
    public ScriptBuilder addChunk(ScriptChunk chunk) {
        return addChunk(chunks.size(), chunk);
    }

    /**
     * Adds the given chunk at the given index in the program
     * 添加创建好的命令到指定的下表
     */
    public ScriptBuilder addChunk(int index, ScriptChunk chunk) {
        chunks.add(index, chunk);
        return this;
    }

    /**
     * Adds the given opcode to the end of the program.
     * 添加指定命令到列表最后
     */
    public ScriptBuilder op(int opcode) {
        return op(chunks.size(), opcode);
    }

    /**
     * Adds the given opcode to the given index in the program
     * 添加指定命令到列表指定位置
     */
    public ScriptBuilder op(int index, int opcode) {
        checkArgument(opcode > OP_PUSHDATA4);
        return addChunk(index, new ScriptChunk(opcode, null));
    }

    /**
     * Adds a copy of the given byte array as a data element (i.e. PUSHDATA) at the end of the program.
     * 添加数据命令（只包含数据）到命令列表最后
     */
    public ScriptBuilder data(byte[] data) {
        if (data.length == 0) {
            return smallNum(0);
        } else {
            return data(chunks.size(), data);
        }
    }

    /**
     * Adds a copy of the given byte array as a data element (i.e. PUSHDATA) at the given index in the program.
     * 添加数据命令（只包含数据）到命令列表指定位置
     */
    public ScriptBuilder data(int index, byte[] data) {
        // implements BIP62
        byte[] copy = Arrays.copyOf(data, data.length);
        int opcode;
        /**
         * 如果数据长度为0，则添加OP_0命令
         * */
        if (data.length == 0) {
            opcode = OP_0;
        }
        /**
         * 如果数据长度为1
         * */
        else if (data.length == 1) {
            byte b = data[0];
            /**
             * 如果数据长度为1，而且该字符大于1小于16，则添加相应的命令
             * */
            if (b >= 1 && b <= 16) {
                opcode = Script.encodeToOpN(b);
            } else {
                opcode = 1;
            }
        }
        /**
         * 如果数据长度小于0x4c，则添加该数据长度的命令
         * */
        else if (data.length < OP_PUSHDATA1) {
            opcode = data.length;
        }
        /**
         * 如果数据长度小于256，则添加OP_PUSHDATA1（0x4c）命令
         * */
        else if (data.length < 256) {
            opcode = OP_PUSHDATA1;
        }
        /**
         * 如果数据长度小于65536，则添加OP_PUSHDATA2（0x4d）命令
         * */
        else if (data.length < 65536) {
            opcode = OP_PUSHDATA2;
        } else {
            throw new RuntimeException("Unimplemented");
        }
        return addChunk(index, new ScriptChunk(opcode, copy));
    }

    /**
     * Adds the given number to the end of the program. Automatically uses
     * shortest encoding possible.
     */
    public ScriptBuilder number(long num) {
        return number(chunks.size(), num);
    }

    /**
     * Adds the given number to the given index in the program. Automatically
     * uses shortest encoding possible.
     * 添加数据命令（一个long数据）到命令列表指定位置
     */
    public ScriptBuilder number(int index, long num) {
        if (num == -1) {
            return op(index, OP_1NEGATE);
        } else if (num >= 0 && num <= 16) {
            return addChunk(index, new ScriptChunk(Script.encodeToOpN((int) num), null));
        } else {
            return bigNum(index, num);
        }
    }

    /**
     * Adds the given number as a OP_N opcode to the end of the program.
     * Only handles values 0-16 inclusive.
     *
     * @see #(int)
     */
    public ScriptBuilder smallNum(int num) {
        return smallNum(chunks.size(), num);
    }

    /**
     * Adds the given number as a push data chunk.
     * This is intended to use for negative numbers or values > 16, and although
     * it will accept numbers in the range 0-16 inclusive, the encoding would be
     * considered non-standard.
     *
     * @see #(int)
     */
    protected ScriptBuilder bigNum(long num) {
        return bigNum(chunks.size(), num);
    }

    /**
     * Adds the given number as a OP_N opcode to the given index in the program.
     * Only handles values 0-16 inclusive.
     *
     * @see #(int)
     */
    public ScriptBuilder smallNum(int index, int num) {
        checkArgument(num >= 0, "Cannot encode negative numbers with smallNum");
        checkArgument(num <= 16, "Cannot encode numbers larger than 16 with smallNum");
        return addChunk(index, new ScriptChunk(Script.encodeToOpN(num), null));
    }

    /**
     * Adds the given number as a push data chunk to the given index in the program.
     * This is intended to use for negative numbers or values > 16, and although
     * it will accept numbers in the range 0-16 inclusive, the encoding would be
     * considered non-standard.
     * 将给定的数字作为命令添加到程序中的给定索引中
     *
     * @see #(int)
     */
    protected ScriptBuilder bigNum(int index, long num) {
        final byte[] data;

        if (num == 0) {
            data = new byte[0];
        } else {
            Stack<Byte> result = new Stack<Byte>();
            final boolean neg = num < 0;
            long absvalue = Math.abs(num);

            while (absvalue != 0) {
                result.push((byte) (absvalue & 0xff));
                absvalue >>= 8;
            }

            if ((result.peek() & 0x80) != 0) {
                // The most significant byte is >= 0x80, so push an extra byte that
                // contains just the sign of the value.
                result.push((byte) (neg ? 0x80 : 0));
            } else if (neg) {
                // The most significant byte is < 0x80 and the value is negative,
                // set the sign bit so it is subtracted and interpreted as a
                // negative when converting back to an integral.
                result.push((byte) (result.pop() | 0x80));
            }

            data = new byte[result.size()];
            for (int byteIdx = 0; byteIdx < data.length; byteIdx++) {
                data[byteIdx] = result.get(byteIdx);
            }
        }

        // At most the encoded value could take up to 8 bytes, so we don't need
        // to use OP_PUSHDATA opcodes
        return addChunk(index, new ScriptChunk(data.length, data));
    }

    /**
     * Creates a new immutable Script based on the state of the builder.
     * 根据当前命令列表创建一个不可变脚本
     */
    public Script build() {
        return new Script(chunks);
    }

    /**
     * Creates a scriptPubKey that encodes payment to the given address.
     * 根据地址创建一个OutputScript/scriptPublicKry     转账
     */
    public static Script createOutputScript(byte[] address, int type) {
        //如果是P2SH类型的创建P2SH对应的锁定脚本
        if (type == 0) {
            // OP_HASH160 <scriptHash> OP_EQUAL
            return new ScriptBuilder()
                    .op(OP_HASH160)
                    .data(address)
                    .op(OP_EQUAL)
                    .build();
        } else {
            // OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
            return new ScriptBuilder()
                    .op(OP_DUP)
                    .op(OP_HASH160)
                    .data(address)
                    .op(OP_EQUALVERIFY)
                    .op(OP_CHECKSIG)
                    .build();
        }
    }

    /**
     * Creates a scriptPubKey that encodes payment to the given raw public key.
     * 根据公钥创建一个OutputScript/scriptPublicKry
     * 创建P2PK（Pay-to-Public-Key）锁定脚本
     */
    public static Script createOutputScript(ECKey key) {
        return new ScriptBuilder().data(key.getPubKey()).op(OP_CHECKSIG).build();
    }

    /**
     * Creates a scriptSig that can redeem a pay-to-address output.
     * If given signature is null, incomplete scriptSig will be created with OP_0 instead of signature
     * 根据签名和公钥创建一个pay-to-address的inputScript/scriptSig用于解锁交易的OutputScript/scriptPublicKry
     */
    public static Script createInputScript(@Nullable TransactionSignature signature, ECKey pubKey) {
        byte[] pubkeyBytes = pubKey.getPubKey();
        //byte[] sigBytes = signature != null ? signature.encodeToBitcoin() : new byte[]{};
        byte[] sigBytes = null;
        return new ScriptBuilder().data(sigBytes).data(pubkeyBytes).build();
    }

    /**
     * Creates a scriptSig that can redeem a pay-to-address output.
     * If given signature is null, incomplete scriptSig will be created with OP_0 instead of signature
     * 根据签名和公钥创建一个pay-to-address的inputScript/scriptSig用于解锁交易的OutputScript/scriptPublicKry
     */
    public static Script createNulsInputScript(@Nullable byte[] signBytes, byte[] pubKeyBytes) {
        return new ScriptBuilder().data(signBytes).data(pubKeyBytes).build();
    }

    /**
     * Creates a scriptSig that can redeem a pay-to-pubkey output.
     * If given signature is null, incomplete scriptSig will be created with OP_0 instead of signature
     * 根据签名创建一个pay-to-public_key的inputScript/scriptSig用于解锁交易的OutputScript/scriptPublicKry
     */
    public static Script createInputScript(@Nullable TransactionSignature signature) {
        //byte[] sigBytes = signature != null ? signature.encodeToBitcoin() : new byte[]{};
        byte[] sigBytes = null;
        return new ScriptBuilder().data(sigBytes).build();
    }

    /**
     * Creates a program that requires at least N of the given keys to sign, using OP_CHECKMULTISIG.
     * 根据多个公钥创建多重签名的OutputScript/scriptPublicKry
     */
    public static Script createMultiSigOutputScript(int threshold, List<ECKey> pubkeys) {
        checkArgument(threshold > 0);
        checkArgument(threshold <= pubkeys.size());
        checkArgument(pubkeys.size() <= 16);  // That's the max we can represent with a single opcode.这是我们可以用一个操作码来表示的最大值
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(threshold);
        for (ECKey key : pubkeys) {
            builder.data(key.getPubKey());
        }
        builder.smallNum(pubkeys.size());
        builder.op(OP_CHECKMULTISIG);
        return builder.build();
    }

    /**
     * Creates a program that requires at least N of the given keys to sign, using OP_CHECKMULTISIG.
     * 根据多个公钥创建多重签名的OutputScript/scriptPublicKry
     */
    public static Script createNulsMultiSigOutputScript(int threshold, List<String> pubkeys) {
        checkArgument(threshold > 0);
        checkArgument(threshold <= pubkeys.size());
        checkArgument(pubkeys.size() <= 16);  // That's the max we can represent with a single opcode.这是我们可以用一个操作码来表示的最大值
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(threshold);
        for (String pubKey : pubkeys) {
            builder.data(Hex.decode(pubKey));
        }
        builder.smallNum(pubkeys.size());
        builder.op(OP_CHECKMULTISIG);
        return builder.build();
    }

    /**
     * Create a program that satisfies an OP_CHECKMULTISIG program.
     * 根据多个签名创建inputScript/scriptSig解锁脚本
     **/
    public static Script createByteNulsMultiSigOutputScript(int threshold, List<byte[]> pubkeys) {
        checkArgument(threshold > 0);
        checkArgument(threshold <= pubkeys.size());
        checkArgument(pubkeys.size() <= 16);  // That's the max we can represent with a single opcode.这是我们可以用一个操作码来表示的最大值
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(threshold);
        for (byte[] pubkey : pubkeys) {
            builder.data(pubkey);
        }
        builder.smallNum(pubkeys.size());
        builder.op(OP_CHECKMULTISIG);
        return builder.build();
    }

    /**
     * Create a program that satisfies an OP_CHECKMULTISIG program.
     * 根据多个签名创建inputScript/scriptSig解锁脚本
     **/
    public static Script createMultiSigInputScript(List<TransactionSignature> signatures) {
        List<byte[]> sigs = new ArrayList<byte[]>(signatures.size());
        for (TransactionSignature signature : signatures) {
            //sigs.add(signature.encodeToBitcoin());
        }
        return createMultiSigInputScriptBytes(sigs, null);
    }

    /**
     * Create a program that satisfies an OP_CHECKMULTISIG program.
     */
    public static Script createMultiSigInputScript(TransactionSignature... signatures) {
        return createMultiSigInputScript(Arrays.asList(signatures));
    }

    /**
     * Create a program that satisfies an OP_CHECKMULTISIG program, using pre-encoded signatures.
     */
    public static Script createMultiSigInputScriptBytes(List<byte[]> signatures) {
        return createMultiSigInputScriptBytes(signatures, null);
    }

    /**
     * Create a program that satisfies a pay-to-script hashed OP_CHECKMULTISIG program.
     * If given signature list is null, incomplete scriptSig will be created with OP_0 instead of signatures
     * P2SH（支付到脚本模式，使用多重签名就需要用到这种模式）
     */
    public static Script createP2SHMultiSigInputScript(@Nullable List<TransactionSignature> signatures,
                                                       Script multisigProgram) {
        List<byte[]> sigs = new ArrayList<byte[]>();
        if (signatures == null) {
            // create correct number of empty signatures
            int numSigs = multisigProgram.getNumberOfSignaturesRequiredToSpend();  //花费这笔UTXO需要的签名数量
            for (int i = 0; i < numSigs; i++) {
                sigs.add(new byte[]{});
            }
        } else {
            for (TransactionSignature signature : signatures) {
                //sigs.add(signature.encodeToBitcoin());
            }
        }
        return createMultiSigInputScriptBytes(sigs, multisigProgram.getProgram());
    }

    /**
     * Create a program that satisfies a pay-to-script hashed OP_CHECKMULTISIG program.
     * If given signature list is null, incomplete scriptSig will be created with OP_0 instead of signatures
     * P2SH（支付到脚本模式，使用多重签名就需要用到这种模式）
     */
    public static Script createNulsP2SHMultiSigInputScript(@Nullable List<byte[]> signatures,
                                                           Script multisigProgram) {
        List<byte[]> sigs = new ArrayList<byte[]>();
        if (signatures == null) {
            // create correct number of empty signatures
            int numSigs = multisigProgram.getNumberOfSignaturesRequiredToSpend();  //花费这笔UTXO需要的签名数量
            for (int i = 0; i < numSigs; i++) {
                sigs.add(new byte[]{});
            }
        } else {
            for (byte[] signature : signatures) {
                sigs.add(signature);
            }
        }
        return createMultiSigInputScriptBytes(sigs, multisigProgram.getProgram());
    }

    /**
     * Create a program that satisfies an OP_CHECKMULTISIG program, using pre-encoded signatures.
     * Optionally, appends the script program bytes if spending a P2SH output.
     */
    public static Script createMultiSigInputScriptBytes(List<byte[]> signatures, @Nullable byte[] multisigProgramBytes) {
        checkArgument(signatures.size() <= 16);
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(0);  // Work around a bug in CHECKMULTISIG that is now a required part of the protocol.
        for (byte[] signature : signatures) {
            builder.data(signature);
        }
        if (multisigProgramBytes != null) {
            builder.data(multisigProgramBytes);
        }
        return builder.build();
    }


    /**
     * Returns a copy of the given scriptSig with the signature inserted in the given position.
     * <p>
     * This function assumes that any missing sigs have OP_0 placeholders. If given scriptSig already has all the signatures
     * in place, IllegalArgumentException will be thrown.
     *
     * @param targetIndex     where to insert the signature
     * @param sigsPrefixCount how many items to copy verbatim (e.g. initial OP_0 for multisig)
     * @param sigsSuffixCount how many items to copy verbatim at end (e.g. redeemScript for P2SH)
     */
    public static Script updateScriptWithSignature(Script scriptSig, byte[] signature, int targetIndex,
                                                   int sigsPrefixCount, int sigsSuffixCount) {
        ScriptBuilder builder = new ScriptBuilder();
        List<ScriptChunk> inputChunks = scriptSig.getChunks();
        int totalChunks = inputChunks.size();

        // Check if we have a place to insert, otherwise just return given scriptSig unchanged.
        // We assume here that OP_0 placeholders always go after the sigs, so
        // to find if we have sigs missing, we can just check the chunk in latest sig position
        boolean hasMissingSigs = inputChunks.get(totalChunks - sigsSuffixCount - 1).equalsOpCode(OP_0);
        checkArgument(hasMissingSigs, "ScriptSig is already filled with signatures");

        // copy the prefix
        for (ScriptChunk chunk : inputChunks.subList(0, sigsPrefixCount)) {
            builder.addChunk(chunk);
        }

        // copy the sigs
        int pos = 0;
        boolean inserted = false;
        for (ScriptChunk chunk : inputChunks.subList(sigsPrefixCount, totalChunks - sigsSuffixCount)) {
            if (pos == targetIndex) {
                inserted = true;
                builder.data(signature);
                pos++;
            }
            if (!chunk.equalsOpCode(OP_0)) {
                builder.addChunk(chunk);
                pos++;
            }
        }

        // add OP_0's if needed, since we skipped them in the previous loop
        while (pos < totalChunks - sigsPrefixCount - sigsSuffixCount) {
            if (pos == targetIndex) {
                inserted = true;
                builder.data(signature);
            } else {
                builder.addChunk(new ScriptChunk(OP_0, null));
            }
            pos++;
        }

        // copy the suffix
        for (ScriptChunk chunk : inputChunks.subList(totalChunks - sigsSuffixCount, totalChunks)) {
            builder.addChunk(chunk);
        }

        checkState(inserted);
        return builder.build();
    }

    /**
     * Creates a scriptPubKey that sends to the given script hash. Read
     * <a href="https://github.com/bitcoin/bips/blob/master/bip-0016.mediawiki">BIP 16</a> to learn more about this
     * kind of script.
     * 根据hash创建P2SH锁定脚本
     */
    public static Script createP2SHOutputScript(byte[] hash) {
        checkArgument(hash.length == 23);
        return new ScriptBuilder().op(OP_HASH160).data(hash).op(OP_EQUAL).build();
    }

    /**
     * Creates a scriptPubKey for the given redeem script.
     * 根据赎回脚本创建P2SH的锁定脚本
     */
    public static Script createP2SHOutputScript(Script redeemScript) {
        Address address = new Address(NulsContext.DEFAULT_CHAIN_ID, NulsContext.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(redeemScript.getProgram()));
        //byte[] hash = Utils.sha256hash160(redeemScript.getProgram());
        byte[] hash = address.getAddressBytes();
        return ScriptBuilder.createP2SHOutputScript(hash);
    }

    /**
     * Creates a P2SH output script with given public keys and threshold. Given public keys will be placed in
     * redeem script in the lexicographical sorting order.
     * <p>
     * 使用给定的公钥和阈值创建一个P2SH输出脚本。给定公共密钥将被放置在在字典排序顺序中赎回脚本
     */
    public static Script createP2SHOutputScript(int threshold, List<ECKey> pubkeys) {
        Script redeemScript = createRedeemScript(threshold, pubkeys);
        return createP2SHOutputScript(redeemScript);
    }

    /**
     * Creates redeem script with given public keys and threshold. Given public keys will be placed in
     * redeem script in the lexicographical sorting order.
     */
    public static Script createRedeemScript(int threshold, List<ECKey> pubkeys) {
        pubkeys = new ArrayList<ECKey>(pubkeys);
        Collections.sort(pubkeys, ECKey.PUBKEY_COMPARATOR);
        return ScriptBuilder.createMultiSigOutputScript(threshold, pubkeys);
    }

    /**
     * Creates redeem script with given public keys and threshold. Given public keys will be placed in
     * redeem script in the lexicographical sorting order.
     */
    public static Script createNulsRedeemScript(int threshold, List<String> pubkeys) {
        pubkeys = new ArrayList<String>(pubkeys);
        Collections.sort(pubkeys, PUBKEY_COMPARATOR);
        return ScriptBuilder.createNulsMultiSigOutputScript(threshold, pubkeys);
    }

    /**
     * Creates redeem script with given public keys and threshold. Given public keys will be placed in
     * redeem script in the lexicographical sorting order.
     */
    public static Script createByteNulsRedeemScript(int threshold, List<byte[]> pubkeys) {
        pubkeys = new ArrayList<byte[]>(pubkeys);
        Collections.sort(pubkeys, PUBKEY_BYTE_COMPARATOR);
        return ScriptBuilder.createByteNulsMultiSigOutputScript(threshold, pubkeys);
    }

    /**
     * Creates a script of the form OP_RETURN [data]. This feature allows you to attach a small piece of data (like
     * a hash of something stored elsewhere) to a zero valued output which can never be spent and thus does not pollute
     * the ledger.
     */
    public static Script createOpReturnScript(byte[] data) {
        checkArgument(data.length <= 80);
        return new ScriptBuilder().op(OP_RETURN).data(data).build();
    }

    public static final Comparator<String> PUBKEY_COMPARATOR = new Comparator<String>() {
        private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

        @Override
        public int compare(String k1, String k2) {
            return comparator.compare(Hex.decode(k1), Hex.decode(k2));
        }
    };

    public static final Comparator<byte[]> PUBKEY_BYTE_COMPARATOR = new Comparator<byte[]>() {
        private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

        @Override
        public int compare(byte[] k1, byte[] k2) {
            return comparator.compare(k1, k2);
        }
    };
}
