/*
 *
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.rpc.resources.form;

import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.utils.TransactionManager;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Niels
 * @date 2018/3/22
 */
@ApiModel(value = "交易数据")
public class TxForm {

    @ApiModelProperty(name = "data", value = "交易数据", required = true)
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = StringUtils.formatStringPara(data);
    }

    public Transaction getTx() throws Exception {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        Transaction tx = TransactionManager.getInstance(new NulsByteBuffer(Hex.decode(data)));
        return tx;
    }
}
