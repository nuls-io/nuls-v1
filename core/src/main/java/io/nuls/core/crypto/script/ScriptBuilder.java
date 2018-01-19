/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.core.crypto.script;


import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.TransactionSignature;
import io.nuls.core.utils.crypto.Utils;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.core.crypto.script.ScriptOpCodes.*;


public class ScriptBuilder {
    private List<ScriptChunk> chunks;

    /** Creates a fresh ScriptBuilder with an empty program. */
    public ScriptBuilder() {
        chunks = new LinkedList<ScriptChunk>();
    }

    /** Creates a fresh ScriptBuilder with the given program as the starting point. */
    public ScriptBuilder(Script template) {
        chunks = new ArrayList<ScriptChunk>(template.getChunks());
    }

    /** Adds the given chunk to the end of the program */
    public ScriptBuilder addChunk(ScriptChunk chunk) {
        return addChunk(chunks.size(), chunk);
    }

    /** Adds the given chunk at the given index in the program */
    public ScriptBuilder addChunk(int index, ScriptChunk chunk) {
        chunks.add(index, chunk);
        return this;
    }

    /** Adds the given opcode to the end of the program. */
    public ScriptBuilder op(int opcode) {
        return op(chunks.size(), opcode);
    }

    /** Adds the given opcode to the given index in the program */
    public ScriptBuilder op(int index, int opcode) {
        Utils.checkState(opcode > OP_PUSHDATA4);
        return addChunk(index, new ScriptChunk(opcode, null));
    }

    /** Adds a copy of the given byte array as a data element (i.e. PUSHDATA) at the end of the program. */
    public ScriptBuilder data(byte[] data) {
        if (data.length == 0) {
            return smallNum(0);
        } else {
            return data(chunks.size(), data);
        }
    }

