/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Niels Wang
 */
public class CreateAgentTest {

    @Test
    public void test() {
        List<String> agentAddressList = getAgentAddressList();
        List<String> packingAddressList = getPackingAddressList();
        for (int x = 0; x < agentAddressList.size(); x++) {
            String address = agentAddressList.get(x);
            String toAddress = packingAddressList.get(x);

            String param = "{\"agentAddress\": \"" + address + "\",\"packingAddress\": \"" + toAddress + "\", \"rewardAddress\": \"" + address + "\", \"commissionRate\": 10,\"deposit\": 2000000000000, \"password\": \"\"}";

            String url = "http://127.0.0.1:8001/api/consensus/agent";

            String res = post(url, param, "utf-8");

            System.out.println(res);
        }
    }

    public List<String> getAgentAddressList() {
        List<String> list = new ArrayList<>();

        list.add("2Cc2gqxcsJyM4tbJxqaHTj22HESAeXD");
        list.add("2CYfymtFuVCuYrQLzJ3gBvdZptvz4zj");
        list.add("2CZXYx5qszeCcqYXCwBbvkXZXiK1gk4");
        list.add("2CiHFDCksjv5aCJkz3fSLqtBLucjmMH");
        list.add("2CXRy7Utqpd1RNuhYEVkLYTcRv5JDLS");
        list.add("2CWdSPCo4t1PYHsRCMqs9Vfdy1hEYn5");
        list.add("2CXwBnL1TTqJ7KdjrEMvTsJoFQw6ui6");
        list.add("2CY6uMkQXDyzS8qYBBCwCRFNw2QMjLE");
        list.add("2CcLYrTCre75a2gzcToNYnYA8VgXmjX");
        list.add("2CZXSrhjHp9vKigCFv2SveS4bo1DHfc");
        list.add("2CdxbVs9j8DWKjEztvLafWo8myqqm1k");
        list.add("2CjQ8Mq6Es5gsDhkDbry4pwXXc5DcCW");
        list.add("2Cc8Q5rSFHcDSoQfUEqYcG5HSHRhfzm");
        list.add("2CfwtEqXuhLFWg2EtV6gxqdpmLpfoMJ");
        list.add("2CbJFyiDxkdmTVAtJFU5cqrSNcuw355");
        list.add("2CdKrRBh7bkTcj7TyLJpNS83zi1HKzr");
        list.add("2CirE7j1m2LxeweeH3EiZZZjHcGHCT4");
        list.add("2Cef5TNpVdzsAveDUXUZCT96KEvET5z");
        list.add("2CeH8wjuLk2fVdQD3Q4GyXXEEBxT3Sg");
        list.add("2CgA1Y795Yv8PSo7QhdSHUiXkGiofNs");
        list.add("2CaMJfVt7ouBfGkrAT3ud8pprUymUJb");
        list.add("2CXkbp52hTfx4YKFp2uuXUb2p6uSTc1");
        list.add("2CdD7nHtapywL8sdu43c72YpHaKHrKr");
        list.add("2CfvjcESYnkq8fZZFLtJXSjNa2ehUox");
        list.add("2Cb4oMyPSybkDyPXWsqnpGcoPoAZLmY");
        list.add("2CZ22WuJpByGXKE9Xhw3sXYFK45zg1X");
        list.add("2CbHbC2QEK87hddZAFcLXegwCg5Pq1J");
        list.add("2CWx9PZb4uVLAJX1xMjS4iAndYe3mcG");
        list.add("2Cbp16QYb7tzpHL9MvA1cQbPCkfSbP4");
        list.add("2CcD8oStg6cYCj9SWE9y39j2KNJoGr8");
        list.add("2CivRco72vWqHgPTn2meNuzScP2aQXL");
        list.add("2CiTdr35bjC35ERDFDM2tL6qBxKMAtz");
        list.add("2CV5QF5r5XDzj9WWdmHPJrc8xm5JYAB");
        list.add("2CW4fFepEVpp7DXy9G4jPFPvNMugL7Q");
        list.add("2CkBNLW8Y2X7vdX3hxFBHR3DqsFuWGU");
        list.add("2CfiBnL55wsphYjjPVJHqFiQH7M3oSQ");
        list.add("2CZuUGpZgvRzxzZ4TZNbrp9PrXs51Tn");
        list.add("2CiCmGsSsnfb8Kc8zHafmkG6588yx5u");
        list.add("2CYHLVWGLBxGkijXq2N8JnHRrTLysjQ");
        list.add("2CWWtH7uEA836Mvbd9VzMVZdRiRrdGy");
        list.add("2CeuVcXeibNjDjungWABCRzDa9qtcru");
        list.add("2Cg5j3A3ejCxYsmfUwXfVQePVhFhwHg");
        list.add("2CegCbx9jWCncSHK7M3Jeu4fWu5ZRs5");
        list.add("2CeasNhdfxvVkyCL7rKNK7rCFprUQmG");
        list.add("2Cay675epEHA8YFwYmPEgrQRAhMSj4h");
        list.add("2CiUZjo9yzgEDa81KSZaiGHqfD5UmLt");
        list.add("2CheTVzJ2L37h1w8MRvzafostSzJGsT");
        list.add("2CfkLhjEXkPGQtm3x6oU1wEnjpHYkPu");
        list.add("2Ch5ai5evu94E6Ew2SBiQWaYXAhuMUz");
        list.add("2CaCfVbPGhd2VAiiXTpLxUfJPajrxci");
        list.add("2ChgP29U9SKQFezCen4LELXery6bz1Y");
        list.add("2CkJPfPa2gogLTK5acgrRzEkPHjrDXG");
        list.add("2CZokSNjXUmTKY4yzvtxTQvSBM7iJFh");
        list.add("2CjgMy8zTJUzCWFPDgQR61xfDWBuZLF");
        list.add("2CcTfQgAbMJ6piTnYogNQCC9K1C6mgs");
        list.add("2CjEStoSAngTSdeR7VYbYy9Gj2eTQ9i");
        list.add("2CiB66gXTunGfpvApQavJBKM8oNWo48");
        list.add("2CfjoHuqH9SoUZRW7m19HLFjppaeuX4");
        list.add("2CWDPkrGKvqDY9GtW4GrSH5j8TAg5bB");
        list.add("2CcFe6QLTHLzQvYK1mKi3DqGmyjPXro");
        list.add("2CZ9ekWTuVY41ycJJdn2LDqdTpzNG6r");
        list.add("2Cdieo8MQJL7KMQ51hoSynDJfSR2tk5");
        list.add("2CWVZ9m1kbFKvcB2UwgQGJTGpWTGjVp");
        list.add("2CjLaiMjPmstrAZiV4WnxnPCt7UYAUy");
        list.add("2CdA5WvEmragTZKzmH8QW7WBNvKFis3");
        list.add("2Cg9wn7nuUH9WbKoKfTDsXvWsPHfhBu");
        list.add("2CXRbbbRumpyLbrC8SX96ugqK1WygFr");
        list.add("2CbM3M1KeA3nJHwrNJxvDjf1N3V6Mpm");
        list.add("2CYKSuDFiNrA387LAF526eUeUKw67vC");
        list.add("2CVAxGcxCZv6rwq2v7vUPpe931V3muf");
        list.add("2Cj61FNPY9B8wex1kW95RgApMS4MmTg");
        list.add("2CdPSMVbEnSHErVAxfstxmQctBHDaaQ");
        list.add("2Cc5u8feD9ifQErApAvj7aLaUeg4EQm");
        list.add("2CeEMobjcDf3irnapm2xqdSzPQg724z");
        list.add("2CcFx5iv17Tgymp33HP5fdZeMgL5nDD");
        list.add("2CfzQntRknL5brgKDnpiz1Ayp8zsLYC");
        list.add("2Cbr9JrJvCKZPFo2XPvoDa1aTm6pK81");
        list.add("2ChczYCL6LW3cRj8PtCgmJVoXCrzRJP");
        list.add("2Ce1EyCijdkSKHPSMRu7WXiw8E3Yuhp");
        list.add("2CeXu6QPpQYe9RTZSkkucVFCtMwNRss");
        list.add("2Cba9QaakwYX6eHtQVZgmobBQ7fuzpi");
        list.add("2CZJNsbzzBHsmHvy6MEgxJM4MLoATve");
        list.add("2Ca4f5TN2ZTZY1gWgnmcgwMqXcdgMb3");
        list.add("2Ci6FJsZVzjqN8hyiWpVLh3MbpMfi57");
        list.add("2Ce29AuYXgR1c7UALYxRAp8ZXLJuHU5");
        list.add("2CXzL7yuWFrGD3aJae9tGEE5Hgzy66C");
        list.add("2CfMavgXm2zumM7Jf9oNVdZT1erzvHY");
        list.add("2Cczq6oKSVcaFXkLzds2RM7cBNb5nW9");
        list.add("2CWi6SL7QZMuPEEsRpXrWLirsAbR4aC");
        list.add("2CkSBB7ns5Q3yFUUPfkoWcnAUwcDVwv");
        list.add("2CZPgN5VU81WbhZJrCBXtgT3PXMkUAi");
        list.add("2Cdt9yhbXpCDLuU2gQaQH5M5b3umM3a");
        list.add("2CjuDKiWy9QpaT4VLGY8rGAAGSuwpfv");
        list.add("2CYcaZYkgcpMKHX7TtLjpkyRotbs55R");
        list.add("2Cext6pmhnHoN41Hf3XuZQxmt1cPW8B");
        list.add("2CVyo48t6bqtktuTXYLG41vbn2ZdhbR");
        list.add("2CbzPf8PkZkTTrViAqXp598kfBt24id");
        list.add("2CaCVQjNQFuJCofmcqyPK6GnWJwjimK");
        list.add("2CgTcje5EePMoHsLkTsfVG3uy2hZ61v");
        list.add("2Cc4fBADknrbKLLK7Qkg35W6GH3ur7E");
        return list;
    }

