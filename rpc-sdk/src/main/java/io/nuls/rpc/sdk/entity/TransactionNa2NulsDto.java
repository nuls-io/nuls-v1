package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/28
 */
public class TransactionNa2NulsDto extends TransactionDto {

    private List<InputDto> inputs;

    private List<OutputDto> outputs;

    public TransactionNa2NulsDto(Map<String, Object> map) {
        super(map);
        this.inputs = new ArrayList<>();
        List<Map<String,Object>> inputMapList = (List<Map<String, Object>>) map.get("inputs");
        for(Map<String, Object> inputMap:inputMapList){
            inputs.add(new InputNa2NulsDto(inputMap));
        }
        outputs = new ArrayList<>();
        List<Map<String,Object>> outputMapList = (List<Map<String, Object>>) map.get("outputs");
        for(Map<String, Object> outputMap:outputMapList){
            outputs.add(new OutputNa2NulsDto(outputMap));
        }
    }

    @JsonProperty("fee")
    public String getFee2Nuls() {
        return Na.valueOf(getFee()).toText();
    }

    @JsonProperty("value")
    public String getValue2Nuls() {
        return  Na.valueOf(getValue()).toText();
    }

    @Override
    public List<InputDto> getInputs() {
        return inputs;
    }

    @Override
    public void setInputs(List<InputDto> inputs) {
        this.inputs = inputs;
    }

    @Override
    public List<OutputDto> getOutputs() {
        return outputs;
    }

    @Override
    public void setOutputs(List<OutputDto> outputs) {
        this.outputs = outputs;
    }
}
