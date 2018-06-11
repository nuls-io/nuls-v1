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
 * @date: 2018/6/9
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

        list.add("2CctQ8XM39gQ2mouopKsDCHn88c7tpD");
        list.add("2CjCJDAz8nF35jJnSoxDJywsTrEjJH4");
        list.add("2CeQMfJiqUBggBxZ11kHhpphDVmYN1Q");
        list.add("2Ceiom4NiGSvJRPa1p5i7knt5p7LZCW");
        list.add("2Ck8YZw9ACiVrt332zx5ajqZnySyYJd");
        list.add("2CizEBDY1vj2XeZBLrfrBEAeis8To7K");
        list.add("2CYZyS5cW6mJ6v4683tYPpSR1u6uo4u");
        list.add("2CYfVGi3DBU5xkVLX2N8kPzz5Qra1fh");
        list.add("2CVYTtK3Vxp9USnspXPct51XVe5v3rt");
        list.add("2Cfh9o6QNvDyWg6z3BuxYQCFfwrcNCs");
        list.add("2CZeUHESMsDsLmrzguhWYJ4jPo9BC6Z");
        list.add("2Cc7s5Yi955VQUkShW6r5ShBaeNpCLJ");
        list.add("2Cap3NibYDo2gKouRxQDjPYQ7jAcxzL");
        list.add("2CbNQHajzyRLEUQo6fPemLuvBTH8KQJ");
        list.add("2CbTHr9xAfpiCefHg4acq6KHzBkQnNb");
        list.add("2Cj11bw7UXmCuzJsmVmEzEPkghUARKw");
        list.add("2CW5u9wQ2rebiKxgC76KjkADFkauRoQ");
        list.add("2Cbw4NuMcFcZk5XDFApAZoX8WHyCsrA");
        list.add("2CjCiRYaVmUvUzhWqPfXC5dea4r42QX");
        list.add("2CZrHnYB9zz4p9duv7NwW9eX4hhmBGC");
        list.add("2Cisr69bruAJHJAhDrtCseRbFPEEjpx");
        list.add("2CcK1hGa49qviuKj2MqkDFQ9LNzj2UJ");
        list.add("2CdrLKXPiFfrQeiNDeSSzmmzkCTibxS");
        list.add("2CaJ4zpr5XZhRPGiokxjWia85R5JEFG");
        list.add("2CXEiWcAD4N4DP4X4rFwEKhVdSKTcPa");
        list.add("2CbN8EscBqpaVtZQSCErPrmn4oMonRY");
        list.add("2CZjofq4VwdW45qhjrVi4RQjwqdehbT");
        list.add("2CV91Vy7wX2os1KxndnZPJRcf89yvRJ");
        list.add("2CXD96kvzEUkLp6RxnPXnBmKDxzMBxV");
        list.add("2CYvtBo2VioGCJW9hoRSnLaZwuTL3Tv");
        list.add("2CWihk3mfvA1bDtCQQmBSGwTBdMNxXY");
        list.add("2CiejAG8pffMGCSUdgcZ4m9aATJnyYi");
        list.add("2Cd89a3XMqZRRDfNfsyfkJnoF8NnKbu");
        list.add("2CfZhnjp8Y7iHFMNr6cnzwAf2NL22hc");
        list.add("2Cf7y6x2dFUKHFnATWCGogV4zuqSLxC");
        list.add("2CggHBMeX7RFxk9BAHo5LXhw6mvN6ZR");
        list.add("2Cdwm6ms7qaTFwHMqhmwpWWoWqFpJMN");
        list.add("2CWUB7ogF8dUi4Crs1CEn5GpsYGk5rt");
        list.add("2CkeT7Lbyf8PUjfhHGaBLV3zBN1hFtt");
        list.add("2CbqaF5i9vjN4dXvcsT8XNf7hVnzNaF");
        list.add("2CYvZK56SSvabxFtjMtuBbk8fPRR9BM");
        list.add("2CfyeJcdaz5TS42UVm6ibiQqENeSt4m");
        list.add("2CZEMFWYnBzsEJcaxA2KqUndNB1MkgB");
        list.add("2ChsRGhX7bLr1LXsexxJzQfjdvNG5Xi");
        list.add("2Cb1y1KHeY3rczHcsTiitwWQMMm4UKt");
        list.add("2CkHq3Nky2Suc97UBSDzeLCXDush1ms");
        list.add("2CkdfDmShKtuRZSGUFkzGL8Mo1cGvmw");
        list.add("2CjjT4JZAfbKqx3Lkpw64zyAopeokCA");
        list.add("2CWFg9PFeHifDY8sN99MpTvr7VuaKqe");
        list.add("2CkdUc4415VpdUVoYc9WEuMdamahx7e");
        list.add("2CgZq27V4puG3U2ENizLPWZSon8e9it");
        list.add("2CavN7G9nf5eQEsSUjpJoK2dimhQGVf");
        list.add("2Cju4HED8MELoWURunAQDEaj4JXL2q7");
        list.add("2CfDSyo7AjMsYvvyUA9e6gDbgWoVCBD");
        list.add("2Ck2NvDkmbiHrvE2vR1DPJgEbjNUvED");
        list.add("2CgTF6m9MasTdJomoBwfdDuHxYtpaji");
        list.add("2CdxCYysDSWdbF1bx6toMzZoaNnWS9m");
        list.add("2CjFiBTF2V9GEXQz5gv9s4XFRQJqJHe");
        list.add("2CecqMkHSWod6KY8aoF4xAvBvfF4hwQ");
        list.add("2CdouMobict2bdfzyuu3GFnUsrMipuu");
        list.add("2CcqiwTJsHKXeY6KFutjytGyabcnvuQ");
        list.add("2Cjw82SnoAeq8Qk7grsVMZuTS8AdRrz");
        list.add("2ChXBKPhGdcwjGfVpUKjYWYUFDGbeHW");
        list.add("2Cjw3qxMqHMraDLKAP3YCVGPtPU2wUa");
        list.add("2CdTRMAg1frtdFKUc1U3N6ipUwJgue1");
        list.add("2Ca9C75PvPQkZbPEdZnfjp8XR1HF6E2");
        list.add("2CXhT42q9oibS2cw5iSsotWaXhbXHdp");
        list.add("2CiLCk6fAWmmY9bg4XdF8MAJMGPuPjY");
        list.add("2CcukhJCGjjs4sMuHwzHh3AkkEcss2c");
        list.add("2CjQMfd37t3qFjowapHSmcTUk5rxrSi");
        list.add("2CYhPTiLCHpHjyFYZ9wrf2HwgQ98bUT");
        list.add("2Cgw5Xs5Z9SFQo4Pxn5aj1GVXaKrHFT");
        list.add("2CYwCqbtoqgPS9Pn93qZdn3AgSkeERe");
        list.add("2Ch5eeyrB2hbqRWhmN849f5iVuEphbm");
        list.add("2CVXTDwmGTLn1v6t6cEPCm2Tp8dYW62");
        list.add("2CjSd9BEy5bjkgZCP8aiTBXLc7Bkyi7");
        list.add("2CjaV2X8kMXMq5CHQraMSmJ8x6b6hXY");
        list.add("2CUzZHQCGE7aiPCaD1n54xJkC9BrdKh");
        list.add("2CfAry1qVbtwtGucSDJTHfr35EpmUuy");
        list.add("2CgbPidjuNuPPDU4ZoALTCEegRHFG84");
        list.add("2Cc12jfe6gRNGSbFdUCjaJassgpPuwJ");
        list.add("2CatCdtqaeBfADyMGKkbb2hr3HpfFtY");
        list.add("2ChmSpB1gjppQPfkq44T5QsefUDMJSz");
        list.add("2CfquoNg9nfDh74GcioDXsvPbtTjCLv");
        list.add("2CX7j1Zrx7htWgXenD9k3EakNyMHEuD");
        list.add("2CfYshHRZSHuK7hypmHPL2qKpJ2ZaKX");
        list.add("2CZRpRPsP2cWY1CLjBgjAQqneD8W8Rf");
        list.add("2CZnsiR2q7yZw2dYVmktwMZaVW5E6qk");
        list.add("2Cc54TepK6JvuXx9McyFxsdULMbLrW1");
        list.add("2Chni3ha4J7kP6AX9x73r88r4SGwXnZ");
        list.add("2CYtxJPuzybSfbh1rvnKWex5CGJ4u7v");
        list.add("2Ca7f7aB9AVqsSTUWannPcgoTjA1ZpB");
        list.add("2CaVYGJeWSLEgbVPCEYBjroyv61HcCw");
        list.add("2CXUy4fDcC3mrM3u2fvHVQraCcL62BQ");
        list.add("2CZ4vSNrkJynMrT5ccejVEgv9o1njoN");
        list.add("2CXyVt1FXCZMeYHFNMFmjgS85vsZzRf");
        list.add("2CbhPAK5CJBK3YT1fTeJQATr9d8mAeZ");
        list.add("2CVsx51dNjfwKfpdFL44kJsAGTwEqki");
        list.add("2CZ8yRoVjzzU1ntEVDmXJhLD2dRmNMF");
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
