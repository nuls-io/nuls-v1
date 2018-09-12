package org.ethereum.trie;

import io.nuls.core.tools.crypto.Hex;
import org.ethereum.crypto.HashUtil;

public class TrieTest {

    public static void main(String[] args) {
        Trie<byte[]> trie = new SecureTrie(new byte[]{});
        System.out.println(Hex.encode(HashUtil.EMPTY_TRIE_HASH));
    }

}
