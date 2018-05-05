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



package io.nuls.kernel.script;

import java.util.HashMap;
import java.util.Map;

/**
 * Various constants that define the assembly-like scripting language that forms part of the Bitcoin protocol.
 * See   for details. Also provides a method to convert them to a string.
 */
public class ScriptOpCodes {
    // push value
    public static final int OP_0 = 0x00; // push empty vector
    public static final int OP_FALSE = OP_0;
    public static final int OP_PUSHDATA1 = 0x4c;
    public static final int OP_PUSHDATA2 = 0x4d;
    public static final int OP_PUSHDATA4 = 0x4e;
    public static final int OP_1NEGATE = 0x4f;
    public static final int OP_RESERVED = 0x50;
    public static final int OP_1 = 0x51;
    public static final int OP_TRUE = OP_1;
    public static final int OP_2 = 0x52;
    public static final int OP_3 = 0x53;
    public static final int OP_4 = 0x54;
    public static final int OP_5 = 0x55;
    public static final int OP_6 = 0x56;
    public static final int OP_7 = 0x57;
    public static final int OP_8 = 0x58;
    public static final int OP_9 = 0x59;
    public static final int OP_10 = 0x5a;
    public static final int OP_11 = 0x5b;
    public static final int OP_12 = 0x5c;
    public static final int OP_13 = 0x5d;
    public static final int OP_14 = 0x5e;
    public static final int OP_15 = 0x5f;
    public static final int OP_16 = 0x60;

    // control
    public static final int OP_NOP = 0x61;
    public static final int OP_VER = 0x62;
    public static final int OP_IF = 0x63;
    public static final int OP_NOTIF = 0x64;
    public static final int OP_VERIF = 0x65;
    public static final int OP_VERNOTIF = 0x66;
    public static final int OP_ELSE = 0x67;
    public static final int OP_ENDIF = 0x68;
    public static final int OP_VERIFY = 0x69;
    public static final int OP_RETURN = 0x6a;

    // stack ops
    public static final int OP_TOALTSTACK = 0x6b;
    public static final int OP_FROMALTSTACK = 0x6c;
    public static final int OP_2DROP = 0x6d;
    public static final int OP_2DUP = 0x6e;
    public static final int OP_3DUP = 0x6f;
    public static final int OP_2OVER = 0x70;
    public static final int OP_2ROT = 0x71;
    public static final int OP_2SWAP = 0x72;
    public static final int OP_IFDUP = 0x73;
    public static final int OP_DEPTH = 0x74;
    public static final int OP_DROP = 0x75;
    public static final int OP_DUP = 0x76;
    public static final int OP_NIP = 0x77;
    public static final int OP_OVER = 0x78;
    public static final int OP_PICK = 0x79;
    public static final int OP_ROLL = 0x7a;
    public static final int OP_ROT = 0x7b;
    public static final int OP_SWAP = 0x7c;
    public static final int OP_TUCK = 0x7d;

    // splice ops
    public static final int OP_CAT = 0x7e;
    public static final int OP_SUBSTR = 0x7f;
    public static final int OP_LEFT = 0x80;
    public static final int OP_RIGHT = 0x81;
    public static final int OP_SIZE = 0x82;

    // bit logic
    public static final int OP_INVERT = 0x83;
    public static final int OP_AND = 0x84;
    public static final int OP_OR = 0x85;
    public static final int OP_XOR = 0x86;
    public static final int OP_EQUAL = 0x87;
    public static final int OP_EQUALVERIFY = 0x88;
    public static final int OP_RESERVED1 = 0x89;
    public static final int OP_RESERVED2 = 0x8a;

    // numeric
    public static final int OP_1ADD = 0x8b;
    public static final int OP_1SUB = 0x8c;
    public static final int OP_2MUL = 0x8d;
    public static final int OP_2DIV = 0x8e;
    public static final int OP_NEGATE = 0x8f;
    public static final int OP_ABS = 0x90;
    public static final int OP_NOT = 0x91;
    public static final int OP_0NOTEQUAL = 0x92;
    public static final int OP_ADD = 0x93;
    public static final int OP_SUB = 0x94;
    public static final int OP_MUL = 0x95;
    public static final int OP_DIV = 0x96;
    public static final int OP_MOD = 0x97;
    public static final int OP_LSHIFT = 0x98;
    public static final int OP_RSHIFT = 0x99;
    public static final int OP_BOOLAND = 0x9a;
    public static final int OP_BOOLOR = 0x9b;
    public static final int OP_NUMEQUAL = 0x9c;
    public static final int OP_NUMEQUALVERIFY = 0x9d;
    public static final int OP_NUMNOTEQUAL = 0x9e;
    public static final int OP_LESSTHAN = 0x9f;
    public static final int OP_GREATERTHAN = 0xa0;
    public static final int OP_LESSTHANOREQUAL = 0xa1;
    public static final int OP_GREATERTHANOREQUAL = 0xa2;
    public static final int OP_MIN = 0xa3;
    public static final int OP_MAX = 0xa4;
    public static final int OP_WITHIN = 0xa5;

