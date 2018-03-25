package io.nuls.rpc.sdk.entity;


import java.util.Map;

public class BalanceDto {

    private Long balance;

    private Long usable;

    private Long locked;

    public BalanceDto(Map<String, Object> map) {
        balance= Long.parseLong(""+ map.get("balance"));
        usable= Long.parseLong(""+ map.get("usable"));
        locked= Long.parseLong(""+ map.get("locked"));
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getUsable() {
        return usable;
    }

    public void setUsable(Long usable) {
        this.usable = usable;
    }

    public Long getLocked() {
        return locked;
    }

    public void setLocked(Long locked) {
        this.locked = locked;
    }
}
