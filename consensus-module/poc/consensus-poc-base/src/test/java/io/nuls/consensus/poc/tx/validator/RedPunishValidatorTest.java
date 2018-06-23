/*
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

package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.model.validator.HeaderSignValidator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/6/23
 */
public class RedPunishValidatorTest {

    @Test
    public void validate() {
        byte[] bytes = Hex.decode("08002caf54276401000100a96e025378a0dfb880cda4978ffee7374ba010787401fd96010020e4fc1859c1a66dc1a12359201d989ac487f0c95fa1e0964e178f4845c4f3e8b10020b5b552ada9da0bddcea0703fe94187034732f02443fdc28d4b1b06a03c0e462220185427640153060000010000000eb7f20600030000ca5327640102002103133c05a5ba309992fc5c77cf389bc945b525ba0ac7cda08045984f6f616d9bbf0046304402204c8f016b645e7e183b7669c52826c9532e802f3c54ae9ef0991c3853c5edf04d022000d4e02ce983af6cb58169526b416eda7ac0db797f23d5c1672db156429b70eb0020f7fd0ec69bc936ceeab3d9320c7c9bcc9b137fdfffc1dd691d8d8a3cf0985b99002029fdbade205923827b56aba7a0cd1b5b236d67adb130704e5641a36ef772cba540665427640153060000020000000eb8f206000300303f5427640101002103133c05a5ba309992fc5c77cf389bc945b525ba0ac7cda08045984f6f616d9bbf00463044022039c2aa29b088dc98ee0dab4570e0ff9bb463e083eb0cd17d9734e9994acf346f02200eea1a9dc61c119c6bbcba09ba9c30857495d79ffac84877406c3c75160a3047ffffffff00");
        RedPunishTransaction tx = new RedPunishTransaction();
        RedPunishValidator validator = new RedPunishValidator();
        HeaderSignValidator signValidator = new HeaderSignValidator();

        try {
            tx.parse(bytes, 0);
            validator.validate(tx);
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }
}