    /** Adds a copy of the given byte array as a data element (i.e. PUSHDATA) at the given index in the program. */
    public ScriptBuilder data(int index, byte[] data) {
        // implements BIP62
        byte[] copy = Arrays.copyOf(data, data.length);
        int opcode;
        if (data.length == 0) {
            opcode = OP_0;
        } else if (data.length == 1) {
            byte b = data[0];
            if (b >= 1 && b <= 16) {
                opcode = Script.encodeToOpN(b);
            } else {
                opcode = 1;
            }
        } else if (data.length < OP_PUSHDATA1) {
            opcode = data.length;
        } else if (data.length < 256) {
            opcode = OP_PUSHDATA1;
        } else if (data.length < 65536) {
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
        if (num >= 0 && num < 16) {
            return smallNum((int) num);
        } else {
            return bigNum(num);
        }
    }

    /**
     * Adds the given number to the given index in the program. Automatically
     * uses shortest encoding possible.
     */
    public ScriptBuilder number(int index, long num) {
        if (num >= 0 && num < 16) {
            return addChunk(index, new ScriptChunk(Script.encodeToOpN((int) num), null));
        } else {
            return bigNum(index, num);
        }
    }

    /**
     * Adds the given number as a OP_N opcode to the end of the program.
     * Only handles values 0-16 inclusive.
     * 
     */
    public ScriptBuilder smallNum(int num) {
        return smallNum(chunks.size(), num);
    }

    /** Adds the given number as a push data chunk.
     * This is intended to use for negative numbers or values > 16, and although
     * it will accept numbers in the range 0-16 inclusive, the encoding would be
     * considered non-standard.
     */
    protected ScriptBuilder bigNum(long num) {
        return bigNum(chunks.size(), num);
    }

    /**
     * Adds the given number as a OP_N opcode to the given index in the program.
     * Only handles values 0-16 inclusive.
     */
    public ScriptBuilder smallNum(int index, int num) {
        Utils.checkState(num >= 0, "Cannot encode negative numbers with smallNum");
        Utils.checkState(num <= 16, "Cannot encode numbers larger than 16 with smallNum");
        return addChunk(index, new ScriptChunk(Script.encodeToOpN(num), null));
    }

    /**
     * Adds the given number as a push data chunk to the given index in the program.
     * This is intended to use for negative numbers or values > 16, and although
     * it will accept numbers in the range 0-16 inclusive, the encoding would be
     * considered non-standard.
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

    /** Creates a new immutable Script based on the state of the builder. */
    public Script build() {
        return new Script(chunks);
    }

    /** Creates a scriptPubKey that encodes payment to the given address. */
//    public static Script createOutputScript(Address to) {
//        if (to.isP2SHAddress()) {
//            // OP_HASH160 <scriptHash> OP_EQUAL
//            return new ScriptBuilder()
//                .op(OP_HASH160)
//                .data(to.getHash160())
//                .op(OP_EQUAL)
//                .build();
//        } else {
//            // OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
//            return new ScriptBuilder()
//                .op(OP_DUP)
//                .op(OP_HASH160)
//                .data(to.getHash160())
//                .op(OP_EQUALVERIFY)
//                .op(OP_CHECKSIG)
//                .build();
//        }
//    }

    /** Creates a scriptPubKey that encodes payment to the given raw public key. */
    public static Script createOutputScript(ECKey key) {
        return new ScriptBuilder().data(key.getPubKey()).op(OP_CHECKSIG).build();
    }

    /**
     * Creates a scriptSig that can redeem a pay-to-address output.
     * If given signature is null, incomplete scriptSig will be created with OP_0 instead of signature
     */
    public static Script createInputScript(TransactionSignature signature, ECKey pubKey) {
        byte[] pubkeyBytes = pubKey.getPubKey();
        byte[] sigBytes = signature != null ? signature.encode() : new byte[]{};
        return new ScriptBuilder().data(sigBytes).data(pubkeyBytes).build();
    }

    /**
     * Creates a scriptSig that can redeem a pay-to-pubkey output.
     * If given signature is null, incomplete scriptSig will be created with OP_0 instead of signature
     */
    public static Script createInputScript(TransactionSignature signature) {
        byte[] sigBytes = signature != null ? signature.encode() : new byte[]{};
        return new ScriptBuilder().data(sigBytes).build();
    }

    /** Creates a program that requires at least N of the given keys to sign, using OP_CHECKMULTISIG. */
    public static Script createMultiSigOutputScript(int threshold, List<ECKey> pubkeys) {
        Utils.checkState(threshold > 0);
        Utils.checkState(threshold <= pubkeys.size());
        Utils.checkState(pubkeys.size() <= 16);  // That's the max we can represent with a single opcode.
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(threshold);
        for (ECKey key : pubkeys) {
            builder.data(key.getPubKey());
        }
        builder.smallNum(pubkeys.size());
        builder.op(OP_CHECKMULTISIG);
        return builder.build();
    }

    /** Create a program that satisfies an OP_CHECKMULTISIG program. */
    public static Script createMultiSigInputScript(List<TransactionSignature> signatures) {
        List<byte[]> sigs = new ArrayList<byte[]>(signatures.size());
        for (TransactionSignature signature : signatures) {
            sigs.add(signature.encode());
        }

        return createMultiSigInputScriptBytes(sigs, null);
    }

    /** Create a program that satisfies an OP_CHECKMULTISIG program. */
    public static Script createMultiSigInputScript(TransactionSignature... signatures) {
        return createMultiSigInputScript(Arrays.asList(signatures));
    }

    /** Create a program that satisfies an OP_CHECKMULTISIG program, using pre-encoded signatures. */
    public static Script createMultiSigInputScriptBytes(List<byte[]> signatures) {
    	return createMultiSigInputScriptBytes(signatures, null);
    }

    /**
     * Create a program that satisfies a pay-to-script hashed OP_CHECKMULTISIG program.
     * If given signature list is null, incomplete scriptSig will be created with OP_0 instead of signatures
     */
    public static Script createP2SHMultiSigInputScript(List<TransactionSignature> signatures,
                                                       Script multisigProgram) {
        List<byte[]> sigs = new ArrayList<byte[]>();
        if (signatures == null) {
            // create correct number of empty signatures
            int numSigs = multisigProgram.getNumberOfSignaturesRequiredToSpend();
            for (int i = 0; i < numSigs; i++) {
                sigs.add(new byte[]{});
            }
        } else {
            for (TransactionSignature signature : signatures) {
                sigs.add(signature.encode());
            }
        }
        return createMultiSigInputScriptBytes(sigs, multisigProgram.getProgram());
    }

    /**
     * Create a program that satisfies an OP_CHECKMULTISIG program, using pre-encoded signatures. 
     * Optionally, appends the script program bytes if spending a P2SH output.
     */
    public static Script createMultiSigInputScriptBytes(List<byte[]> signatures, byte[] multisigProgramBytes) {
        Utils.checkState(signatures.size() <= 16);
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(0);  // Work around a bug in CHECKMULTISIG that is now a required part of the protocol.
        for (byte[] signature : signatures) {
            builder.data(signature);
        }
        if (multisigProgramBytes!= null) {
            builder.data(multisigProgramBytes);
        }
        return builder.build();
    }

    /**
     * Returns a copy of the given scriptSig with the signature inserted in the given position.
     *
     * This function assumes that any missing sigs have OP_0 placeholders. If given scriptSig already has all the signatures
     * in place, IllegalArgumentException will be thrown.
     *
     * @param targetIndex where to insert the signature
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
        Utils.checkState(hasMissingSigs, "ScriptSig is already filled with signatures");

        // copy the prefix
        for (ScriptChunk chunk: inputChunks.subList(0, sigsPrefixCount)) {
            builder.addChunk(chunk);
        }

        // copy the sigs
        int pos = 0;
        boolean inserted = false;
        for (ScriptChunk chunk: inputChunks.subList(sigsPrefixCount, totalChunks - sigsSuffixCount)) {
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
            }
            else {
                builder.addChunk(new ScriptChunk(OP_0, null));
            }
            pos++;
        }

        // copy the suffix
        for (ScriptChunk chunk: inputChunks.subList(totalChunks - sigsSuffixCount, totalChunks)) {
            builder.addChunk(chunk);
        }

        Utils.checkState(inserted);
        return builder.build();
    }

    /**
     * Creates a scriptPubKey that sends to the given script hash. Read
     * <a href="https://github.com/bitcoin/bips/blob/master/bip-0016.mediawiki">BIP 16</a> to learn more about this
     * kind of script.
     */
    public static Script createP2SHOutputScript(byte[] hash) {
        Utils.checkState(hash.length == 20);
        return new ScriptBuilder().op(OP_HASH160).data(hash).op(OP_EQUAL).build();
    }

    /**
     * Creates a scriptPubKey for the given redeem script.
     */
    public static Script createP2SHOutputScript(Script redeemScript) {
        byte[] hash = Utils.sha256hash160(redeemScript.getProgram());
        return ScriptBuilder.createP2SHOutputScript(hash);
    }

    /**
     * Creates a script of the form OP_RETURN [data]. This feature allows you to attach a small piece of data (like
     * a hash of something stored elsewhere) to a zero valued output which can never be spent and thus does not pollute
     * the ledger.
     */
    public static Script createOpReturnScript(byte[] data) {
        Utils.checkState(data.length <= 80);
        return new ScriptBuilder().op(OP_RETURN).data(data).build();
    }

    public static Script createCLTVPaymentChannelOutput(BigInteger time, ECKey from, ECKey to) {
        byte[] timeBytes = Utils.reverseBytes(Utils.encodeMPI(time, false));
        if (timeBytes.length > 5) {
            throw new RuntimeException("Time too large to encode as 5-byte int");
        }
        return new ScriptBuilder().op(OP_IF)
                .data(to.getPubKey()).op(OP_CHECKSIGVERIFY)
                .op(OP_ELSE)
                .data(timeBytes).op(OP_CHECKLOCKTIMEVERIFY).op(OP_DROP)
                .op(OP_ENDIF)
                .data(from.getPubKey()).op(OP_CHECKSIG).build();
    }

    public static Script createCLTVPaymentChannelRefund(TransactionSignature signature) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(signature.encode());
        builder.data(new byte[] { 0 }); // Use the CHECKLOCKTIMEVERIFY if branch
        return builder.build();
    }

