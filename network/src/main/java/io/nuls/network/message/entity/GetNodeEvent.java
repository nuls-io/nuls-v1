/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;

/**
 * @author vivi
 * @date 2017/12/1.
 */
public class GetNodeEvent extends io.nuls.core.event.BaseEvent<BasicTypeData<Integer>> {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    public GetNodeEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_NODE_EVENT);
//        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public GetNodeEvent(int length) {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_NODE_EVENT);
//        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.setEventBody(new BasicTypeData<>(length));
    }

    @Override
    protected BasicTypeData<Integer> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new BasicTypeData<>());
    }

}
