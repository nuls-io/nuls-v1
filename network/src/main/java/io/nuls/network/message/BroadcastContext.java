package io.nuls.network.message;

import io.nuls.network.entity.BroadcastResult;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author vivi
 * @date 2017/12/8.
 */
public class BroadcastContext {

    private static final BroadcastContext INSTALL = new BroadcastContext();

    private Map<String, BroadcastResult> context = new Hashtable<String, BroadcastResult>();

    private BroadcastContext() {
    }

    public synchronized static BroadcastContext get() {
        return INSTALL;
    }

    public void add(String hash, BroadcastResult result) {
        context.put(hash, result);
    }

    public boolean exist(String hash) {
        return context.containsKey(hash);
    }

    public BroadcastResult get(String hash) {
        return context.get(hash);
    }

    public BroadcastResult remove(String hash) {
        return context.remove(hash);
    }
}