    public static Script createCLTVPaymentChannelP2SHRefund(TransactionSignature signature, Script redeemScript) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(signature.encode());
        builder.data(new byte[] { 0 }); // Use the CHECKLOCKTIMEVERIFY if branch
        builder.data(redeemScript.getProgram());
        return builder.build();
    }

    public static Script createCLTVPaymentChannelP2SHInput(byte[] from, byte[] to, Script redeemScript) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(from);
        builder.data(to);
        builder.smallNum(1); // Use the CHECKLOCKTIMEVERIFY if branch
        builder.data(redeemScript.getProgram());
        return builder.build();
    }

    public static Script createCLTVPaymentChannelInput(TransactionSignature from, TransactionSignature to) {
        return createCLTVPaymentChannelInput(from.encode(), to.encode());
    }

    public static Script createCLTVPaymentChannelInput(byte[] from, byte[] to) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(from);
        builder.data(to);
        builder.smallNum(1); // Use the CHECKLOCKTIMEVERIFY if branch
        return builder.build();
    }
    
    /**
     * 帐户注册输入脚本
     * @param hash160
     * @param signs
     * @param version
     * @return Script
     */
    public static Script createRegisterInputScript(byte[] hash160, byte[][] signs, int version) {
    	ScriptBuilder builder = new ScriptBuilder();
    	
        builder.data(signs[0]);
        builder.data(signs[1]);
        
        builder.smallNum(version);
        builder.data(hash160);
        
        return builder.build();
    }
    
