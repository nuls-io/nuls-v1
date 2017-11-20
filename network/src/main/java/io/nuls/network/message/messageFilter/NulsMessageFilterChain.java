package io.nuls.network.message.messageFilter;


import io.nuls.core.mesasge.NulsMessage;

import java.util.List;

public class NulsMessageFilterChain {

    protected List<NulsMessageFilter> filterList;
    void addFilter(NulsMessageFilter filter){
        filterList.add(filter);
    }
    void deleteFilter(NulsMessageFilter filter){
        ;
    }

    public boolean doFilter(NulsMessage message){
//        for (int i = 0; i< filterList.size(); i++) {
//            if(!filterList.get(i).filter()) {
//                return false;
//            }
//        }
        return true;
    }
}
