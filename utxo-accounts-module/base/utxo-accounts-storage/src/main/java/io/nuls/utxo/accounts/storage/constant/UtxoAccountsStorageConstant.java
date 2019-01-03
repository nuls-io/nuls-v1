/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.utxo.accounts.storage.constant;

/**
 * @desription:
 * @author: cody
 */
public interface UtxoAccountsStorageConstant {
    byte []DB_NAME_UTXO_ACCOUNTS_BLOCK_SYN_KEY = "utxo_accounts_block_syn_key".getBytes();
    String DB_NAME_UTXO_ACCOUNTS_CHANGE_SUFFIX_KEY = "suffix";
//    String DB_NAME_UTXO_ACCOUNTS_BLOCK_INDEX="utxo_accounts_block_index";
    String DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE = "utxo_accounts_block_cache";
    String DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE = "utxo_accounts_confirmed_balance";
//    String DB_NAME_UTXO_ACCOUNTS_LOCKEDTIME_BALANCE = "utxo_accounts_lockedtime_balance";
//    String DB_NAME_UTXO_ACCOUNTS_LOCKEDHEIGHT_BALANCE = "utxo_accounts_lockedheight_balance";
    final int MAX_CACHE_BLOCK_NUM=1100;

}