//    /**
//     * 帐户注册输出脚本
//     * @param hash160
//     * @param mgpubkeys
//     * @param trpubkeys
//     * @return
//     */
//    public static Script createRegisterOutScript(byte[] hash160, byte[][] mgpubkeys, byte[][] trpubkeys) {
//    	ScriptBuilder builder = new ScriptBuilder();
//        builder.data(hash160);
//        builder.op(OP_EQUALVERIFY);
//
//        builder.op(ScriptOpCodes.OP_VERMG);
//        builder.op(ScriptOpCodes.OP_IF);
//        builder.data(mgpubkeys[0]);
//        builder.data(mgpubkeys[1]);
//        
//        builder.op(ScriptOpCodes.OP_ELSE);
//        builder.data(trpubkeys[0]);
//        builder.data(trpubkeys[1]);
//        builder.op(ScriptOpCodes.OP_ENDIF);
//        
//        builder.op(ScriptOpCodes.OP_CHECKSIG);
//        
//        return builder.build();
//    }

    /**
     * 创建一个空签名
     * @param type
     * @param hash160
     * @return Script
     */
	public static Script createEmptyInputScript(int type, byte[] hash160) {
		ScriptBuilder builder = new ScriptBuilder();
		
		builder.smallNum(type);
        builder.data(hash160);
        
        return builder.build();
	}
	
	/**
     * 创建一个空签名
     * @param data
     * @return Script
     */
	public static Script createCoinbaseInputScript(byte[] data) {
		ScriptBuilder builder = new ScriptBuilder();

        builder.data(data);
        
        return builder.build();
	}
	
//	/**
//	 * 交易输出脚本
//	 * @param to
//	 * @return Script
//	 */
//	public static Script createOutputScript(Address to) {
//		if(to.isCertAccount()) {
//			//输出到认证账户的交易输出脚本
//	        return new ScriptBuilder()
//        		.op(OP_DROP)
//	            .op(OP_PUBKEY)
//	            .data(to.getHash160())
//	            .op(OP_EQUALVERIFY)
//	            .op(OP_CHECKSIG)
//	            .build();
//		} else {
//			//输出到普通账户的交易输出脚本
//			return new ScriptBuilder()
//					.op(OP_DUP)
//					.op(OP_HASH160)
//					.data(to.getHash160())
//					.op(OP_EQUALVERIFY)
//					.op(OP_CHECKSIG)
//					.build();
//		}
//    }
	
	/**
	 * 认证账户的交易输入签名脚本
	 * @param signs
	 * @param txid
	 * @param hash160
	 * @return Script
	 */
	public static Script createCertAccountInputScript(byte[][] signs, byte[] txid, byte[] hash160) {
		if(signs == null) {
			return new ScriptBuilder()
					.data(new byte[]{})
					.op(OP_VERTR)
					.data(txid)
					.data(hash160)
					.build();
		} else {
			Utils.checkState(signs.length == 1, "签名不正确"); //facjas
			return new ScriptBuilder()
					.data(signs[0])
					//data(signs[1])  //facjas
					.op(OP_VERTR)
					.data(txid)
					.data(hash160)
					.build();
		}
	}

