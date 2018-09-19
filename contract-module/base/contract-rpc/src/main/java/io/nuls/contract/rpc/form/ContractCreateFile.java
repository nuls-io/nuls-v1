package io.nuls.contract.rpc.form;

import io.nuls.contract.util.ContractUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.InputStream;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/4/20
 */
@ApiModel(value = "创建智能合约表单数据-jar")
public class ContractCreateFile extends ContractBase {

    @ApiModelProperty(name = "contractCode", value = "智能合约代码", required = true)
    private InputStream contractCode;
    @ApiModelProperty(name = "args", value = "参数列表", required = false)
    private Object[] args;

    public InputStream getContractCode() {
        return contractCode;
    }

    public void setContractCode(InputStream contractCode) {
        this.contractCode = contractCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public String[][] getArgs(String[] types) {
        return ContractUtil.twoDimensionalArray(args, types);
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
