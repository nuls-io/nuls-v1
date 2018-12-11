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
package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.account.util.AccountTool;
import io.nuls.core.tools.crypto.AESEncrypt;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.utils.SerializeUtils;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * @author: Charlie
 * @date: 2018/9/3
 */
public class AccountTest {

    /**
     * 获得一个，用错误的密码，解密加密后的私钥，得到一个错误的地址
     * 测试用错误的密码keystore导入keystore的问题
     */
    @Test
    public void decrypTest(){
        String acc = "NsdvWJqEtRcekQVf88UoKH4Y789ka9YD";
        String encryptedPrivateKey = "3254fad53298a1fbf1fcda27acdb4bcd1c895001a8443233d4d9ce9ca803c7e6dcd9bd32813668a9ad2687eb4e1173b0";

        while (true){
            String pwd = getPwd();
            try {
                byte[] priKey = AESEncrypt.decrypt(Hex.decode(encryptedPrivateKey), pwd);
                Account account = AccountTool.createAccount(Hex.encode(priKey));
                if(!acc.equals(account.getAddress().getBase58())){
                    System.out.println("======= 结果 ======");
                    System.out.println("加密的私钥：3254fad53298a1fbf1fcda27acdb4bcd1c895001a8443233d4d9ce9ca803c7e6dcd9bd32813668a9ad2687eb4e1173b0");
                    System.out.println("错误的密码：" + pwd);
                    System.out.println("解出错误的私钥：" + Hex.encode(priKey));
                    System.out.println();
                    System.out.println("正确的密码：nuls123456");
                    System.out.println("解出正确的私钥：" + Hex.encode(AESEncrypt.decrypt(Hex.decode(encryptedPrivateKey), "nuls123456")));
                    //break;
                }
            } catch (Exception e) {
                System.out.println("没解出来：" + pwd);
            }
        }

    }


    private String getPwd(){
        char[] word = {'a','b','c','d','e','f','g','h','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
                //,'A','B','C','D','E','F','G','H','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        char[] number = {'0','1','2','3','4','5','6','7','8','9'};

        Random rand = new Random();
        String pwd = "nuls";
        for(int i = 0; i<10;i++){
            int randNumber = rand.nextInt(word.length);
            if(i>3){
                randNumber = rand.nextInt(number.length);
                char num =number[randNumber];
                pwd += num;
                continue;
            }
           /* char w =word[randNumber];
            pwd += w;*/
        }
        return pwd;
    }

    //测试私钥
    @Test
    public void privateKeyTest(){
        for(int i=1; i<1000000000;i++){
            ECKey ecKey = new ECKey();
            String prikey = Hex.encode(ecKey.getPrivKeyBytes());
            ECKey ecKey2 = ECKey.fromPrivate(new BigInteger(1, Hex.decode(prikey)));
            ECKey ecKey3 = ECKey.fromPrivate(new BigInteger(Hex.decode(prikey)));
            if(!Arrays.equals(ecKey.getPubKey(), ecKey3.getPubKey())){
                //1.0.1的bug, 导入私钥BigInteger少传参数, 如果不等说明问题大了。
                System.out.println("error: " + prikey);
                Address address1 = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(ecKey.getPubKey()));
                Address address3 = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(ecKey3.getPubKey()));
                System.out.println("原始地址: " + address1.getBase58());
                System.out.println("导入后地址3: " + address3.getBase58());
                try {
                    System.out.println("原始ecKey: " + JSONUtils.obj2json(ecKey));
                    System.out.println("导入后ecKey3: " + JSONUtils.obj2json(ecKey3));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            if (!Arrays.equals(ecKey.getPubKey(), ecKey2.getPubKey())) {
                System.out.println("error: " + prikey);
                Address address1 = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(ecKey.getPubKey()));
                Address address2 = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(ecKey2.getPubKey()));
                System.out.println("原始地址: " + address1.getBase58());
                System.out.println("导入后地址: " + address2.getBase58());
                try {
                    System.out.println("原始ecKey: " + JSONUtils.obj2json(ecKey));
                    System.out.println("导入后ecKey: " + JSONUtils.obj2json(ecKey2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            } else {
                System.out.println(i + " ok: " + prikey);
                Address addr = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(ecKey.getPubKey()));
                if(addr.getBase58().endsWith("lichao")||addr.getBase58().endsWith("Charlie")){
                    System.out.println(" yeah yeah yeah: " + addr.getBase58());
                    System.out.println(" yeah yeah yeah: " + prikey);
                    break;
                }
            }
           /* try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }
}