//	/**
//	 * 创建通用的验证脚本
//	 * @param pubkey	公钥
//	 * @param sign		签名
//	 * @return Script
//	 */
//	public static Script createVerifyScript(byte[] pubkey, byte[] sign) {
//        return new ScriptBuilder()
//    		.data(pubkey)
//    		.data(sign)
//            .op(OP_CHECKSIG)
//            .build();
//    }
	
	/**
	 * 创建通用的普通账户验证脚本
	 * @param pubkey	公钥
	 * @param sign		签名
	 * @return Script
	 */
	public static Script createSystemAccountScript(byte[] hash160, byte[] pubkey, byte[] sign) {
        return new ScriptBuilder()
    		.data(pubkey)
    		.op(OP_DUP)
    		.op(OP_HASH160)
    		.data(hash160)
    		.op(OP_EQUALVERIFY)
    		.data(sign)
            .op(OP_CHECKSIG)
            .build();
    }

//	/**
//	 * 创建认证账户签名脚本
//	 * @param type  1账户管理，2交易
//	 * @param txid	认证账户信息的交易Id
//	 * @param hash160	认证账户的hash160
//	 * @param sign1		签名1
//	 * @param sign2		签名2
//	 * @return Script
//	 */
//	public static Script createCertAccountScript(int type, Sha256Hash txid, byte[] hash160, byte[] sign1, byte[] sign2) {
//        if(type == Definition.TX_VERIFY_MG) {
//            return new ScriptBuilder()
//                    .op(ScriptOpCodes.OP_VERMG)
//                    .data(txid.getBytes())
//                    .op(OP_PUBKEY)
//                    .data(hash160)
//                    .op(OP_EQUALVERIFY)
//                    .data(sign1)
//                    .data(sign2)
//                    .op(OP_CHECKSIG)
//                    .build();
//        }else{
//            return new ScriptBuilder()
//                    .op(ScriptOpCodes.OP_VERTR)
//                    .data(txid.getBytes())
//                    .op(OP_PUBKEY)
//                    .data(hash160)
//                    .op(OP_EQUALVERIFY)
//                    .data(sign1)
//                    .op(OP_CHECKSIG)
//                    .build();
//        }
//	}
	
	/**
     * 防伪验证输入脚本
     * @param verifyCodeContent
     * @return Script
     */
	public static Script createAntifakeInputScript(byte[] verifyCodeContent) {
		ScriptBuilder builder = new ScriptBuilder();

		builder.data(verifyCodeContent);
        
        return builder.build();
	}
	
//	/**
//	 * 防伪验证输出脚本
//	 * @param hash160		防伪码生产者账户hash160
//	 * @param antifakeCode		防伪码内容
//	 * @return Script
//	 */
//	public static Script createAntifakeOutputScript(byte[] hash160, Sha256Hash antifakeCode) {
//		Utils.checkNotNull(hash160);
//		Utils.checkNotNull(antifakeCode);
//		//输出到认证账户的交易输出脚本
//        return new ScriptBuilder()
//            .op(OP_SHA256)
//            .data(antifakeCode.getBytes())
//            .op(OP_EQUAL)
//            .build();
//    }

	/**
	 * 创建参与共识保证金输出脚本，使用时只能输出到指定的账户
	 * 该脚本不支持通用的验证，所以在最后返回false，如果攻击者要尝试普通交易花费，则不会验证通过
	 * @param hash160 			//正常退出或者不扣除保证金时的赎回账户
	 * @param punishmentHash160	//惩罚时的赎回账户
	 * @return Script
	 */
	public static Script createConsensusOutputScript(byte[] hash160, byte[] punishmentHash160) {
		return new ScriptBuilder()
            .data(hash160)
            .data(punishmentHash160)
            .data(new byte[] { 0 })
            .build();
	}
}
