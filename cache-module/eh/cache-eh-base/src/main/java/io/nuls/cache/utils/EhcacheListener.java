package io.nuls.cache.utils;

import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.model.CacheListenerItem;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

/**
 * @author Niels
 * @date 2018/1/23
 */
public class EhcacheListener implements CacheEventListener {

    private final NulsCacheListener listener;

    public EhcacheListener(NulsCacheListener listener) {
        this.listener = listener;
    }

    @Override
    public void onEvent(CacheEvent event) {
        CacheListenerItem item = new CacheListenerItem(event.getKey(), event.getNewValue(), event.getOldValue());
        switch (event.getType()) {
            case CREATED:
                listener.onCreate(item);
                break;
            case EVICTED:
                listener.onEvict(item);
                break;
            case EXPIRED:
                listener.onExpire(item);
                break;
            case REMOVED:
                listener.onRemove(item);
                break;
            case UPDATED:
                listener.onUpdate(item);
                break;
            default:
                return;
        }
    }
}
