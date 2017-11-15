package io.nuls.core.crypto.script;

import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.nuls.core.crypto.script.ScriptOpCodes.*;

/**
 * <p>Programs embedded inside transactions that control redemption of payments.</p>
 *
 * <p>Bitcoin transactions don't specify what they do directly. Instead <a href="https://en.bitcoin.it/wiki/Script">a
 * small binary stack language</a> is used to define programs that when evaluated return whether the transaction
 * "accepts" or rejects the other transactions connected to it.</p>
 *
 * <p>In SPV mode, scripts are not run, because that would require all transactions to be available and lightweight
 * clients don't have that data. In full mode, this class is used to run the interpreted language. It also has
 * static methods for building scripts.</p>
 */
public class Script {

    /** Enumeration to encapsulate the type of this script. */
    public enum ScriptType {
        // Do NOT change the ordering of the following definitions because their ordinals are stored in databases.
        NO_TYPE,
        P2PKH,
        PUB_KEY,
        P2SH
    }

    public enum VerifyFlag {
        P2SH, // Enable BIP16-style subscript evaluation.
        STRICTENC, // Passing a non-strict-DER signature or one with undefined hashtype to a checksig operation causes script failure.
        DERSIG, // Passing a non-strict-DER signature to a checksig operation causes script failure (softfork safe, BIP66 rule 1)
        LOW_S, // Passing a non-strict-DER signature or one with S > order/2 to a checksig operation causes script failure
        NULLDUMMY, // Verify dummy stack item consumed by CHECKMULTISIG is of zero-length.
        SIGPUSHONLY, // Using a non-push operator in the scriptSig causes script failure (softfork safe, BIP62 rule 2).
        MINIMALDATA, // Require minimal encodings for all push operations
        DISCOURAGE_UPGRADABLE_NOPS, // Discourage use of NOPs reserved for upgrades (NOP1-10)
        CLEANSTACK, // Require that only a single stack element remains after evaluation.
        CHECKLOCKTIMEVERIFY // Enable CHECKLOCKTIMEVERIFY operation
    }
    public static final EnumSet<VerifyFlag> ALL_VERIFY_FLAGS = EnumSet.allOf(VerifyFlag.class);

    private static final Logger log = LoggerFactory.getLogger(Script.class);
    public static final long MAX_SCRIPT_ELEMENT_SIZE = 520;  // bytes
    public static final int SIG_SIZE = 75;
    /** Max number of sigops allowed in a standard p2sh redeem script */
    public static final int MAX_P2SH_SIGOPS = 15;

    // The program is a set of chunks where each element is either [opcode] or [data, data, data ...]
    protected List<ScriptChunk> chunks;
    // Unfortunately, scripts are not ever re-serialized or canonicalized when used in signature hashing. Thus we
    // must preserve the exact bytes that we read off the wire, along with the parsed form.
    protected byte[] program;

    // Creation time of the associated keys in seconds since the epoch.
    private long creationTimeSeconds;

    /** Creates an empty script that serializes to nothing. */
    private Script() {
        chunks = new ArrayList<ScriptChunk>();
    }

    // Used from ScriptBuilder.
    Script(List<ScriptChunk> chunks) {
        this.chunks = Collections.unmodifiableList(new ArrayList<ScriptChunk>(chunks));
        creationTimeSeconds = TimeService.currentTimeMillis();
    }

    /**
     * Construct a Script that copies and wraps the programBytes array. The array is parsed and checked for syntactic
     * validity.
     * @param programBytes Array of program bytes from a transaction.
     */
    public Script(byte[] programBytes) throws ScriptException {
        program = programBytes;
        parse(programBytes);
        creationTimeSeconds = 0;
    }

    public Script(byte[] programBytes, long creationTimeSeconds) throws ScriptException {
        program = programBytes;
        parse(programBytes);
        this.creationTimeSeconds = creationTimeSeconds;
    }

    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public void setCreationTimeSeconds(long creationTimeSeconds) {
        this.creationTimeSeconds = creationTimeSeconds;
    }

    /**
     * Returns the program opcodes as a string, for example "[1234] DUP HASH160"
     */
    @Override
    public String toString() {
        return Utils.join(chunks);
    }

