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
        list.add("2ChUb6GoinPEHHgrp3aa4T9eSArmnav");
        list.add("2CaGRJmocDMNZBhb713F7beTLNR8yRN");
        list.add("2CW9PJn8p4RNetWgXEgTadK4YsnhuAi");
        list.add("2CfU7ipWUtVrFwJqkxSmaPsb88zM8GV");
        list.add("2CehCrWoQdnPV877ZoE9xkmkhJNE7g4");
        list.add("2CddiAbv2Cy4m1L57U2UPuZSM7W1dZL");
        list.add("2CanEH1yffdjYTVr4uRasU87A16uhrv");
        list.add("2CbVYmXXbvN3rXotk2NDx8baTuXwpPB");
        list.add("2Cf4WRC7bH2qDj1srYrLA6KQceA7sNp");
        list.add("2CgqQiNRgLvLiBh6iGTHxUxjY6HVpnz");
        list.add("2CdVRrudpZpUNUYpmKMsKyVpgwaog5n");
        list.add("2ChFdrJECPA6dtxTJnzXsBhT2dfsYJJ");
        list.add("2CVPiWns6C7MozhVZLLZHxv7Sw6xcbb");
        list.add("2CZko8ghksL24WVSnrjCYTdtp1WuYUR");
        list.add("2CZV3WheLcQTeTdxrJ3WuyhPMytcZAS");
        list.add("2CbqwyheSMPUNcyfJWKxYNcR6d8rXDz");
        list.add("2CYzadXqKRojukR9zju9iGetU8pxLhB");
        list.add("2CakRPp9SkcBnbcx4zgdFNPMgQdmBX8");
        list.add("2CYykxsxk83385x8JscdvGZdN1pV8Tt");
        list.add("2CY5NWS93dJn9UjGvy4S56fCyca2HPW");
        list.add("2CfG73rvdRo7mDuoyYDHWNkLSbnEhMM");
        list.add("2CYquTPN9AwTEQQ6BwrdWk3mP2rsr2m");
        list.add("2CVDyez3uPGf6PFvXy31AAK7DkAFKz4");
        list.add("2CYe7CKe3koHK2BHKjvFSoJqHqDyG8b");
        list.add("2CasMm61zEqGr2WeJpWwR8YfWkR84of");
        list.add("2CYYYUnwNnf4Y56ZAc6q8Fe3kJ7q2YW");
        list.add("2Cgyczyza2DkBzykAbzn2gPktg5zMd6");
        list.add("2CgVQ1W8djwNTb6UL6JVHqamhNPkJXr");
        list.add("2CasDSdtu8QXnvXgSo4Y8VfoFGxLeKy");
        list.add("2CZvQTWEAsSbW6fQVAUoe1a9jtJqVGB");
        list.add("2CiGECMH422PhjzM75tq92zupjx9T95");
        list.add("2CZNTvHLmaVehCEVPWLJKXbtgYZM357");
        list.add("2CWEmPDyiSjNsDkFwG3io1ZjVpJHgcq");
        list.add("2Cbuw56XfBUre2GHrf3f5xhiTN13Xnh");
        list.add("2Cj2DaG62HADmLLb3XX1X71T2XXxgBb");
        list.add("2CbASfNjpm1TLzmbu2MXExCLDsBKxeJ");
        list.add("2CgVHbV3E18Us8W7q79kFJsEs8xUYQa");
        list.add("2CjSszy9HNAZfrvtWef5Yo2szn2NL7t");
        list.add("2CVvMQmCu5tbXAFUhdyuSnVas3oDZMh");
        list.add("2CciddkbWwPcwydJHLV8Ubmangz3Arc");
        list.add("2CcKkbNzrW7Swhor7AUFhhq3JD6NHrq");
        list.add("2CfavZXvxUym1NWbRYPn1Q3EvenXTnc");
        list.add("2ChtXTDCKTgnqpCtWRxVqqm3WAw4chb");
        list.add("2CWHA4xR7CL8WTKdMiguGAAhftpNeGU");
        list.add("2CgSQzvwgYeNdexbpRHYvGwnDV1w4Re");
        list.add("2Cj26vtvSuvrNBksasznWArwmgT1rLU");
        list.add("2Ch2Z9jFLmmtrKoiW9vm6nJWvgSQHD4");
        list.add("2CfxXeR3oPSz9YU5KR23G3mvPFkShFA");
        list.add("2Ck9Z28WnqHJR8yDsm9qyDwGXLUdXMF");
        list.add("2CXCXVe46QohHyRG9pHjRdz84wEqfYy");
        list.add("2CVmAtiwD4paYESJVpnhhMGArv75Z2H");
        list.add("2ChtTRVEZ7SXycxg4F2QEWs5ijSyVxG");
        list.add("2CfX8asXYuiPHegmdzhuMrwEtiaXCnv");
        list.add("2Ca169EtAX5VA6zkuLiJtuAH6pPpVk6");
        list.add("2CkNkFqdrGo9sihbHiGrsyRgmvSjWpB");
        list.add("2ChhEG9cFX2rqoQoha6fPCW4tgsYsKT");
        list.add("2CWDmBWAP4VS7sNU6iVFqtoaCv7sHvf");
        list.add("2CeghGyZzKVa5MpAgXWsoPMrQjrLUVL");
        list.add("2CcghWKMVqwUVjh4dtyXuNkrowfpXFN");
        list.add("2CVU3mKwwABCwTkqjGCw2jeenWCdCZL");
        list.add("2ChwvtiZZ6Xbnxhfs1y6nCDhYvtfXHC");
        list.add("2CcKRNTrWddivzV6XoH1kwbTVHpAsLM");
        list.add("2CamFy15wPb5q3JEgWFCbjXRdM8UKUC");
        list.add("2CeJMUURNRGqXfbuiU9VZ4LuwgDLRXJ");
        list.add("2CfeqC6rzyzaJeUVH7v2VbKq2uZfUtE");
        list.add("2CbcGdwLLYaKznxa1iN2i8EpUzR7KeW");
        list.add("2CZSws6iw7DdjeJTqn7q7HmYivFwoTs");
        list.add("2CWTd4Ja29ZFTMLtXcxGUodjgFEdyDe");
        list.add("2CXPeZLyND2pwnGAXLph42XHnArNmUQ");
        list.add("2CUvnz6QLHqw37o9hSPe6Atgu84NQqG");
        list.add("2Cc7RwY2hAxN1Ng4Zw4LPHz519dKTAS");
        list.add("2CWZTRULSaJnGV6W6BAkgX294FczRLw");
        list.add("2CiApJR2v7JF4kxzz55zLcbwocqg45M");
        list.add("2CUws5DD8y5mZJhGyjRC4cJ2hnvh3rw");
        list.add("2CYPHniW8mvGjNU72a4Xc68bosSsR2E");
        list.add("2CcPagRspDk5q7JXbyCidmuPH1uJiwU");
        list.add("2Ch4e4ECfFg92S98SEHzRPqkzf2BRxT");
        list.add("2CftG1fFPRoNRTu2a1MirEdtFMgWF9Z");
        list.add("2CZskGzCHDcGPGeLZ4KoxyBJGhp1L1Y");
        list.add("2CjFAKZkGiQiMVm7QBZMmRXBQXC7LBg");
        list.add("2CZHyWUwedFvgZNX6co9vhSxfmQKZuC");
        list.add("2CYRFuDHPBJfmdspDwsPNawMD82DthN");
        list.add("2CWtjYDJoR2T2qcaAhobamjsJM4hJsE");
        list.add("2CioGFfZDwazpQFjyWpieAFEQBFn8T2");
        list.add("2Ch1pcK3exeTToKYxcbfWDPTnXfpCE7");
        list.add("2CbyJ9QpkUANrhp7njACtDfktVR8Dff");
        list.add("2Ch7XMXM45ByEpgmLT8r93BycTTXJ4n");
        list.add("2CbUfFzp16pmqQs2FeRDVQCna6qmgbV");
        list.add("2CWPybf3g8ijpgeHW3TnCbLFJDchazE");
        list.add("2ChUUKu8h6uzB6GEYukzAVSwRpWbsjV");
        list.add("2CfGgd1NXGSmASPkpnvVN32Gco6z6nk");
        list.add("2CYjunQY34o7vndWVJAWNoVuhPvDEWZ");
        list.add("2CivDE2mfhcCEwUMjwqnEKfRxNABtY6");
        list.add("2CWy9ZEg8AqjheUjh1YccsexLB2sRsi");
        list.add("2ChLB19K2xR1LT6JqZLke4BPiTAHMcs");
        list.add("2Cb2QHPRjALQyuVQUY56ReFzhoxQjof");
        list.add("2CiPDAyuR8DcBfoaRZWHkcy4gwYVBc8");
        list.add("2ChEVWWQuZ9Jtxa9F6TqUqKUTwysVz8");
        list.add("2CdntCQwnxixsKsHw1mDSBnB1tcDcuX");
        list.add("2CZUEPREZ8eiEXrh6zS8J1LR2vvznkw");
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