    // crypto
    public static final int OP_RIPEMD160 = 0xa6;
    public static final int OP_SHA1 = 0xa7;
    public static final int OP_SHA256 = 0xa8;
    public static final int OP_HASH160 = 0xa9;
    public static final int OP_HASH256 = 0xaa;
    public static final int OP_CODESEPARATOR = 0xab;
    public static final int OP_CHECKSIG = 0xac;
    public static final int OP_CHECKSIGVERIFY = 0xad;
    public static final int OP_CHECKMULTISIG = 0xae;
    public static final int OP_CHECKMULTISIGVERIFY = 0xaf;

    // block state
    /** Check lock time of the block. Introduced in BIP 65, replacing OP_NOP2 */
    public static final int OP_CHECKLOCKTIMEVERIFY = 0xb1;

    // expansion
    public static final int OP_NOP1 = 0xb0;
    /** Deprecated by BIP 65 */
    public static final int OP_NOP2 = OP_CHECKLOCKTIMEVERIFY;
    public static final int OP_NOP3 = 0xb2;
    public static final int OP_NOP4 = 0xb3;
    public static final int OP_NOP5 = 0xb4;
    public static final int OP_NOP6 = 0xb5;
    public static final int OP_NOP7 = 0xb6;
    public static final int OP_NOP8 = 0xb7;
    public static final int OP_NOP9 = 0xb8;
    public static final int OP_NOP10 = 0xb9;
    
    //OP_MG 交易类型判断，栈顶元素是否是帐户管理类交易  Transaction.VERSION_REGISTER, Transaction.VERSION_CHANGEPWD
    public static final int OP_VERMG = 0xc1;
    //交易类型判断，栈顶元素是否是资金交易  Transaction.VERSION_PAY
    public static final int OP_VERTR = 0xc2;
    //根据栈顶元素的交易hash获取公匙和hash160
    public static final int OP_PUBKEY = 0xc3;
    
    public static final int OP_INVALIDOPCODE = 0xff;

    private static final Map<Integer, String> OP_CODE_MAP = new HashMap<Integer, String>();
    private static final Map<String, Integer> OP_CODE_NAME_MAP = new HashMap<String, Integer>();
    
