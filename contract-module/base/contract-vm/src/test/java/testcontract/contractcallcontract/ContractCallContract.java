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
package testcontract.contractcallcontract;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Utils;
import io.nuls.contract.sdk.annotation.Payable;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2018/10/6
 */
public class ContractCallContract implements Contract {

    @Override
    @Payable
    public void _payable() {

    }

    @Payable
    public String callContract(Address contract, String methodName, String[] args, BigInteger value) {
        try {
            String[][] args2 = null;
            if(args != null) {
                args2 = new String[args.length][];
                int i = 0;
                for (String arg : args) {
                    args2[i++] = new String[]{arg};
                }
            }
            contract.call(methodName, null, args2, value);
            return "success";
        } catch (Exception e) {
            Utils.revert("exception: " + e.getMessage());
            return e.getMessage();
        }
    }

    @Payable
    public String test(Address contract, String methodName, String[] args, BigInteger value) {
        try {
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
