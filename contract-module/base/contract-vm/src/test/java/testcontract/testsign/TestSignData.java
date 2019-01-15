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
package testcontract.testsign;

import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Utils;
import io.nuls.contract.sdk.annotation.View;

/**
 * @author: PierreLuo
 * @date: 2019/1/4
 */
public class TestSignData implements Contract {

    public boolean verify(String data, String sign, String pub) {
        return Utils.verifySignatureData(data, sign, pub);
    }

    public boolean verifyDefaultData() {
        String data = "416e67656c696c6c6f75";
        String sign = "3044022077186ebccb5694e67270724ec1849693072719cb83ccfadbdbe85dab3fc77c1902204fede64d08df7ef9a04a00444b393d2362ee92fec64d7d37855a0746121d58de";
        String pub = "02ab62dc4833795666e75409c0c42b33ae7f8ee496b69e86a0d07276a5c445309f";
        return Utils.verifySignatureData(data, sign, pub);
    }

    @View
    public boolean verifyView(String data, String sign, String pub) {
        return Utils.verifySignatureData(data, sign, pub);
    }

    @View
    public boolean verifyDefaultDataView() {
        String data = "416e67656c696c6c6f75";
        String sign = "3044022077186ebccb5694e67270724ec1849693072719cb83ccfadbdbe85dab3fc77c1902204fede64d08df7ef9a04a00444b393d2362ee92fec64d7d37855a0746121d58de";
        String pub = "02ab62dc4833795666e75409c0c42b33ae7f8ee496b69e86a0d07276a5c445309f";
        return Utils.verifySignatureData(data, sign, pub);
    }
}