    /** Returns the serialized program as a newly created byte array. */
    public byte[] getProgram() {
        try {
            // Don't round-trip as Bitcoin Core doesn't and it would introduce a mismatch.
            if (program != null)
                return Arrays.copyOf(program, program.length);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (ScriptChunk chunk : chunks) {
                chunk.write(bos);
            }
            program = bos.toByteArray();
            return program;
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /** Returns an immutable list of the scripts parsed form. Each chunk is either an opcode or data element. */
    public List<ScriptChunk> getChunks() {
        return Collections.unmodifiableList(chunks);
    }

    private static final ScriptChunk[] STANDARD_TRANSACTION_SCRIPT_CHUNKS = {
        new ScriptChunk(ScriptOpCodes.OP_DUP, null, 0),
        new ScriptChunk(ScriptOpCodes.OP_HASH160, null, 1),
        new ScriptChunk(ScriptOpCodes.OP_EQUALVERIFY, null, 23),
        new ScriptChunk(ScriptOpCodes.OP_CHECKSIG, null, 24),
    };

    /**
     * <p>To run a script, first we parse it which breaks it up into chunks representing pushes of data or logical
     * opcodes. Then we can run the parsed chunks.</p>
     *
     * <p>The reason for this split, instead of just interpreting directly, is to make it easier
     * to reach into a programs structure and pull out bits of data without having to run it.
     * This is necessary to render the to/from addresses of transactions in a user interface.
     * Bitcoin Core does something similar.</p>
     */
    private void parse(byte[] program) throws ScriptException {
        chunks = new ArrayList<ScriptChunk>(5);   // Common size.
        ByteArrayInputStream bis = new ByteArrayInputStream(program);
        int initialSize = bis.available();
        while (bis.available() > 0) {
            int startLocationInProgram = initialSize - bis.available();
            int opcode = bis.read();

            long dataToRead = -1;
            if (opcode >= 0 && opcode < OP_PUSHDATA1) {
                // Read some bytes of data, where how many is the opcode value itself.
                dataToRead = opcode;
            } else if (opcode == OP_PUSHDATA1) {
                if (bis.available() < 1) throw new ScriptException("Unexpected end of script");
                dataToRead = bis.read();
            } else if (opcode == OP_PUSHDATA2) {
                // Read a short, then read that many bytes of data.
                if (bis.available() < 2) throw new ScriptException("Unexpected end of script");
                dataToRead = bis.read() | (bis.read() << 8);
            } else if (opcode == OP_PUSHDATA4) {
                // Read a uint32, then read that many bytes of data.
                // Though this is allowed, because its value cannot be > 520, it should never actually be used
                if (bis.available() < 4) throw new ScriptException("Unexpected end of script");
                dataToRead = ((long)bis.read()) | (((long)bis.read()) << 8) | (((long)bis.read()) << 16) | (((long)bis.read()) << 24);
            }

            ScriptChunk chunk;
            if (dataToRead == -1) {
                chunk = new ScriptChunk(opcode, null, startLocationInProgram);
            } else {
                if (dataToRead > bis.available())
                    throw new ScriptException("Push of data element that is larger than remaining data");
                byte[] data = new byte[(int)dataToRead];
                Utils.checkState(dataToRead == 0 || bis.read(data, 0, (int)dataToRead) == dataToRead);
                chunk = new ScriptChunk(opcode, data, startLocationInProgram);
            }
            // Save some memory by eliminating redundant copies of the same chunk objects.
            for (ScriptChunk c : STANDARD_TRANSACTION_SCRIPT_CHUNKS) {
                if (c.equals(chunk)) chunk = c;
            }
            chunks.add(chunk);
        }
    }

    /**
     * Returns true if this script is of the form <pubkey> OP_CHECKSIG. This form was originally intended for transactions
     * where the peers talked to each other directly via TCP/IP, but has fallen out of favor with time due to that mode
     * of operation being susceptible to man-in-the-middle attacks. It is still used in coinbase outputs and can be
     * useful more exotic types of transaction, but today most payments are to addresses.
     */
    public boolean isSentToRawPubKey() {
        return chunks.size() == 2 && chunks.get(1).equalsOpCode(OP_CHECKSIG) &&
               !chunks.get(0).isOpCode() && chunks.get(0).data.length > 1;
    }

    /**
     * Returns true if this script is of the form DUP HASH160 <pubkey hash> EQUALVERIFY CHECKSIG, ie, payment to an
     * address like 1VayNert3x1KzbpzMGt2qdqrAThiRovi8. This form was originally intended for the case where you wish
     * to send somebody money with a written code because their node is offline, but over time has become the standard
     * way to make payments due to the short and recognizable base58 form addresses come in.
     */
//    public boolean isSentToAddress() {
//        return 	(chunks.size() == 5 &&
//        		chunks.get(0).equalsOpCode(OP_DUP) &&
//        		chunks.get(1).equalsOpCode(OP_HASH160) &&
//        		chunks.get(2).data.length == Address.LENGTH &&
//        		chunks.get(3).equalsOpCode(OP_EQUALVERIFY) &&
//        		chunks.get(4).equalsOpCode(OP_CHECKSIG)) ||
//        		 (chunks.size() == 5 &&
//                		chunks.get(0).equalsOpCode(OP_DROP) &&
//                		chunks.get(1).equalsOpCode(OP_PUBKEY) &&
//                		chunks.get(2).data.length == Address.LENGTH &&
//                		chunks.get(3).equalsOpCode(OP_EQUALVERIFY) &&
//                		chunks.get(4).equalsOpCode(OP_CHECKSIG)) ;
//    }
//
//    /**
//     * 是否输出到系统账户（也就是普通账户）
//     * @return boolean
//     */
//    public boolean isSentToSystemAddress() {
//        return 	chunks.size() == 5 &&
//        		chunks.get(0).equalsOpCode(OP_DUP) &&
//        		chunks.get(1).equalsOpCode(OP_HASH160) &&
//        		chunks.get(2).data.length == Address.LENGTH &&
//        		chunks.get(3).equalsOpCode(OP_EQUALVERIFY) &&
//        		chunks.get(4).equalsOpCode(OP_CHECKSIG);
//    }
//
//    /**
//     * 是否输出到认证账户
//     * @return boolean
//     */
//    public boolean isSentToCertAccountAddress() {
//        return 	chunks.size() == 5 &&
//                		chunks.get(0).equalsOpCode(OP_DROP) &&
//                		chunks.get(1).equalsOpCode(OP_PUBKEY) &&
//                		chunks.get(2).data.length == Address.LENGTH &&
//                		chunks.get(3).equalsOpCode(OP_EQUALVERIFY) &&
//                		chunks.get(4).equalsOpCode(OP_CHECKSIG);
//    }

    /**
     * An alias for isPayToScriptHash.
     */
    @Deprecated
    public boolean isSentToP2SH() {
        return isPayToScriptHash();
    }

    /**
     * <p>If a program matches the standard template DUP HASH160 &lt;pubkey hash&gt; EQUALVERIFY CHECKSIG
     * then this function retrieves the third element.
     * In this case, this is useful for fetching the destination address of a transaction.</p>
     * 
     * <p>If a program matches the standard template HASH160 &lt;script hash&gt; EQUAL
     * then this function retrieves the second element.
     * In this case, this is useful for fetching the hash of the redeem script of a transaction.</p>
     * 
     * <p>Otherwise it throws a ScriptException.</p>
     *
     */
//    public byte[] getPubKeyHash() throws ScriptException {
//        if (isSentToAddress())
//            return chunks.get(2).data;
//        else if (isPayToScriptHash())
//            return chunks.get(1).data;
//        else
//            throw new ScriptException("Script not in the standard scriptPubKey form");
//    }

    /**
     * Returns the public key in this script. If a script contains two constants and nothing else, it is assumed to
     * be a scriptSig (input) for a pay-to-address output and the second constant is returned (the first is the
     * signature). If a script contains a constant and an OP_CHECKSIG opcode, the constant is returned as it is
     * assumed to be a direct pay-to-key scriptPubKey (output) and the first constant is the public key.
     *
     * @throws ScriptException if the script is none of the named forms.
     */
    public byte[] getPubKey() throws ScriptException {
        if (chunks.size() != 2) {
            throw new ScriptException("Script not of right size, expecting 2 but got " + chunks.size());
        }
        return chunks.get(1).data;
//        final ScriptChunk chunk0 = chunks.get(0);
//        final byte[] chunk0data = chunk0.data;
//        final ScriptChunk chunk1 = chunks.get(1);
//        final byte[] chunk1data = chunk1.data;
//        if (chunk0data != null && chunk0data.length > 2 && chunk1data != null && chunk1data.length > 2) {
//            // If we have two large constants assume the input to a pay-to-address output.
//            return chunk1data;
//        } else if (chunk1.equalsOpCode(OP_CHECKSIG) && chunk0data != null && chunk0data.length > 2) {
//            // A large constant followed by an OP_CHECKSIG is the key.
//            return chunk0data;
//        } else {
//            throw new ScriptException("Script did not match expected form: " + this);
//        }
    }

    /**
     * Retrieves the sender public key from a LOCKTIMEVERIFY transaction
     * @return byte[]
     * @throws ScriptException
     */
    public byte[] getCLTVPaymentChannelSenderPubKey() throws ScriptException {
        if (!isSentToCLTVPaymentChannel()) {
            throw new ScriptException("Script not a standard CHECKLOCKTIMVERIFY transaction: " + this);
        }
        return chunks.get(8).data;
    }

    /**
     * Retrieves the recipient public key from a LOCKTIMEVERIFY transaction
     * @return byte[]
     * @throws ScriptException
     */
    public byte[] getCLTVPaymentChannelRecipientPubKey() throws ScriptException {
        if (!isSentToCLTVPaymentChannel()) {
            throw new ScriptException("Script not a standard CHECKLOCKTIMVERIFY transaction: " + this);
        }
        return chunks.get(1).data;
    }

//    public BigInteger getCLTVPaymentChannelExpiry() {
//        if (!isSentToCLTVPaymentChannel()) {
//            throw new ScriptException("Script not a standard CHECKLOCKTIMEVERIFY transaction: " + this);
//        }
//        return castToBigInteger(chunks.get(4).data, 5);
//    }
//
//    /**
//     * For 2-element [input] scripts assumes that the paid-to-address can be derived from the public key.
//     * The concept of a "from address" isn't well defined in Bitcoin and you should not assume the sender of a
//     * transaction can actually receive coins on it. This method may be removed in future.
//     */
//    @Deprecated
//    public Address getFromAddress(NetworkParams params) throws ScriptException {
//        return new Address(params, Utils.sha256hash160(getPubKey()));
//    }

    ////////////////////// Interface for writing scripts from scratch ////////////////////////////////

    /**
     * Writes out the given byte buffer to the output stream with the correct opcode prefix
     * To write an integer call writeBytes(out, Utils.reverseBytes(Utils.encodeMPI(val, false)));
     */
    public static void writeBytes(OutputStream os, byte[] buf) throws IOException {
        if (buf.length < OP_PUSHDATA1) {
            os.write(buf.length);
            os.write(buf);
        } else if (buf.length < 256) {
            os.write(OP_PUSHDATA1);
            os.write(buf.length);
            os.write(buf);
        } else if (buf.length < 65536) {
            os.write(OP_PUSHDATA2);
            os.write(0xFF & (buf.length));
            os.write(0xFF & (buf.length >> 8));
            os.write(buf);
        } else {
            throw new RuntimeException("Unimplemented");
        }
    }

    /** Creates a program that requires at least N of the given keys to sign, using OP_CHECKMULTISIG. */
    public static byte[] createMultiSigOutputScript(int threshold, List<ECKey> pubkeys) {
        Utils.checkState(threshold > 0);
        Utils.checkState(threshold <= pubkeys.size());
        Utils.checkState(pubkeys.size() <= 16);  // That's the max we can represent with a single opcode.
        if (pubkeys.size() > 3) {
            log.warn("Creating a multi-signature output that is non-standard: {} pubkeys, should be <= 3", pubkeys.size());
        }
        try {
            ByteArrayOutputStream bits = new ByteArrayOutputStream();
            bits.write(encodeToOpN(threshold));
            for (ECKey key : pubkeys) {
                writeBytes(bits, key.getPubKey());
            }
            bits.write(encodeToOpN(pubkeys.size()));
            bits.write(OP_CHECKMULTISIG);
            return bits.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static byte[] createInputScript(byte[] signature, byte[] pubkey) {
        try {
            // TODO: Do this by creating a Script *first* then having the script reassemble itself into bytes.
            ByteArrayOutputStream bits = new UnsafeByteArrayOutputStream(signature.length + pubkey.length + 2);
            writeBytes(bits, signature);
            writeBytes(bits, pubkey);
            return bits.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] createInputScript(byte[] signature) {
        try {
            // TODO: Do this by creating a Script *first* then having the script reassemble itself into bytes.
            ByteArrayOutputStream bits = new UnsafeByteArrayOutputStream(signature.length + 2);
            writeBytes(bits, signature);
            return bits.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an incomplete scriptSig that, once filled with signatures, can redeem output containing this scriptPubKey.
     * Instead of the signatures resulting script has OP_0.
     * Having incomplete input script allows to pass around partially signed tx.
     * It is expected that this program later on will be updated with proper signatures.
     */
//    public Script createEmptyInputScript(ECKey key, Script redeemScript) {
//        if (isSentToAddress()) {
//            Utils.checkState(key != null, "Key required to create pay-to-address input script");
//            return ScriptBuilder.createInputScript(null, key);
//        } else if (isSentToRawPubKey()) {
//            return ScriptBuilder.createInputScript(null);
//        } else if (isPayToScriptHash()) {
//            Utils.checkState(redeemScript != null, "Redeem script required to create P2SH input script");
//            return ScriptBuilder.createP2SHMultiSigInputScript(null, redeemScript);
//        } else {
//            throw new ScriptException("Do not understand script type: " + this);
//        }
//    }

//    /**
//     * Returns a copy of the given scriptSig with the signature inserted in the given position.
//     */
//    public Script getScriptSigWithSignature(Script scriptSig, byte[] sigBytes, int index) {
//        int sigsPrefixCount = 0;
//        int sigsSuffixCount = 0;
//        if (isPayToScriptHash()) {
//            sigsPrefixCount = 1; // OP_0 <sig>* <redeemScript>
//            sigsSuffixCount = 1;
//        } else if (isSentToMultiSig()) {
//            sigsPrefixCount = 1; // OP_0 <sig>*
//        } else if (isSentToAddress()) {
//            sigsSuffixCount = 1; // <sig> <pubkey>
//        }
//        return ScriptBuilder.updateScriptWithSignature(scriptSig, sigBytes, index, sigsPrefixCount, sigsSuffixCount);
//    }

    /**
     * Returns a list of the keys required by this script, assuming a multi-sig script.
     *
     * @throws ScriptException if the script type is not understood or is pay to address or is P2SH (run this method on the "Redeem script" instead).
     */
    public List<ECKey> getPubKeys() {
        if (!isSentToMultiSig())
            throw new ScriptException("Only usable for multisig scripts.");

        ArrayList<ECKey> result = new ArrayList<ECKey>();
        int numKeys = Script.decodeFromOpN(chunks.get(chunks.size() - 2).opcode);
        for (int i = 0 ; i < numKeys ; i++)
            result.add(ECKey.fromPublicOnly(chunks.get(1 + i).data));
        return result;
    }


    ////////////////////// Interface used during verification of transactions/blocks ////////////////////////////////

    private static int getSigOpCount(List<ScriptChunk> chunks, boolean accurate) throws ScriptException {
        int sigOps = 0;
        int lastOpCode = OP_INVALIDOPCODE;
        for (ScriptChunk chunk : chunks) {
            if (chunk.isOpCode()) {
                switch (chunk.opcode) {
                case OP_CHECKSIG:
                case OP_CHECKSIGVERIFY:
                    sigOps++;
                    break;
                case OP_CHECKMULTISIG:
                case OP_CHECKMULTISIGVERIFY:
                    if (accurate && lastOpCode >= OP_1 && lastOpCode <= OP_16)
                        sigOps += decodeFromOpN(lastOpCode);
                    else
                        sigOps += 20;
                    break;
                default:
                    break;
                }
                lastOpCode = chunk.opcode;
            }
        }
        return sigOps;
    }

    static int decodeFromOpN(int opcode) {
        Utils.checkState((opcode == OP_0 || opcode == OP_1NEGATE) || (opcode >= OP_1 && opcode <= OP_16), "decodeFromOpN called on non OP_N opcode");
        if (opcode == OP_0)
            return 0;
        else if (opcode == OP_1NEGATE)
            return -1;
        else
            return opcode + 1 - OP_1;
    }

    static int encodeToOpN(int value) {
        Utils.checkState(value >= -1 && value <= 16, "encodeToOpN called for " + value + " which we cannot encode in an opcode.");
        if (value == 0)
            return OP_0;
        else if (value == -1)
            return OP_1NEGATE;
        else
            return value - 1 + OP_1;
    }

    /**
     * Gets the count of regular SigOps in the script program (counting multisig ops as 20)
     */
    public static int getSigOpCount(byte[] program) throws ScriptException {
        Script script = new Script();
        try {
            script.parse(program);
        } catch (ScriptException e) {
            // Ignore errors and count up to the parse-able length
        }
        return getSigOpCount(script.chunks, false);
    }
    
    /**
     * Gets the count of P2SH Sig Ops in the Script scriptSig
     */
    public static long getP2SHSigOpCount(byte[] scriptSig) throws ScriptException {
        Script script = new Script();
        try {
            script.parse(scriptSig);
        } catch (ScriptException e) {
            // Ignore errors and count up to the parse-able length
        }
        for (int i = script.chunks.size() - 1; i >= 0; i--)
            if (!script.chunks.get(i).isOpCode()) {
                Script subScript =  new Script();
                subScript.parse(script.chunks.get(i).data);
                return getSigOpCount(subScript.chunks, true);
            }
        return 0;
    }

    /**
     * Returns number of signatures required to satisfy this script.
     */
    public int getNumberOfSignaturesRequiredToSpend() {
        if (isSentToMultiSig()) {
            // for N of M CHECKMULTISIG script we will need N signatures to spend
            ScriptChunk nChunk = chunks.get(0);
            return Script.decodeFromOpN(nChunk.opcode);
        } else if (
                //isSentToAddress() ||
                isSentToRawPubKey()) {
            // pay-to-address and pay-to-pubkey require single sig
            return 1;
        } else if (isPayToScriptHash()) {
            throw new IllegalStateException("For P2SH number of signatures depends on redeem script");
        } else {
            throw new IllegalStateException("Unsupported script type");
        }
    }

    /**
     * Returns number of bytes required to spend this script. It accepts optional ECKey and redeemScript that may
     * be required for certain types of script to estimate target size.
     */
//    public int getNumberOfBytesRequiredToSpend(ECKey pubKey, Script redeemScript) {
//        if (isPayToScriptHash()) {
//            // scriptSig: <sig> [sig] [sig...] <redeemscript>
//            Utils.checkState(redeemScript != null, "P2SH script requires redeemScript to be spent");
//            return redeemScript.getNumberOfSignaturesRequiredToSpend() * SIG_SIZE + redeemScript.getProgram().length;
//        } else if (isSentToMultiSig()) {
//            // scriptSig: OP_0 <sig> [sig] [sig...]
//            return getNumberOfSignaturesRequiredToSpend() * SIG_SIZE + 1;
//        } else if (isSentToRawPubKey()) {
//            // scriptSig: <sig>
//            return SIG_SIZE;
//        } else if (isSentToAddress()) {
//            // scriptSig: <sig> <pubkey>
//            int uncompressedPubKeySize = 65;
//            return SIG_SIZE + (pubKey != null ? pubKey.getPubKey().length : uncompressedPubKeySize);
//        } else {
//            throw new IllegalStateException("Unsupported script type");
//        }
//    }

    /**
     * <p>Whether or not this is a scriptPubKey representing a pay-to-script-hash output. In such outputs, the logic that
     * controls reclamation is not actually in the output at all. Instead there's just a hash, and it's up to the
     * spending input to provide a program matching that hash. This rule is "soft enforced" by the network as it does
     * not exist in Bitcoin Core. It means blocks containing P2SH transactions that don't match
     * correctly are considered valid, but won't be mined upon, so they'll be rapidly re-orgd out of the chain. This
     * logic is defined by <a href="https://github.com/bitcoin/bips/blob/master/bip-0016.mediawiki">BIP 16</a>.</p>
     *
     * <p>bitcoinj does not support creation of P2SH transactions today. The goal of P2SH is to allow short addresses
     * even for complex scripts (eg, multi-sig outputs) so they are convenient to work with in things like QRcodes or
     * with copy/paste, and also to minimize the size of the unspent output set (which improves performance of the
     * Bitcoin system).</p>
     */
    public boolean isPayToScriptHash() {
        // We have to check against the serialized form because BIP16 defines a P2SH output using an exact byte
        // template, not the logical program structure. Thus you can have two programs that look identical when
        // printed out but one is a P2SH script and the other isn't! :(
        byte[] program = getProgram();
        return program.length == 23 &&
               (program[0] & 0xff) == OP_HASH160 &&
               (program[1] & 0xff) == 0x14 &&
               (program[22] & 0xff) == OP_EQUAL;
    }

    /**
     * Returns whether this script matches the format used for multisig outputs: [n] [keys...] [m] CHECKMULTISIG
     */
    public boolean isSentToMultiSig() {
        if (chunks.size() < 4) return false;
        ScriptChunk chunk = chunks.get(chunks.size() - 1);
        // Must end in OP_CHECKMULTISIG[VERIFY].
        if (!chunk.isOpCode()) return false;
        if (!(chunk.equalsOpCode(OP_CHECKMULTISIG) || chunk.equalsOpCode(OP_CHECKMULTISIGVERIFY))) return false;
        try {
            // Second to last chunk must be an OP_N opcode and there should be that many data chunks (keys).
            ScriptChunk m = chunks.get(chunks.size() - 2);
            if (!m.isOpCode()) return false;
            int numKeys = decodeFromOpN(m.opcode);
            if (numKeys < 1 || chunks.size() != 3 + numKeys) return false;
            for (int i = 1; i < chunks.size() - 2; i++) {
                if (chunks.get(i).isOpCode()) return false;
            }
            // First chunk must be an OP_N opcode too.
            if (decodeFromOpN(chunks.get(0).opcode) < 1) return false;
        } catch (IllegalStateException e) {
            return false;   // Not an OP_N opcode.
        }
        return true;
    }

    public boolean isSentToCLTVPaymentChannel() {
        if (chunks.size() != 10) return false;
        // Check that opcodes match the pre-determined format.
        if (!chunks.get(0).equalsOpCode(OP_IF)) return false;
        // chunk[1] = recipient pubkey
        if (!chunks.get(2).equalsOpCode(OP_CHECKSIGVERIFY)) return false;
        if (!chunks.get(3).equalsOpCode(OP_ELSE)) return false;
        // chunk[4] = locktime
        if (!chunks.get(5).equalsOpCode(OP_CHECKLOCKTIMEVERIFY)) return false;
        if (!chunks.get(6).equalsOpCode(OP_DROP)) return false;
        if (!chunks.get(7).equalsOpCode(OP_ENDIF)) return false;
        // chunk[8] = sender pubkey
        if (!chunks.get(9).equalsOpCode(OP_CHECKSIG)) return false;
        return true;
    }

    private static boolean equalsRange(byte[] a, int start, byte[] b) {
        if (start + b.length > a.length)
            return false;
        for (int i = 0; i < b.length; i++)
            if (a[i + start] != b[i])
                return false;
        return true;
    }
    
    /**
     * Returns the script bytes of inputScript with all instances of the specified script object removed
     */
    public static byte[] removeAllInstancesOf(byte[] inputScript, byte[] chunkToRemove) {
        // We usually don't end up removing anything
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(inputScript.length);
        try {
	        int cursor = 0;
	        while (cursor < inputScript.length) {
	            boolean skip = equalsRange(inputScript, cursor, chunkToRemove);
	            
	            int opcode = inputScript[cursor++] & 0xFF;
	            int additionalBytes = 0;
	            if (opcode >= 0 && opcode < OP_PUSHDATA1) {
	                additionalBytes = opcode;
	            } else if (opcode == OP_PUSHDATA1) {
	                additionalBytes = (0xFF & inputScript[cursor]) + 1;
	            } else if (opcode == OP_PUSHDATA2) {
	                additionalBytes = ((0xFF & inputScript[cursor]) |
	                                  ((0xFF & inputScript[cursor+1]) << 8)) + 2;
	            } else if (opcode == OP_PUSHDATA4) {
	                additionalBytes = ((0xFF & inputScript[cursor]) |
	                                  ((0xFF & inputScript[cursor+1]) << 8) |
	                                  ((0xFF & inputScript[cursor+1]) << 16) |
	                                  ((0xFF & inputScript[cursor+1]) << 24)) + 4;
	            }
	            if (!skip) {
	                try {
	                    bos.write(opcode);
	                    bos.write(Arrays.copyOfRange(inputScript, cursor, cursor + additionalBytes));
	                } catch (IOException e) {
	                    throw new RuntimeException(e);
	                }
	            }
	            cursor += additionalBytes;
	        }
	        return bos.toByteArray();
        } finally {
			try {
				bos.close();
			} catch (IOException e) {
				Log.error(e);
			}
		}
    }
    
    /**
     * Returns the script bytes of inputScript with all instances of the given op code removed
     */
    public static byte[] removeAllInstancesOfOp(byte[] inputScript, int opCode) {
        return removeAllInstancesOf(inputScript, new byte[] {(byte)opCode});
    }
    
    ////////////////////// Script verification and helpers ////////////////////////////////
    
    private static boolean castToBool(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0)
            	//除了byte[]{0}之外都为true
                return !(i == data.length - 1 && (data[i] & 0xFF) == 0x80);
        }
        return false;
    }
    
    /**
     * Cast a script chunk to a BigInteger.
     *
     * @see #castToBigInteger(byte[], int) for values with different maximum
     * sizes.
     * @throws ScriptException if the chunk is longer than 4 bytes.
     */
    private static BigInteger castToBigInteger(byte[] chunk) throws ScriptException {
        if (chunk.length > 4)
            throw new ScriptException("Script attempted to use an integer larger than 4 bytes");
        return Utils.decodeMPI(Utils.reverseBytes(chunk), false);
    }

    /**
     * Cast a script chunk to a BigInteger. Normally you would want
     * {@link #castToBigInteger(byte[])} instead, this is only for cases where
     * the normal maximum length does not apply (i.e. CHECKLOCKTIMEVERIFY).
     *
     * @param maxLength the maximum length in bytes.
     * @throws ScriptException if the chunk is longer than the specified maximum.
     */
    private static BigInteger castToBigInteger(final byte[] chunk, final int maxLength) throws ScriptException {
        if (chunk.length > maxLength)
            throw new ScriptException("Script attempted to use an integer larger than "
                + maxLength + " bytes");
        return Utils.decodeMPI(Utils.reverseBytes(chunk), false);
    }

    public boolean isOpReturn() {
        return chunks.size() > 0 && chunks.get(0).equalsOpCode(OP_RETURN);
    }


    // Utility that doesn't copy for internal use
    private byte[] getQuickProgram() {
        if (program != null)
            return program;
        return getProgram();
    }

    /**
     * @return ScriptType
     */
//    public ScriptType getScriptType() {
//        ScriptType type = ScriptType.NO_TYPE;
//        if (isSentToAddress()) {
//            type = ScriptType.P2PKH;
//        } else if (isSentToRawPubKey()) {
//            type = ScriptType.PUB_KEY;
//        } else if (isPayToScriptHash()) {
//            type = ScriptType.P2SH;
//        }
//        return type;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(getQuickProgram(), ((Script)o).getQuickProgram());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getQuickProgram());
    }
    
//    /**
//     * 运行脚本
//     */
//    public void run(Transaction tx, int index, Script out) throws VerificationException {
//    	//TODO
//
//    	LinkedList<byte[]> stack = new LinkedList<byte[]>();
//
//    	//执行输入脚本
//        executeScript(tx, index, this, stack);
//
//        //执行输出脚本
//        executeScript(tx, index, out, stack);
//
//        if (stack.size() == 0)
//            throw new ScriptException("Stack empty at end of script execution.");
//
//        if (!castToBool(stack.pollLast()))
//            throw new ScriptException("Script resulted in a non-true stack: " + stack);
//    }
    
//    public static void executeScript(Transaction tx, int index, Script script, LinkedList<byte[]> stack) {
//    	//操作码数量，最多允许501个
//    	int opCount = 0;
//
//        int lastCodeSepLocation = 0;
//
//        LinkedList<byte[]> altstack = new LinkedList<byte[]>();
//        LinkedList<Boolean> ifStack = new LinkedList<Boolean>();
//
//        for (ScriptChunk chunk : script.chunks) {
//            boolean shouldExecute = !ifStack.contains(false);
//            //压入空值
//            if (chunk.opcode == OP_0) {
//                if (!shouldExecute)
//                    continue;
//                stack.add(new byte[] {});
//            } else if (!chunk.isOpCode()) {
//                if (chunk.data.length > MAX_SCRIPT_ELEMENT_SIZE)
//                    throw new ScriptException("Attempted to push a data string larger than 520 bytes");
//
//                if (!shouldExecute)
//                    continue;
//                stack.add(chunk.data);
//            } else {
//                int opcode = chunk.opcode;
//                if (opcode > OP_16) {
//                    opCount++;
//                    if (opCount > 501)
//                        throw new ScriptException("More script operations than is allowed");
//                }
//                switch (opcode) {
//                case OP_IF:
//                    if (!shouldExecute) {
//                        ifStack.add(false);
//                        continue;
//                    }
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_IF on an empty stack");
//                    ifStack.add(castToBool(stack.pollLast()));
//                    continue;
//                case OP_NOTIF:
//                    if (!shouldExecute) {
//                        ifStack.add(false);
//                        continue;
//                    }
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_NOTIF on an empty stack");
//                    ifStack.add(!castToBool(stack.pollLast()));
//                    continue;
//                case OP_ELSE:
//                    if (ifStack.isEmpty())
//                        throw new ScriptException("Attempted OP_ELSE without OP_IF/NOTIF");
//                    ifStack.add(!ifStack.pollLast());
//                    continue;
//                case OP_ENDIF:
//                    if (ifStack.isEmpty())
//                        throw new ScriptException("Attempted OP_ENDIF without OP_IF/NOTIF");
//                    ifStack.pollLast();
//                    continue;
//                }
//
//                if (!shouldExecute)
//                    continue;
//
//                switch(opcode) {
//                // OP_0 is no opcode
//                case OP_1NEGATE:
//                    stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false)));
//                    break;
//                case OP_1:
//                case OP_2:
//                case OP_3:
//                case OP_4:
//                case OP_5:
//                case OP_6:
//                case OP_7:
//                case OP_8:
//                case OP_9:
//                case OP_10:
//                case OP_11:
//                case OP_12:
//                case OP_13:
//                case OP_14:
//                case OP_15:
//                case OP_16:
//                    stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(decodeFromOpN(opcode)), false)));
//                    break;
//                case OP_DUP:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_DUP on an empty stack");
//                    stack.add(stack.getLast());
//                    break;
//                case OP_DROP:
//                	if (stack.size() < 1)
//                		throw new ScriptException("Attempted OP_DROP on an empty stack");
//                	stack.pollLast();
//                	break;
//                case OP_IFDUP:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_IFDUP on an empty stack");
//                    if (castToBool(stack.getLast()))
//                        stack.add(stack.getLast());
//                    break;
//                case OP_EQUAL:
//                    if (stack.size() < 2)
//                        throw new ScriptException("Attempted OP_EQUALVERIFY on a stack with size < 2");
//                    stack.add(Arrays.equals(stack.pollLast(), stack.pollLast()) ? new byte[] {1} : new byte[] {});
//                    break;
//                case OP_EQUALVERIFY:	//判断栈顶2元素是否相等
//                    if (stack.size() < 2)
//                        throw new ScriptException("Attempted OP_EQUALVERIFY on a stack with size < 2");
//                    byte[] b1 = stack.pollLast();
//                    byte[] b2 = stack.pollLast();
//
//                    if (!Arrays.equals(b1, b2))
//                        throw new ScriptException("OP_EQUALVERIFY: non-equal data");
//                    break;
//                case OP_VERMG:
//                	//配合OP_PUBKEY使用，在OP_PUBKEY之前告知需要获取管理账户的公钥，还是交易的公钥
//                	if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_VERMG on a stack with size < 1");
//            		stack.add(new byte[] {0});
//                	break;
//                case OP_VERTR:
//                	//配合OP_PUBKEY使用，在OP_PUBKEY之前告知需要获取管理账户的公钥，还是交易的公钥
//                	if (stack.size() < 1)
//                		throw new ScriptException("Attempted OP_VERTR on a stack with size < 1");
//            		stack.add(new byte[] {1});
//                	break;
//                case OP_PUBKEY:
//                	//根据栈顶元素的交易hash获取公匙
//                	//至少要有2个元素，栈顶是hash160，第二个是公钥类型
//                    if (stack.size() < 2)
//                        throw new ScriptException("获取公钥时，栈里元素少于所需个数2");
//
//                	BlockStoreProvider blockStoreProvider = SpringContextUtils.getBean(BlockStoreProvider.class);
//
//                	byte[] certTxid = stack.pollLast();
//
//                	TransactionStore txs = blockStoreProvider.getTransaction(certTxid);
//                	if(txs == null || txs.getTransaction() == null) {
//                		throw new ScriptException("引用了错误的账户");
//                	}
//                	Transaction certTx = txs.getTransaction();
//                	if(!(certTx instanceof CertAccountTransaction)) {
//                		throw new ScriptException("引用了错误的交易");
//                	} else {
//                		CertAccountTransaction certAccountTx = (CertAccountTransaction) certTx;
//                		byte[] type = stack.pollLast();
//                		if(Arrays.equals(type, new byte[]{0})) {
//                			//账户管理公钥
//                			byte[][] mgPugkeys = certAccountTx.getMgPubkeys();
//                			for (byte[] bs : mgPugkeys) {
//                				stack.add(bs);
//							}
//                		} else if(Arrays.equals(type, new byte[]{1})) {
//                			//交易管理公钥
//                			byte[][] mgPugkeys = certAccountTx.getTrPubkeys();
//                			for (byte[] bs : mgPugkeys) {
//                				stack.add(bs);
//							}
//                		} else {
//                    		throw new ScriptException("错误的公钥类型");
//                		}
//                		stack.add(certAccountTx.getHash160());
//                	}
//                	break;
//                case OP_RIPEMD160:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_RIPEMD160 on an empty stack");
//                    RIPEMD160Digest digest = new RIPEMD160Digest();
//                    byte[] dataToHash = stack.pollLast();
//                    digest.update(dataToHash, 0, dataToHash.length);
//                    byte[] ripmemdHash = new byte[20];
//                    digest.doFinal(ripmemdHash, 0);
//                    stack.add(ripmemdHash);
//                    break;
//                case OP_SHA1:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_SHA1 on an empty stack");
//                    try {
//                        stack.add(MessageDigest.getInstance("SHA-1").digest(stack.pollLast()));
//                    } catch (NoSuchAlgorithmException e) {
//                        throw new RuntimeException(e);  // Cannot happen.
//                    }
//                    break;
//                case OP_SHA256:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_SHA256 on an empty stack");
//                    stack.add(Sha256Hash.hash(stack.pollLast()));
//                    break;
//                case OP_HASH160:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_HASH160 on an empty stack");
//                    byte[] b = stack.pollLast();
//                    stack.add(Utils.sha256hash160(ECKey.fromPublicOnly(b).getPubKey(false)));
//                    break;
//                case OP_HASH256:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_SHA256 on an empty stack");
//                    stack.add(Sha256Hash.hashTwice(stack.pollLast()));
//                    break;
//                case OP_CODESEPARATOR:
//                    lastCodeSepLocation = chunk.getStartLocationInProgram() + 1;
//                    break;
//                case OP_CHECKSIG:
//                case OP_CHECKSIGVERIFY:
//                    if (tx == null)
//                        throw new IllegalStateException("tx is null");
//                    executeCheckSig(tx, index, script, stack, opcode);
//                    break;
//                default:
//                    throw new ScriptException("Script used a reserved opcode " + opcode);
//                }
//            }
//
//            if (stack.size() + altstack.size() > 1000 || stack.size() + altstack.size() < 0)
//                throw new ScriptException("Stack size exceeded range");
//        }
//
//        if (!ifStack.isEmpty())
//            throw new ScriptException("OP_IF/OP_NOTIF without OP_ENDIF");
//    }

    /*
     * 验证交易签名
     */
//	private static void executeCheckSig(Transaction tx, int index, Script script, LinkedList<byte[]> stack, int opcode) {
//
//		//复制一个tx，否则会导致里面的数据被修改
//		tx = new Transaction(tx.getNetwork(), tx.baseSerialize());
//
//		if(script.isSentToSystemAddress()) {
//			if(stack.size() < 2) {
//				throw new ScriptException("Check sign of the stack size < 2");
//			}
//			byte[] pubkey = stack.pollLast();
//	    	byte[] sigBytes = stack.pollLast();
//
//	    	//清楚验证脚本
//	    	TransactionSignature sig  = TransactionSignature.decode(sigBytes);
//
//	        Sha256Hash hash = tx.hashForSignature(index, script.program, (byte) sig.sighashFlags);
//
//	        boolean sigValid = false;
//
//	        sigValid = ECKey.verify(hash.getBytes(), sig, pubkey);
//
//
//			if (opcode == OP_CHECKSIG) {
//				stack.add(sigValid ? new byte[] {1} : new byte[] {});
//			}
//		} else if(script.isSentToCertAccountAddress()) {
//			if(stack.size() < 2) {
//    			throw new ScriptException("验证签名，栈里元素少于2");
//    		}
//
//			//byte[] pubkey2 = stack.pollLast();
//			byte[] pubkey1 = stack.pollLast();
//        	//byte[] sign2 = stack.pollLast();
//        	byte[] sign1 = stack.pollLast();
//
//        	//清楚验证脚本 facjas
//	    	//TransactionSignature sig  = TransactionSignature.decode(sign2);
//
//        	//Sha256Hash hash = tx.hashForSignature(index, script.program, (byte) sig.sighashFlags);
//
//            //sigValid = ECKey.verify(hash.getBytes(), sig, pubkey2);
//
//
// 	        boolean sigValid = false;
//
//            TransactionSignature sig  = TransactionSignature.decode(sign1);
//
//            Sha256Hash hash = tx.hashForSignature(index, script.program, (byte) sig.sighashFlags);
//
//            sigValid = ECKey.verify(hash.getBytes(), sig, pubkey1);
//
//
// 			if (opcode == OP_CHECKSIG) {
// 				stack.add(sigValid ? new byte[] {1} : new byte[] {});
// 			}
//		}
//	}

//	/**
//	 * hash为验证的内容，先把内容压入栈内，再执行脚步
//	 * @param hash
//	 */
//	public void runVerify(Sha256Hash hash) {
//		LinkedList<byte[]> stack = new LinkedList<byte[]>();
//		stack.add(hash.getBytes());
//
//		executeScript(this, stack);
//	}
	
	/**
	 * hash为验证的内容，先把内容压入栈内，再执行脚步
	 * @param script
	 */
//	public void execute(Script script) {
//		LinkedList<byte[]> stack = new LinkedList<byte[]>();
//		executeScript(this, stack);
//
//		if (stack.size() == 0)
//            throw new ScriptException("Stack empty at end of script execution.");
//
//        if (!castToBool(stack.pollLast()))
//            throw new ScriptException("Script resulted in a non-true stack: " + stack);
//	}
    
//    public static void executeScript(Script script, LinkedList<byte[]> stack) {
//    	//操作码数量，最多允许501个
//    	int opCount = 0;
//
//        int lastCodeSepLocation = 0;
//
//        LinkedList<byte[]> altstack = new LinkedList<byte[]>();
//        LinkedList<Boolean> ifStack = new LinkedList<Boolean>();
//
//        for (ScriptChunk chunk : script.chunks) {
//            boolean shouldExecute = !ifStack.contains(false);
//            //压入空值
//            if (chunk.opcode == OP_0) {
//                if (!shouldExecute)
//                    continue;
//                stack.add(new byte[] {});
//            } else if (!chunk.isOpCode()) {
//                if (chunk.data.length > MAX_SCRIPT_ELEMENT_SIZE)
//                    throw new ScriptException("Attempted to push a data string larger than 520 bytes");
//
//                if (!shouldExecute)
//                    continue;
//                stack.add(chunk.data);
//            } else {
//                int opcode = chunk.opcode;
//                if (opcode > OP_16) {
//                    opCount++;
//                    if (opCount > 501)
//                        throw new ScriptException("More script operations than is allowed");
//                }
//                switch (opcode) {
//                case OP_IF:
//                    if (!shouldExecute) {
//                        ifStack.add(false);
//                        continue;
//                    }
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_IF on an empty stack");
//                    ifStack.add(castToBool(stack.pollLast()));
//                    continue;
//                case OP_NOTIF:
//                    if (!shouldExecute) {
//                        ifStack.add(false);
//                        continue;
//                    }
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_NOTIF on an empty stack");
//                    ifStack.add(!castToBool(stack.pollLast()));
//                    continue;
//                case OP_ELSE:
//                    if (ifStack.isEmpty())
//                        throw new ScriptException("Attempted OP_ELSE without OP_IF/NOTIF");
//                    ifStack.add(!ifStack.pollLast());
//                    continue;
//                case OP_ENDIF:
//                    if (ifStack.isEmpty())
//                        throw new ScriptException("Attempted OP_ENDIF without OP_IF/NOTIF");
//                    ifStack.pollLast();
//                    continue;
//                }
//
//                if (!shouldExecute)
//                    continue;
//
//                switch(opcode) {
//                // OP_0 is no opcode
//                case OP_1NEGATE:
//                    stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false)));
//                    break;
//                case OP_1:
//                case OP_2:
//                case OP_3:
//                case OP_4:
//                case OP_5:
//                case OP_6:
//                case OP_7:
//                case OP_8:
//                case OP_9:
//                case OP_10:
//                case OP_11:
//                case OP_12:
//                case OP_13:
//                case OP_14:
//                case OP_15:
//                case OP_16:
//                    stack.add(Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(decodeFromOpN(opcode)), false)));
//                    break;
//                case OP_DUP:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_DUP on an empty stack");
//                    stack.add(stack.getLast());
//                    break;
//                case OP_DROP:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_DUP on an empty stack");
//                    stack.pollLast();
//                    break;
//                case OP_IFDUP:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_IFDUP on an empty stack");
//                    if (castToBool(stack.getLast()))
//                        stack.add(stack.getLast());
//                    break;
//                case OP_EQUAL:
//                    if (stack.size() < 2)
//                        throw new ScriptException("Attempted OP_EQUALVERIFY on a stack with size < 2");
//                    stack.add(Arrays.equals(stack.pollLast(), stack.pollLast()) ? new byte[] {1} : new byte[] {});
//                    break;
//                case OP_EQUALVERIFY:	//判断栈顶2元素是否相等
//                    if (stack.size() < 2)
//                        throw new ScriptException("Attempted OP_EQUALVERIFY on a stack with size < 2");
//                    byte[] b1 = stack.pollLast();
//                    byte[] b2 = stack.pollLast();
//
//                    if (!Arrays.equals(b1, b2))
//                        throw new ScriptException("OP_EQUALVERIFY: non-equal data");
//                    break;
//                case OP_VERMG:
//                	//配合OP_PUBKEY使用，在OP_PUBKEY之前告知需要获取管理账户的公钥，还是交易的公钥
//                	if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_VERMG on a stack with size < 1");
//            		stack.add(new byte[] {0});
//                	break;
//                case OP_VERTR:
//                	//配合OP_PUBKEY使用，在OP_PUBKEY之前告知需要获取管理账户的公钥，还是交易的公钥
//                	if (stack.size() < 1)
//                		throw new ScriptException("Attempted OP_VERTR on a stack with size < 1");
//            		stack.add(new byte[] {1});
//                	break;
//                case OP_PUBKEY:
//                	//根据栈顶元素的交易hash获取公匙
//                	//至少要有2个元素，栈顶是hash160，第二个是公钥类型
//                    if (stack.size() < 2)
//                        throw new ScriptException("获取公钥时，栈里元素少于所需个数2");
//
//                	BlockStoreProvider blockStoreProvider = SpringContextUtils.getBean(BlockStoreProvider.class);
//
//                	byte[] certTxid = stack.pollLast();
//
//                	TransactionStore txs = blockStoreProvider.getTransaction(certTxid);
//                	if(txs == null || txs.getTransaction() == null) {
//                		throw new ScriptException("引用了错误的账户");
//                	}
//                	Transaction certTx = txs.getTransaction();
//                	if(!(certTx instanceof CertAccountTransaction)) {
//                		throw new ScriptException("引用了错误的交易");
//                	} else {
//                		CertAccountTransaction certAccountTx = (CertAccountTransaction) certTx;
//                		byte[] type = stack.pollLast();
//                		if(Arrays.equals(type, new byte[]{0})) {
//                			//账户管理公钥
//                			byte[][] mgPugkeys = certAccountTx.getMgPubkeys();
//                			for (byte[] bs : mgPugkeys) {
//                				stack.add(bs);
//							}
//                		} else if(Arrays.equals(type, new byte[]{1})) {
//                			//交易管理公钥
//                			byte[][] mgPugkeys = certAccountTx.getTrPubkeys();
//                			for (byte[] bs : mgPugkeys) {
//                				stack.add(bs);
//							}
//                		} else {
//                    		throw new ScriptException("错误的公钥类型");
//                		}
//                		stack.add(certAccountTx.getHash160());
//                	}
//                	break;
//                case OP_RIPEMD160:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_RIPEMD160 on an empty stack");
//                    RIPEMD160Digest digest = new RIPEMD160Digest();
//                    byte[] dataToHash = stack.pollLast();
//                    digest.update(dataToHash, 0, dataToHash.length);
//                    byte[] ripmemdHash = new byte[20];
//                    digest.doFinal(ripmemdHash, 0);
//                    stack.add(ripmemdHash);
//                    break;
//                case OP_SHA1:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_SHA1 on an empty stack");
//                    try {
//                        stack.add(MessageDigest.getInstance("SHA-1").digest(stack.pollLast()));
//                    } catch (NoSuchAlgorithmException e) {
//                        throw new RuntimeException(e);  // Cannot happen.
//                    }
//                    break;
//                case OP_SHA256:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_SHA256 on an empty stack");
//                    stack.add(Sha256Hash.hash(stack.pollLast()));
//                    break;
//                case OP_HASH160:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_HASH160 on an empty stack");
//                    byte[] b = stack.pollLast();
//                    stack.add(Utils.sha256hash160(ECKey.fromPublicOnly(b).getPubKey(false)));
//                    break;
//                case OP_HASH256:
//                    if (stack.size() < 1)
//                        throw new ScriptException("Attempted OP_SHA256 on an empty stack");
//                    stack.add(Sha256Hash.hashTwice(stack.pollLast()));
//                    break;
//                case OP_CODESEPARATOR:
//                    lastCodeSepLocation = chunk.getStartLocationInProgram() + 1;
//                    break;
//                case OP_CHECKSIG:
//                case OP_CHECKSIGVERIFY:  {
//
//                	if(script.isSystemAccount()) {
//                		byte[] sign = stack.pollLast();
//                    	byte[] pubkey = stack.pollLast();
//                    	byte[] hash = stack.pollLast();
//
//                		if(!ECKey.fromPublicOnly(pubkey).verify(hash, sign)) {
//                			throw new ScriptException("Check sign fail");
//                		}
//
//                		if (opcode == OP_CHECKSIG)
//                            stack.add(new byte[] {1});
//                	} else {
//
//                		if(stack.size() != 3 && stack.size() != 5) {
//                			throw new ScriptException("验证签名，栈里元素数量不正确");
//                		}
//                		if(stack.size() == 5) {
//                            //verify mg
//                            byte[] sign2 = stack.pollLast();
//                            byte[] sign1 = stack.pollLast();
//                            byte[] pubkey2 = stack.pollLast();
//                            byte[] pubkey1 = stack.pollLast();
//                            byte[] hash = stack.pollLast();
//
//                            boolean sigValid = ECKey.verify(hash, sign2, pubkey2);
//
//                            if (sigValid) {
//                                sigValid = ECKey.verify(hash, sign1, pubkey1);
//                            }
//
//                            if (opcode == OP_CHECKSIG) {
//                                stack.add(sigValid ? new byte[]{1} : new byte[]{});
//                            }
//                        } else {
//                            //verify tr
//                            byte[] sign = stack.pollLast();
//                            byte[] pubkey = stack.pollLast();
//                            byte[] hash = stack.pollLast();
//                            boolean sigValid = ECKey.verify(hash, sign, pubkey);
//                            if (opcode == OP_CHECKSIG) {
//                                stack.add(sigValid ? new byte[]{1} : new byte[]{});
//                            }
//                        }
//                	}
//                    break;
//                }
//                default:
//                    throw new ScriptException("Script used a reserved opcode " + opcode);
//                }
//            }
//
//            if (stack.size() + altstack.size() > 1000 || stack.size() + altstack.size() < 0)
//                throw new ScriptException("Stack size exceeded range");
//        }
//
//        if (!ifStack.isEmpty())
//            throw new ScriptException("OP_IF/OP_NOTIF without OP_ENDIF");
//    }
//
//    /**
//     * 认证账户类签名脚本运行验证
//     * @param transaction
//     */
//	public void runCertAccountSign(CertAccountTransaction transaction) {
//		//复制一个交易
//		CertAccountTransaction tx = (CertAccountTransaction) transaction.getNetwork().getDefaultSerializer().makeTransaction(transaction.baseSerialize(), 0);
//		//清除验证脚本
//		tx.cleanScripts();
//		//签名内容
//		Sha256Hash hash = Sha256Hash.of(tx.baseSerialize());
//		
//		//运行脚本
//		LinkedList<byte[]> stack = new LinkedList<byte[]>();
//		//操作码数量，最多允许101个
//    	int opCount = 0;
//    	
//		for (ScriptChunk chunk : chunks) {
//        	//压入空值
//            if (chunk.opcode == OP_0) {
//                stack.add(new byte[] {});
//            } else if (!chunk.isOpCode()) {
//                if (chunk.data.length > MAX_SCRIPT_ELEMENT_SIZE)
//                    throw new ScriptException("需要压入的数据超过最大限制，最大限制为 520 bytes");
//                stack.add(chunk.data);
//            } else {
//                int opcode = chunk.opcode;
//                if (opcode > OP_16) {
//                    opCount++;
//                    if (opCount > 101)
//                        throw new ScriptException("超过允许运行的opcode个数");
//                }
//                switch (opcode) {
//                case OP_EQUAL:	//判断栈顶2元素是否相等
//                    if (stack.size() < 2)
//                        throw new ScriptException("判断是否相等时栈顶小于2个元素");
//                    byte[] b1 = stack.pollLast();
//                    byte[] b2 = stack.pollLast();
//                    
//                    if (!Arrays.equals(b1, b2))
//                        throw new ScriptException("OP_EQUAL: 数据不相等");
//                    break;
//                case OP_VERMG:	//获取账户管理的公钥
//            		stack.add(new byte[] {0});
//                	break;
//                case OP_VERTR:	//获取交易的公钥
//            		stack.add(new byte[] {1});
//                	break;
//                case OP_PUBKEY: {
//                	//根据栈顶元素的交易hash获取公匙
//                	//至少要有2个元素，栈顶是hash160，第二个是公钥类型
//                    if (stack.size() < 2)
//                        throw new ScriptException("获取公钥时，栈里元素少于所需个数2");
//                	
//                	BlockStoreProvider blockStoreProvider = SpringContextUtils.getBean(BlockStoreProvider.class);
//                	TransactionStore txs = blockStoreProvider.getTransaction(stack.pollLast());
//                	if(txs == null || txs.getTransaction() == null) {
//                		throw new ScriptException("引用了错误的账户");
//                	}
//                	Transaction certTx = txs.getTransaction();
//                	if(!(certTx instanceof CertAccountTransaction)) {
//                		throw new ScriptException("引用了错误的交易");
//                	} else {
//                		CertAccountTransaction certAccountTx = (CertAccountTransaction) certTx;
//                		byte[] type = stack.pollLast();
//                		if(Arrays.equals(type, new byte[]{0})) {
//                			//账户管理公钥
//                			byte[][] mgPugkeys = certAccountTx.getMgPubkeys();
//                			for (byte[] bs : mgPugkeys) {
//                				stack.add(bs);
//							}
//                		} else if(Arrays.equals(type, new byte[]{1})) {
//                			//交易管理公钥
//                			byte[][] mgPugkeys = certAccountTx.getTrPubkeys();
//                			for (byte[] bs : mgPugkeys) {
//                				stack.add(bs);
//							}
//                		} else {
//                    		throw new ScriptException("错误的公钥类型");
//                		}
//                		stack.add(certAccountTx.getHash160());
//                	}
//                	break;
//                }
//                case OP_CHECKSIG:
//                case OP_CHECKSIGVERIFY:  {
//                	//判断签名是否正确，认证类账户都有2个公私钥对，所有每次需要验证2个签名，应该有4个元素才对
//                	if(stack.size() < 4) {
//            			throw new ScriptException("验证签名，栈里元素少于4");
//            		}
//                	
//                	byte[] sign2 = stack.pollLast();
//                	byte[] sign1 = stack.pollLast();
//                	byte[] pubkey2 = stack.pollLast();
//                	byte[] pubkey1 = stack.pollLast();
//                	
//            		if(!ECKey.fromPublicOnly(pubkey1).verify(hash.getBytes(), sign1) || !ECKey.fromPublicOnly(pubkey2).verify(hash.getBytes(), sign2)) {
//            			throw new ScriptException("签名错误");
//            		}
//                    break;
//                }
//                default:
//                    throw new ScriptException("Script used a reserved opcode " + opcode);
//                }
//            }
//        }
//	}

//	public int getAccountType(NetworkParams network) {
//		if(chunks.size() == 5 &&
//        		chunks.get(0).equalsOpCode(OP_DUP) &&
//        		chunks.get(1).equalsOpCode(OP_HASH160) &&
//        		chunks.get(2).data.length == Address.LENGTH &&
//        		chunks.get(3).equalsOpCode(OP_EQUALVERIFY) &&
//        		chunks.get(4).equalsOpCode(OP_CHECKSIG)) {
//			return network.getSystemAccountVersion();
//		} else if (chunks.size() == 5 &&
//                		chunks.get(0).equalsOpCode(OP_DROP) &&
//                		chunks.get(1).equalsOpCode(OP_PUBKEY) &&
//                		chunks.get(2).data.length == Address.LENGTH &&
//                		chunks.get(3).equalsOpCode(OP_EQUALVERIFY) &&
//                		chunks.get(4).equalsOpCode(OP_CHECKSIG)) {
//			return network.getCertAccountVersion();
//		}
//		return 0;
//	}
//
//	/**
//	 * 根据签名脚本，获取账户的hash160
//	 * @return byte[]
//	 */
//	public byte[] getAccountHash160() {
//		if(isSystemAccount()) {
//			return chunks.get(3).data;
//		} else if(isCertAccount()) {
//			return chunks.get(3).data;
//		} else {
//			throw new VerificationException("不适用的脚本");
//		}
//	}
//
//	/**
//	 * 根据签名脚本，获取账户的地址
//	 * @return Address
//	 */
//	public Address getAccountAddress(NetworkParams network) {
//		if(isSystemAccount()) {
//			return new Address(network, network.getSystemAccountVersion(), getAccountHash160());
//		} else if(isCertAccount()) {
//			return new Address(network, network.getCertAccountVersion(), getAccountHash160());
//		} else {
//			throw new VerificationException("不适用的脚本");
//		}
//	}
//
//	/**
//	 * 根据签名脚本，获取账户的base58地址
//	 * @return byte[]
//	 */
//	public String getAccountBase58(NetworkParams network) {
//		return getAccountAddress(network).getBase58();
//	}
//
//	/**
//	 * 根据签名脚本，获取普通系统账户的公钥
//	 * @return byte[]
//	 */
//	public byte[] getAccountPubkey() {
//		if(isSystemAccount()) {
//			return chunks.get(0).data;
//		} else {
//			throw new VerificationException("不适用的脚本");
//		}
//	}
//
//	/**
//	 * 判断是否是系统账户的签名
//	 * @return boolean
//	 */
//	public boolean isSystemAccount() {
//		return chunks.size() == 7 &&
//        		chunks.get(1).equalsOpCode(OP_DUP) &&
//        		chunks.get(2).equalsOpCode(OP_HASH160) &&
//        		chunks.get(3).data.length == Address.LENGTH &&
//        		chunks.get(4).equalsOpCode(OP_EQUALVERIFY) &&
//        		chunks.get(6).equalsOpCode(OP_CHECKSIG);
//	}
//
//	/**
//	 * 判断是否是认证账户的签名
//	 * @return boolean
//	 */
//	public boolean isCertAccount() {
//	    //facjas
//
//
//		return (chunks.size() == 8 &&
//				    (chunks.get(0).equalsOpCode(OP_VERMG) &&
//				    chunks.get(1).data.length == Sha256Hash.LENGTH &&
//        		    chunks.get(2).equalsOpCode(OP_PUBKEY) &&
//        		    chunks.get(3).data.length == Address.LENGTH &&
//        		    chunks.get(4).equalsOpCode(OP_EQUALVERIFY) &&
//        		    chunks.get(7).equalsOpCode(OP_CHECKSIG))
//        		||
//                (chunks.size() == 7 &&
//                        (chunks.get(0).equalsOpCode(OP_VERTR))) &&
//                chunks.get(1).data.length == Sha256Hash.LENGTH &&
//                chunks.get(2).equalsOpCode(OP_PUBKEY) &&
//                chunks.get(3).data.length == Address.LENGTH &&
//                chunks.get(4).equalsOpCode(OP_EQUALVERIFY) &&
//                chunks.get(6).equalsOpCode(OP_CHECKSIG));    // 认证用户交易密钥只有一对 ，facjas
//	}
	
	/**
	 * 获取认证账户的账户信息对应的交易
	 * @return Sha256Hash
	 */
//	public Sha256Hash getCertAccountTxHash() {
//		if(isCertAccount()) {
//			return Sha256Hash.of(chunks.get(1).data);
//		} else {
//			return null;
//		}
//	}
	
	/**
     * 是否是防伪验证输入脚本
     * @return boolean
     */
	public boolean isAntifakeInputScript() {
		if(chunks == null || chunks.size() != 1 || chunks.get(0).data.length != 32) {
			return false;
		}
        return true;
	}
	
//	/**
//	 * 是否是参与共识保证金赎回脚本
//	 * @return boolean
//	 */
//	public boolean isConsensusOutputScript() {
//		if(chunks == null || chunks.size() != 3 || chunks.get(0).isOpCode() || chunks.get(0).data.length != Address.LENGTH
//				|| chunks.get(1).isOpCode() || chunks.get(1).data.length != Address.LENGTH ||
//				chunks.get(2).isOpCode() || !Arrays.equals(chunks.get(2).data, new byte[] { 0 })) {
//			return false;
//		}
//		return true;
//	}
}
