package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/28
 */
public class InputNa2NulsDto extends InputDto {

    public InputNa2NulsDto(Map<String, Object> map) {
        super(map);
    }

    @JsonProperty("value")
    private String getValue2Nuls(){
        return Na.valueOf(getValue()).toText();
    }
}
