package io.nuls.network.message.messageFilter;

public abstract class NulsMessageFilter {
    private String name;
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    abstract boolean doFilter();
}