    public List<String> getPackingAddressList() {
        List<String> list = new ArrayList<>();
        list.add("2Ck4ZpGavtaXtgdBx8NTTZyHpNGoWer");
        list.add("2CZYnwNfMba7inw22p74QSaWQ4QRfD5");
        list.add("2CcHvoKqmUUJB6Fi7ykWg1buzT7fKez");
        list.add("2CVtTnX17jw4gPGkCbkmdqom97JvRFW");
        list.add("2Cc22xksbES2PQko37tPyoxho8RUEnW");
        list.add("2Cj7JT1FW1HHnt8VFLutoWAmG9wWcZ7");
        list.add("2CZde4pbCQwLZ4jU5DseptXoJSPG3zc");
        list.add("2CffA9fq7yVFaovFH8jJh5toV5d3X5K");
        list.add("2CYF21KHdXBXVwQg8PJTbSNLjZpht7g");
        list.add("2CgyVBoRmgNquHkxJTi3vzFWfLto5c9");
        list.add("2CdAeu8Xn4v1NEra1CZsU3xwccDQXcY");
        list.add("2CezK5q6or6z6qoSZCKnARckSLwvWW5");
        list.add("2CbZupjHbjpWxAr6yu6Ruoqu3r4gYNa");
        list.add("2CgT4swe1XHEy3VaqqTCKZUNLM1FTuN");
        list.add("2CisF4qfWZVbZqvsb5xdwCb3z6ELfDB");
        list.add("2CkKdr8yaN1fwDiEti4ezScFbo1RhqV");
        list.add("2CiDg37ubkPWX2R8DRBpNnWpRgFSF4E");
        list.add("2CW4VJyzpw8ZwE8QdFDiH47NVSUj5UG");
        list.add("2CbEQ1eB3n16sBCp9tKddW1cAyWxvfH");
        list.add("2CaafwmH2MuTxjUaPbE92W3Uh3bxH6v");
        list.add("2CWoJCkqw6yGAYvAjsgWTEhS2GK9F5Z");
        list.add("2CbNvcKS3JUeWv6bdiWm9MhMp3be11d");
        list.add("2CXMFnaAHKVA2THQiqBxvj2MaYr9uaM");
        list.add("2CjJTpBs16jpfV9sR2NAgkT8XXzNPug");
        list.add("2Ca2Ke8FGWQHrnVP8H5BYi2XMgkJWD5");
        list.add("2CiHwohP7Fb9rgKPvJ546nEyBwh72Pw");
        list.add("2ChhfPDBAuXBQ5f2G5dRqZmwykvTgPL");
        list.add("2CbiWEkFcGyjRFW4VDNmAVt1J5TY315");
        list.add("2CiEHcrAygtoq7txuCZQ6c93NARz4oD");
        list.add("2CdoJ5TDQj2JTveNHwefiRSAvmqYfXt");
        list.add("2CY5YZpQkfBCpY3qjoAwL5tPmTs7id3");
        list.add("2ChwoeVtnrzJ7MhKeUVFoGb7kviizcN");
        list.add("2CaJPyNAwzW6PfabztHtjwTtYPLdF7A");
        list.add("2CbB7LbZL9F6ZBCfRZfZM9Xe92iDaEf");
        list.add("2Cg6yNqc4QUxorNRtnt2DW7zdfRL6Ef");
        list.add("2CeGcWE2iMhGy6MAMKvmoDAkbxiWsDb");
        list.add("2Ceh7WJmBwQEJmn86vr21fYPpBJe4Vk");
        list.add("2CfccNBRkWbFMHcWEBo6nkJjJ3o8L1h");
        list.add("2CbEPiz7xigbDQwgFD89t2yUTbPWxLR");
        list.add("2CfCUVXNBWzUvRFGSUmhMyr5ebopkue");
        list.add("2Cdcj2trBX7j7fLLBmHSMPX4qXonwPz");
        list.add("2CWeYLSWW5C3gawfYqc7Gd5KwJVyQbQ");
        list.add("2CaqGzmQVR6crzWjXRLJASRLE6GswzC");
        list.add("2CgJvCSoekj2CPADL1Um4Fdfdf5PJw2");
        list.add("2CeW8toTYsvpF8RWJASZXCiqUu5jyCD");
        list.add("2CVeBwK6s21ABdsstRcfB7NLBaZ1ipA");
        list.add("2Cdvo8LJxWYE7DEgB3R62D5Wf5hDkHH");
        list.add("2CWfKD2Lx56cUtAZWTv1XUosJwCTPeG");
        list.add("2CfsNtmZ7h5bgCKhManD8EVgLzATctT");
        list.add("2CVU8kut2DVtLX8UhCx8Bd9cdLTbs84");
        list.add("2CcPoF8VsSwkYHY45PKrXuviKyaXL3E");
        list.add("2CV71AkLjfTmGGbfRAdRks354mtagAt");
        list.add("2CYDL8DyTS6NSTRonXtgeMkcEkdtYLP");
        list.add("2CZKbvmEGjqhEKJHvtQUwpZqURNiVsy");
        list.add("2CdvmDyH8BZERtqFxyzBMYci6Lb1o2f");
        list.add("2CiXuVmydsN91XRFoXWb96MbQxN1r95");
        list.add("2CVYi8FG19yevLGMA4aWip66FXa1yPM");
        list.add("2CYtphwb563mkWRSqHasn6PdDA7quMF");
        list.add("2CXoVMETRuTr9AiHKNVPNTidw9zaKxq");
        list.add("2CWr2XPBMCjR2cn7fTfBTHMWCBDrLpX");
        list.add("2CiX199ak2wJ7KyAW7heBmShZdNTRci");
        list.add("2CXRjvKPUtHAHGUb4k87f7Q4MCmbGSk");
        list.add("2CeagHhjeQ3yvoj4hr9j8WLPi666PgQ");
        list.add("2ChFonnG83efYYtLUsfJDgF7nq6FX5b");
        list.add("2CgiJ4VPVzJpcdzaLU7cX9Tqg2X3WYS");
        list.add("2ChGvFBJB2QwHwViD64cxtE4p73ivBc");
        list.add("2CkZweBpnWYnoseyScLdiWc77RYTkCk");
        list.add("2CXnobsnFebExBZN63JmzMF8UNCp9HE");
        list.add("2CjziRzR8hWesf5z5TBahaJXvFWgbCH");
        list.add("2Ci6ZBtnXHNJrGAbWwHwgSWJWxrB98R");
        list.add("2CfvNnddxUz4kV7VtjYiwzfcJ3PCodo");
        list.add("2CaSYqYXADQqpMU5SkqFxpzFJeMSZcJ");
        list.add("2Cj1iAztSFGRTY4eX7pjQ6uk3gJhkzo");
        list.add("2CYrbFKfmuk5sihkGWg75GtYfrq8AtX");
        list.add("2ChpyEHB2eTU3Jhdq6jAWm1rKG5RNr2");
        list.add("2CVpkMQ7ewLbHLHqAqKcxLqt7Ba35rm");
        list.add("2CkJxvgxcaPPnPVgBFcewHdHdfVFpKn");
        list.add("2CVrEftP4XoyUWKFdZgMoA5JQhbm2yF");
        list.add("2ChgHHrW81zxiv4v96sNgYRMnr4rjXD");
        list.add("2Cbm57iGZUDCfh4uKc1Vpb9JPJVBNuy");
        list.add("2Cgp3SWYWoFefyPKniRLMwjCA5WMeHx");
        list.add("2CcJn7Bg9hknLzRp62BSzWzrcL95LWo");
        list.add("2CWnMRAKYUjd7r4vMMaDbVvFEcF7pz3");
        list.add("2ChmWVLCgzp5CB6mTRmy3vJV2KXwCQe");
        list.add("2CaiUxt4g8NZW6JTj7yzsZiGHFSjNYu");
        list.add("2Cf2HtK8ATiFQ5XZN8PvHvj1UXZBYPY");
        list.add("2CauS5Zc7M5hQuCeJXyhmMiTNnq77fB");
        list.add("2CgTPo8nYobx4T2sqQer9X4eeewwhxf");
        list.add("2CcjKN7bGr2mXzWTLhHXZEUtMU96ZA8");
        list.add("2CW4AiPggkRipHw3PHWig112LDEUGfC");
        list.add("2CVLibmSAx8cK3iwB3Uk8X7tJJJKhJF");
        list.add("2Ce94xXZ7sTKCrUNgQL3NN8F7NhdLsB");
        list.add("2CY8ZghBG7Q18rgmFxuPmw7Yf2mYyB6");
        list.add("2CenDwftmMXfFi2xf3ccydD7SVoG1tp");
        list.add("2CiYUrAGmd5S3PNSBt8ERJKPqZizZZd");
        list.add("2CeYeDfvfwBXL9PnLykdAW3K9ox7Nam");
        list.add("2CkTtW9n9ZQXVE5CLUyweVpkj4XuDTy");
        list.add("2CfwW5B2crpXx7GL6v35vybFN4XWMyH");
        list.add("2ChJhikdEQ1YPevk77Wxcfa79qUyU2y");
        return list;
    }

    public String post(String url, final String param, String encoding) {
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
                sb.append("");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
