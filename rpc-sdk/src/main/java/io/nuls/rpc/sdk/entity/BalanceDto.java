package io.nuls.rpc.sdk.entity;


import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.Map;

public class BalanceDto {

    private Long balance;

    private Long usable;

    private Long locked;

    public BalanceDto(Map<String, Object> map) {
        balance= StringUtils.parseLong(map.get("balance"));
        usable= StringUtils.parseLong(map.get("usable"));
        locked= StringUtils.parseLong(map.get("locked"));
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