    static {
    	OP_CODE_MAP.put(OP_0, "0");
        OP_CODE_MAP.put(OP_0, "0");
        OP_CODE_MAP.put(OP_PUSHDATA1, "PUSHDATA1");
        OP_CODE_MAP.put(OP_PUSHDATA2, "PUSHDATA2");
        OP_CODE_MAP.put(OP_PUSHDATA4, "PUSHDATA4");
        OP_CODE_MAP.put(OP_1NEGATE, "1NEGATE");
        OP_CODE_MAP.put(OP_RESERVED, "RESERVED");
        OP_CODE_MAP.put(OP_1, "1");
        OP_CODE_MAP.put(OP_2, "2");
        OP_CODE_MAP.put(OP_3, "3");
        OP_CODE_MAP.put(OP_4, "4");
        OP_CODE_MAP.put(OP_5, "5");
        OP_CODE_MAP.put(OP_6, "6");
        OP_CODE_MAP.put(OP_7, "7");
        OP_CODE_MAP.put(OP_8, "8");
        OP_CODE_MAP.put(OP_9, "9");
        OP_CODE_MAP.put(OP_10, "10");
        OP_CODE_MAP.put(OP_11, "11");
        OP_CODE_MAP.put(OP_12, "12");
        OP_CODE_MAP.put(OP_13, "13");
        OP_CODE_MAP.put(OP_14, "14");
        OP_CODE_MAP.put(OP_15, "15");
        OP_CODE_MAP.put(OP_16, "16");
        OP_CODE_MAP.put(OP_NOP, "NOP");
        OP_CODE_MAP.put(OP_VER, "VER");
        OP_CODE_MAP.put(OP_IF, "IF");
        OP_CODE_MAP.put(OP_NOTIF, "NOTIF");
        OP_CODE_MAP.put(OP_VERIF, "VERIF");
        OP_CODE_MAP.put(OP_VERNOTIF, "VERNOTIF");
        OP_CODE_MAP.put(OP_ELSE, "ELSE");
        OP_CODE_MAP.put(OP_ENDIF, "ENDIF");
        OP_CODE_MAP.put(OP_VERIFY, "VERIFY");
        OP_CODE_MAP.put(OP_RETURN, "RETURN");
        OP_CODE_MAP.put(OP_TOALTSTACK, "TOALTSTACK");
        OP_CODE_MAP.put(OP_FROMALTSTACK, "FROMALTSTACK");
        OP_CODE_MAP.put(OP_2DROP, "2DROP");
        OP_CODE_MAP.put(OP_2DUP, "2DUP");
        OP_CODE_MAP.put(OP_3DUP, "3DUP");
        OP_CODE_MAP.put(OP_2OVER, "2OVER");
        OP_CODE_MAP.put(OP_2ROT, "2ROT");
        OP_CODE_MAP.put(OP_2SWAP, "2SWAP");
        OP_CODE_MAP.put(OP_IFDUP, "IFDUP");
        OP_CODE_MAP.put(OP_DEPTH, "DEPTH");
        OP_CODE_MAP.put(OP_DROP, "DROP");
        OP_CODE_MAP.put(OP_DUP, "DUP");
        OP_CODE_MAP.put(OP_NIP, "NIP");
        OP_CODE_MAP.put(OP_OVER, "OVER");
        OP_CODE_MAP.put(OP_PICK, "PICK");
        OP_CODE_MAP.put(OP_ROLL, "ROLL");
        OP_CODE_MAP.put(OP_ROT, "ROT");
        OP_CODE_MAP.put(OP_SWAP, "SWAP");
        OP_CODE_MAP.put(OP_TUCK, "TUCK");
        OP_CODE_MAP.put(OP_CAT, "CAT");
        OP_CODE_MAP.put(OP_SUBSTR, "SUBSTR");
        OP_CODE_MAP.put(OP_LEFT, "LEFT");
        OP_CODE_MAP.put(OP_RIGHT, "RIGHT");
        OP_CODE_MAP.put(OP_SIZE, "SIZE");
        OP_CODE_MAP.put(OP_INVERT, "INVERT");
        OP_CODE_MAP.put(OP_AND, "AND");
        OP_CODE_MAP.put(OP_OR, "OR");
        OP_CODE_MAP.put(OP_XOR, "XOR");
        OP_CODE_MAP.put(OP_EQUAL, "EQUAL");
        OP_CODE_MAP.put(OP_EQUALVERIFY, "EQUALVERIFY");
        OP_CODE_MAP.put(OP_RESERVED1, "RESERVED1");
        OP_CODE_MAP.put(OP_RESERVED2, "RESERVED2");
        OP_CODE_MAP.put(OP_1ADD, "1ADD");
        OP_CODE_MAP.put(OP_1SUB, "1SUB");
        OP_CODE_MAP.put(OP_2MUL, "2MUL");
        OP_CODE_MAP.put(OP_2DIV, "2DIV");
        OP_CODE_MAP.put(OP_NEGATE, "NEGATE");
        OP_CODE_MAP.put(OP_ABS, "ABS");
        OP_CODE_MAP.put(OP_NOT, "NOT");
        OP_CODE_MAP.put(OP_0NOTEQUAL, "0NOTEQUAL");
        OP_CODE_MAP.put(OP_ADD, "ADD");
        OP_CODE_MAP.put(OP_SUB, "SUB");
        OP_CODE_MAP.put(OP_MUL, "MUL");
        OP_CODE_MAP.put(OP_DIV, "DIV");
        OP_CODE_MAP.put(OP_MOD, "MOD");
        OP_CODE_MAP.put(OP_LSHIFT, "LSHIFT");
        OP_CODE_MAP.put(OP_RSHIFT, "RSHIFT");
        OP_CODE_MAP.put(OP_BOOLAND, "BOOLAND");
        OP_CODE_MAP.put(OP_BOOLOR, "BOOLOR");
        OP_CODE_MAP.put(OP_NUMEQUAL, "NUMEQUAL");
        OP_CODE_MAP.put(OP_NUMEQUALVERIFY, "NUMEQUALVERIFY");
        OP_CODE_MAP.put(OP_NUMNOTEQUAL, "NUMNOTEQUAL");
        OP_CODE_MAP.put(OP_LESSTHAN, "LESSTHAN");
        OP_CODE_MAP.put(OP_GREATERTHAN, "GREATERTHAN");
        OP_CODE_MAP.put(OP_LESSTHANOREQUAL, "LESSTHANOREQUAL");
        OP_CODE_MAP.put(OP_GREATERTHANOREQUAL, "GREATERTHANOREQUAL");
        OP_CODE_MAP.put(OP_MIN, "MIN");
        OP_CODE_MAP.put(OP_MAX, "MAX");
        OP_CODE_MAP.put(OP_WITHIN, "WITHIN");
        OP_CODE_MAP.put(OP_RIPEMD160, "RIPEMD160");
        OP_CODE_MAP.put(OP_SHA1, "SHA1");
        OP_CODE_MAP.put(OP_SHA256, "SHA256");
        OP_CODE_MAP.put(OP_HASH160, "HASH160");
        OP_CODE_MAP.put(OP_HASH256, "HASH256");
        OP_CODE_MAP.put(OP_CODESEPARATOR, "CODESEPARATOR");
        OP_CODE_MAP.put(OP_CHECKSIG, "CHECKSIG");
        OP_CODE_MAP.put(OP_CHECKSIGVERIFY, "CHECKSIGVERIFY");
        OP_CODE_MAP.put(OP_CHECKMULTISIG, "CHECKMULTISIG");
        OP_CODE_MAP.put(OP_CHECKMULTISIGVERIFY, "CHECKMULTISIGVERIFY");
        OP_CODE_MAP.put(OP_NOP1, "NOP1");
        OP_CODE_MAP.put(OP_CHECKLOCKTIMEVERIFY, "CHECKLOCKTIMEVERIFY");
        OP_CODE_MAP.put(OP_NOP3, "NOP3");
        OP_CODE_MAP.put(OP_NOP4, "NOP4");
        OP_CODE_MAP.put(OP_NOP5, "NOP5");
        OP_CODE_MAP.put(OP_NOP6, "NOP6");
        OP_CODE_MAP.put(OP_NOP7, "NOP7");
        OP_CODE_MAP.put(OP_NOP8, "NOP8");
        OP_CODE_MAP.put(OP_NOP9, "NOP9");
        OP_CODE_MAP.put(OP_NOP10, "NOP10");
        
        OP_CODE_MAP.put(OP_VERMG, "VERMG");
        OP_CODE_MAP.put(OP_VERTR, "VERTR");
        OP_CODE_MAP.put(OP_PUBKEY, "PUBKEY");
        
        OP_CODE_NAME_MAP.put("0", OP_0);
        OP_CODE_NAME_MAP.put("PUSHDATA1", OP_PUSHDATA1);
        OP_CODE_NAME_MAP.put("PUSHDATA2", OP_PUSHDATA2);
        OP_CODE_NAME_MAP.put("PUSHDATA4", OP_PUSHDATA4);
        OP_CODE_NAME_MAP.put("1NEGATE", OP_1NEGATE);
        OP_CODE_NAME_MAP.put("RESERVED", OP_RESERVED);
        OP_CODE_NAME_MAP.put("1", OP_1);
        OP_CODE_NAME_MAP.put("2", OP_2);
        OP_CODE_NAME_MAP.put("3", OP_3);
        OP_CODE_NAME_MAP.put("4", OP_4);
        OP_CODE_NAME_MAP.put("5", OP_5);
        OP_CODE_NAME_MAP.put("6", OP_6);
        OP_CODE_NAME_MAP.put("7", OP_7);
        OP_CODE_NAME_MAP.put("8", OP_8);
        OP_CODE_NAME_MAP.put("9", OP_9);
        OP_CODE_NAME_MAP.put("10", OP_10);
        OP_CODE_NAME_MAP.put("11", OP_11);
        OP_CODE_NAME_MAP.put("12", OP_12);
        OP_CODE_NAME_MAP.put("13", OP_13);
        OP_CODE_NAME_MAP.put("14", OP_14);
        OP_CODE_NAME_MAP.put("15", OP_15);
        OP_CODE_NAME_MAP.put("16", OP_16);
        OP_CODE_NAME_MAP.put("NOP", OP_NOP);
        OP_CODE_NAME_MAP.put("VER", OP_VER);
        OP_CODE_NAME_MAP.put("IF", OP_IF);
        OP_CODE_NAME_MAP.put("NOTIF", OP_NOTIF);
        OP_CODE_NAME_MAP.put("VERIF", OP_VERIF);
        OP_CODE_NAME_MAP.put("VERNOTIF", OP_VERNOTIF);
        OP_CODE_NAME_MAP.put("ELSE", OP_ELSE);
        OP_CODE_NAME_MAP.put("ENDIF", OP_ENDIF);
        OP_CODE_NAME_MAP.put("VERIFY", OP_VERIFY);
        OP_CODE_NAME_MAP.put("RETURN", OP_RETURN);
        OP_CODE_NAME_MAP.put("TOALTSTACK", OP_TOALTSTACK);
        OP_CODE_NAME_MAP.put("FROMALTSTACK", OP_FROMALTSTACK);
        OP_CODE_NAME_MAP.put("2DROP", OP_2DROP);
        OP_CODE_NAME_MAP.put("2DUP", OP_2DUP);
        OP_CODE_NAME_MAP.put("3DUP", OP_3DUP);
        OP_CODE_NAME_MAP.put("2OVER", OP_2OVER);
        OP_CODE_NAME_MAP.put("2ROT", OP_2ROT);
        OP_CODE_NAME_MAP.put("2SWAP", OP_2SWAP);
        OP_CODE_NAME_MAP.put("IFDUP", OP_IFDUP);
        OP_CODE_NAME_MAP.put("DEPTH", OP_DEPTH);
        OP_CODE_NAME_MAP.put("DROP", OP_DROP);
        OP_CODE_NAME_MAP.put("DUP", OP_DUP);
        OP_CODE_NAME_MAP.put("NIP", OP_NIP);
        OP_CODE_NAME_MAP.put("OVER", OP_OVER);
        OP_CODE_NAME_MAP.put("PICK", OP_PICK);
        OP_CODE_NAME_MAP.put("ROLL", OP_ROLL);
        OP_CODE_NAME_MAP.put("ROT", OP_ROT);
        OP_CODE_NAME_MAP.put("SWAP", OP_SWAP);
        OP_CODE_NAME_MAP.put("TUCK", OP_TUCK);
        OP_CODE_NAME_MAP.put("CAT", OP_CAT);
        OP_CODE_NAME_MAP.put("SUBSTR", OP_SUBSTR);
        OP_CODE_NAME_MAP.put("LEFT", OP_LEFT);
        OP_CODE_NAME_MAP.put("RIGHT", OP_RIGHT);
        OP_CODE_NAME_MAP.put("SIZE", OP_SIZE);
        OP_CODE_NAME_MAP.put("INVERT", OP_INVERT);
        OP_CODE_NAME_MAP.put("AND", OP_AND);
        OP_CODE_NAME_MAP.put("OR", OP_OR);
        OP_CODE_NAME_MAP.put("XOR", OP_XOR);
        OP_CODE_NAME_MAP.put("EQUAL", OP_EQUAL);
        OP_CODE_NAME_MAP.put("EQUALVERIFY", OP_EQUALVERIFY);
        OP_CODE_NAME_MAP.put("RESERVED1", OP_RESERVED1);
        OP_CODE_NAME_MAP.put("RESERVED2", OP_RESERVED2);
        OP_CODE_NAME_MAP.put("1ADD", OP_1ADD);
        OP_CODE_NAME_MAP.put("1SUB", OP_1SUB);
        OP_CODE_NAME_MAP.put("2MUL", OP_2MUL);
        OP_CODE_NAME_MAP.put("2DIV", OP_2DIV);
        OP_CODE_NAME_MAP.put("NEGATE", OP_NEGATE);
        OP_CODE_NAME_MAP.put("ABS", OP_ABS);
        OP_CODE_NAME_MAP.put("NOT", OP_NOT);
        OP_CODE_NAME_MAP.put("0NOTEQUAL", OP_0NOTEQUAL);
        OP_CODE_NAME_MAP.put("ADD", OP_ADD);
        OP_CODE_NAME_MAP.put("SUB", OP_SUB);
        OP_CODE_NAME_MAP.put("MUL", OP_MUL);
        OP_CODE_NAME_MAP.put("DIV", OP_DIV);
        OP_CODE_NAME_MAP.put("MOD", OP_MOD);
        OP_CODE_NAME_MAP.put("LSHIFT", OP_LSHIFT);
        OP_CODE_NAME_MAP.put("RSHIFT", OP_RSHIFT);
        OP_CODE_NAME_MAP.put("BOOLAND", OP_BOOLAND);
        OP_CODE_NAME_MAP.put("BOOLOR", OP_BOOLOR);
        OP_CODE_NAME_MAP.put("NUMEQUAL", OP_NUMEQUAL);
        OP_CODE_NAME_MAP.put("NUMEQUALVERIFY", OP_NUMEQUALVERIFY);
        OP_CODE_NAME_MAP.put("NUMNOTEQUAL", OP_NUMNOTEQUAL);
        OP_CODE_NAME_MAP.put("LESSTHAN", OP_LESSTHAN);
        OP_CODE_NAME_MAP.put("GREATERTHAN", OP_GREATERTHAN);
        OP_CODE_NAME_MAP.put("LESSTHANOREQUAL", OP_LESSTHANOREQUAL);
        OP_CODE_NAME_MAP.put("GREATERTHANOREQUAL", OP_GREATERTHANOREQUAL);
        OP_CODE_NAME_MAP.put("MIN", OP_MIN);
        OP_CODE_NAME_MAP.put("MAX", OP_MAX);
        OP_CODE_NAME_MAP.put("WITHIN", OP_WITHIN);
        OP_CODE_NAME_MAP.put("RIPEMD160", OP_RIPEMD160);
        OP_CODE_NAME_MAP.put("SHA1", OP_SHA1);
        OP_CODE_NAME_MAP.put("SHA256", OP_SHA256);
        OP_CODE_NAME_MAP.put("HASH160", OP_HASH160);
        OP_CODE_NAME_MAP.put("HASH256", OP_HASH256);
        OP_CODE_NAME_MAP.put("CODESEPARATOR", OP_CODESEPARATOR);
        OP_CODE_NAME_MAP.put("CHECKSIG", OP_CHECKSIG);
        OP_CODE_NAME_MAP.put("CHECKSIGVERIFY", OP_CHECKSIGVERIFY);
        OP_CODE_NAME_MAP.put("CHECKMULTISIG", OP_CHECKMULTISIG);
        OP_CODE_NAME_MAP.put("CHECKMULTISIGVERIFY", OP_CHECKMULTISIGVERIFY);
        OP_CODE_NAME_MAP.put("NOP1", OP_NOP1);
        OP_CODE_NAME_MAP.put("CHECKLOCKTIMEVERIFY", OP_CHECKLOCKTIMEVERIFY);
        OP_CODE_NAME_MAP.put("NOP2", OP_NOP2);
        OP_CODE_NAME_MAP.put("NOP3", OP_NOP3);
        OP_CODE_NAME_MAP.put("NOP4", OP_NOP4);
        OP_CODE_NAME_MAP.put("NOP5", OP_NOP5);
        OP_CODE_NAME_MAP.put("NOP6", OP_NOP6);
        OP_CODE_NAME_MAP.put("NOP7", OP_NOP7);
        OP_CODE_NAME_MAP.put("NOP8", OP_NOP8);
        OP_CODE_NAME_MAP.put("NOP9", OP_NOP9);
        OP_CODE_NAME_MAP.put("NOP10", OP_NOP10);

        OP_CODE_NAME_MAP.put("VERMG", OP_VERMG);
        OP_CODE_NAME_MAP.put("VERTR", OP_VERTR);
        OP_CODE_NAME_MAP.put("PUBKEY", OP_PUBKEY);
    }

    /**
     * Converts the given OpCode into a string (eg "0", "PUSHDATA", or "NON_OP(10)")
     */
    public static String getOpCodeName(int opcode) {
        if (OP_CODE_MAP.containsKey(opcode)) {
            return OP_CODE_MAP.get(opcode);
        }

        return "NON_OP(" + opcode + ")";
    }

    /**
     * Converts the given pushdata OpCode into a string (eg "PUSHDATA2", or "PUSHDATA(23)")
     */
    public static String getPushDataName(int opcode) {
        if (OP_CODE_MAP.containsKey(opcode)) {
            return OP_CODE_MAP.get(opcode);
        }

        return "PUSHDATA(" + opcode + ")";
    }

    /**
     * Converts the given OpCodeName into an int
     */
    public static int getOpCode(String opCodeName) {
        if (OP_CODE_NAME_MAP.containsKey(opCodeName)) {
            return OP_CODE_NAME_MAP.get(opCodeName);
        }

        return OP_INVALIDOPCODE;
    }
}
