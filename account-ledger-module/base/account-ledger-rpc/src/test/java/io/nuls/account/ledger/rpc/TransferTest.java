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

package io.nuls.account.ledger.rpc;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransferTest {

    public static List<String> getAddressList() {
        List<String> list = new ArrayList<>();
        list.add("6HgX3Fd79sCDaJvupEV6EFHsN5bQ6XLN");
        list.add("6Hgi5kA9LwKWz9UJZXPHYNG7rJfvETDu");
        list.add("6HggWcxqWwwcoWY7K2AgkB51box63Tpn");
        list.add("6HgVVbMC8v1hWEmbiCc9uYsLNth2ojHE");
        list.add("6Hgbq6pr1NdsEixtwEnaYkeikeYwHxGq");
        list.add("6HgWAdwFJy2ShCZKhciby5FoojUdPVRn");
        list.add("6HggivpyLtNGSxLri6YviV9e22N7oR36");
        list.add("6HgY5nagiMw3FDTdpahQCwE1K6tKBuKL");
        list.add("6HgcAzFK8dyp2v8fS7XWf96zJg6URYWQ");
        list.add("6HgjSRnivTAZETv4JiBEuSA7vE4PgKqg");
        list.add("6HgVV9ZmAJCDrWex4z9WbLMXJYzKB5Sm");
        list.add("6Hgc4gdvuZARocnF3zx1at8oJvQFzZr7");
        list.add("6HgWFd96oM6tmtSi7G57c8K9sQy4Azpa");
        list.add("6Hgj9jjRHS8cFqgEj7oXGKBxPJQ1xk16");
        list.add("6HgefCXz8HW4Bm9yBepcNPzNHU6veM7P");
        list.add("6HgVoUgPxBzYhVzXLv5oPmZ5FmJ9wPw9");
        list.add("6Hgf7x5qtRJGnoEB1PPjTpATZTEKjTry");
        list.add("6HgbzuLCxpjSPUpekdyky5HkuyxYNcrc");
        list.add("6HgbvWdMkkPZNqEZHky7M69utW2Wjms6");
        list.add("6HgfcNa119Ls8jsv88K7AJvT1hTi7DRT");
        list.add("6HghP9Ack7unAPXsNhiVj9hLF93xG9i9");
        list.add("6HgdkrTN7S38qqRiS3z7N3X3p7dmfzFj");
        list.add("6HgWb5FzYoZHqsDAJT6NHRcJgQtcq3h1");
        list.add("6HgUe9GbVkQCszjzGfsLaNZdtE7fNQTp");
        list.add("6HgWQnUnKGR9iWGcZ26bmGUh2jVpgURF");
        list.add("6HgcnTeKE9RuwsGjWvdyxPVWnwS3xaG7");
        list.add("6HgYSXSnyzYnqisfGvkFfoUsGaGRyqRw");
        list.add("6HgXYf6kHNFpRuKuuNPAxgi1SuDD5bRr");
        list.add("6HgWrSk2CAVGb3mWABWnocLGeaVExCqq");
        list.add("6HgUpezdideyJTkYJjyCbp24YJsjJHoY");
        list.add("6Hgj2DQmwDJHNHD2Ds7kVFDpjUNzNEve");
        list.add("6Hgc2UwBbpGkmoytaMZkW6J7LqyK421N");
        list.add("6HgfB3qjp7R4nbg9qqoQcRFWdbBVXCME");
        list.add("6HgZNgbGjG2Rvj7s8EyDgKN5KWz2xHQH");
        list.add("6Hgeow18ytAFJy2muBNzRmmKU1kcz9X5");
        list.add("6Hgb2ifKzoWWa4FJooqLh44naqDUUvAh");
        list.add("6HgWuSfFY4saXiAnG1kxWCt5YFu63m8w");
        list.add("6HgYP66G59YS7hjz9m2xNtmEaQiXffop");
        list.add("6HgfUu3B6dcgd9W2tPmxudQQxhdoZX3g");
        list.add("6HgdHnLpSKj7bjuzpUycajAAZzwviH9w");
        list.add("6HgiKfDdxZwSdEVkMMdHzURK83C9x4pn");
        list.add("6HggjPbWd39SKkih38sawBWQZVeABSWN");
        list.add("6HgVDq8y3h6Rb1DMQPf5fFQj3ovxHcvT");
        list.add("6Hgc5R2fPuyAJw4MHNUBXHP7QEC6Fa34");
        list.add("6HgUiacni4ePDxtji7UqjVz3RJZxYLxc");
        list.add("6HgZHo7ZD5L8Ak6knaxyAHgWM1YdGd72");
        list.add("6Hge5gh652dqoYv7wQSBoBDEzSGKWsL9");
        list.add("6HgbaMgEVejwnnoVJi49CzYxqd3dvav3");
        list.add("6HgiNUfrxvevV8NVjdVdCavvpLpSTezF");
        list.add("6HgdTYCCXLCMkKXqfZLLjPbPgV5syo2z");
        list.add("6HgeLGrLeZKitz27T5iBBwwR4ZpCV7rJ");
        list.add("6HgUuqtwFn2sAHhA55h8qDTVUjEsYu4Z");
        list.add("6HgajSAbiaiNDpzwus7DTTH9uVUoNL3u");
        list.add("6HgfVvJCeb5S31PhjiT2gre2mqk9LuPw");
        list.add("6HgV4SqQ7Xycigk8rYNwwBbawJMZysey");
        list.add("6HgdanPAeW4JvXPc126JDnH8ozn58g4o");
        list.add("6HghTfTjeBBuqy1969wDyo73Ko32QvSp");
        list.add("6HgjWjTvxrabjSbTjxrTXTfBBiUsQWLC");
        list.add("6HgUnFJuqCK6xHL4LC1VF9x1vDFEQuuX");
        list.add("6HgVwLudq4GCWRM9yRPg3SgLsZVWhNxG");
        list.add("6HggGZwWwU1rwbWVB2JQSkxBueL53giD");
        list.add("6Hgcb74GEoSCFLxRY8S2bEVtGVatRuxZ");
        list.add("6HgdXtwfSNhjYv5WSjPqCMqZGbbA4aLa");
        list.add("6Hgery6Yrc6nC2UE6dP2SDjQePak5gUQ");
        list.add("6Hgdoh1dRpbWzd6Xq1krV4QCENHQ2cLb");
        list.add("6Hgak1PY2APGYKQ5BmRogijmevzqG8J6");
        list.add("6HgePEXgpuVXRYSBhyh1iC6J1P9NXDtR");
        list.add("6HgiRdeAtSrNG5b9ksvmgFNfhJA4z5uu");
        list.add("6HgXkcye6Ewv52mzPR36T5aMvu17ZY58");
        list.add("6Hgfcsp1udjBDVRS2VB91gPyodx2qxQy");
        list.add("6HgfVke7AvxarBb41N17bptqUtNBtkzM");
        list.add("6HgUoLUuWXzbCxB3urZFHayfHL4r63ky");
        list.add("6Hga9Af5rrVDJmCud1hSS4bzPT94MnpQ");
        list.add("6HgUcZTuSVqoQnHnkmYHriaBjkixNBGi");
        list.add("6Hgd64jFrBmr4kiaPfJmRNryri2F3viC");
        list.add("6HgfZTfmwRWggk3qGVFWMn7EnffLtLsz");
        list.add("6HgW5mDc7EWqpMmC2hswjZMfCJKx3QKi");
        list.add("6HgWB6YfZUMbkXCwPLYUMvZdGUQx1sJS");
        list.add("6Hgczkan5vsABbDvnYWaQ28t3bs1G6eg");
        list.add("6HgUGsZHepbWbKzmiDJbAT54CHPwmt4g");
        list.add("6HgbqzKVoxGrjQUtdrka9MUhVAzdNPbm");
        list.add("6HgguLZHBqWXngcy5jcmXGqa66NxXTaJ");
        list.add("6HgZpEwonaRzW5Le8BqYfXB8DovHPPNX");
        list.add("6HggZCL6aY1C6WchUBXf9zPsmpuqPPc2");
        list.add("6HgXddoBKijZrDSF2HJ9krUQyfXwYTCT");
        list.add("6HgUdtuKYg4BMJLtMFL8NPm4D2fFJ618");
        list.add("6HgZJ1Hq2b2krGtdsEuyz2mFPVJRsg88");
        list.add("6HgVAgo7WiJ9xUu9EFU7sJP75jF9Mdob");
        list.add("6HgfGiAkWdhgcoHpSEqJa3qLmcSWSHrA");
        list.add("6HggaxavTGhwfWAXxhhTiiKs4iDEEuJF");
        list.add("6HgVtWcF3hdiGbsnSYEPG643USfXLpNQ");
        list.add("6HgfsTsjWWCzkVn1aPGRQ4YDkZdPrUhC");
        list.add("6HgWaXgnbKnYxhxNmvf6Udsmo6Aa5j3d");
        list.add("6HgbTEKAHQPZQJWEsDa756ESwnzqMfME");
        list.add("6HgizKUiqrpTTcQ2Cy69wJjjM7WoraGB");
        list.add("6HgWuBC2YNbtwftiXFRm5R7Tf6fsHtsz");
        list.add("6HghUTWPaDaK6dzEcvgQaCWtex2cCWcE");
        list.add("6HgaFrs3GQGmvunZXnZ5T73JZspx2ZH8");
        list.add("6HgW8xwZj9xRBiqsPiR1R5TxCPgHfGWZ");
        list.add("6Hgfp6XQvb9chxZKm8fAdo6VsqPivk7n");
        return list;
    }

    private static int successCount = 0;

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            doit();
        }
    }

    private static void doit() {
        List<String> addressList = getAddressList();

        for (String toAddress : addressList) {
            String address = "6HgjYMAN7H27KegtMLpR4rdQJ3q8cNmm";
//            String toAddress = "2Cg7BLHWBSxMhq3FpjR9BrkyxXp4m4j";
            long amount = 201800L;
            String password = "";
            String remark = "test";

            String param = "{\"address\": \"" + address + "\", \"toAddress\": \"" + toAddress + "\", \"password\": \"" + password + "\", \"amount\": \"" + amount + "\", \"remark\": \"" + remark + "\"}";

            String url = "http://127.0.0.1:8001/api/accountledger/transfer";


            for (int i = 0; i < 100; i++) {
                String res = post(url, param, "utf-8");
                if (res.indexOf("true") != -1) {
                    successCount++;
                }
                System.out.println(successCount + "  " + res);
            }
        }

    }

    public static String post(String url, final String param, String encoding) {
        StringBuffer sb = new StringBuffer();
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        // 默认编码UTF-8
        if (StringUtils.isNull(encoding)) {
            encoding = "UTF-8";
        }
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            connection.connect();

            os = connection.getOutputStream();
            os.write(param.getBytes(encoding));
            os.flush();
            is = connection.getInputStream();
            isr = new InputStreamReader(is, encoding);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return sb.toString();
    }
}
