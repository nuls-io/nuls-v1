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