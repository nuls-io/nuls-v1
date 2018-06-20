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

package io.nuls.kernel.utils;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.RpcClientResult;
import jline.console.ConsoleReader;

import java.io.IOException;

/**
 * @author: Charlie
 * @date: 2018/6/2
 */
public class CommandHelper {

    public static boolean checkArgsIsNull(String... args) {
        for (String arg : args) {
            if (arg == null || arg.trim().length() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取用户的新密码 必填
     * @return
     */
    public static String getNewPwd() {
        System.out.print("Please enter the new password(8-20 characters, the combination of letters and numbers).\nEnter your new password:");
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String pwd = null;
            do {
                pwd = reader.readLine('*');
                if (!StringUtils.validPassword(pwd)) {
                    System.out.print("The password is invalid, (8-20 characters, the combination of letters and numbers) .\nReenter the new password: ");
                }
            } while (!StringUtils.validPassword(pwd));
            return pwd;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (!reader.delete()) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 确认新密码
     * @param newPwd
     */
    public static void confirmPwd(String newPwd) {
        System.out.print("Please confirm new password:");
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String confirmed = null;
            do {
                confirmed = reader.readLine('*');
                if (!newPwd.equals(confirmed)) {
                    System.out.print("Password confirmation doesn't match the password.\nConfirm new password: ");
                }
            } while (!newPwd.equals(confirmed));
        } catch (IOException e) {

        } finally {
            try {
                if (!reader.delete()) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 得到用户输入的密码,必须输入
     * 提示信息为默认
     * @return
     */
    public static String getPwd() {
        return getPwd(null);
    }

    /**
     * 得到用户输入的密码,必须输入
     * @param prompt 提示信息
     * @return
     */
    public static String getPwd(String prompt) {
        if(StringUtils.isBlank(prompt)){
            prompt = "Please enter the password.\nEnter your password:";
        }
        System.out.print(prompt);
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String npwd = null;
            do {
                npwd = reader.readLine('*');
                if ("".equals(npwd)) {
                    System.out.print("The password is required.\nEnter your password:");
                }
            } while ("".equals(npwd));
            return npwd;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (!reader.delete()) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 得到用户输入的密码,允许不输入
     * @param prompt
     * @return
     */
    public static String getPwdOptional(String prompt) {
        if(StringUtils.isBlank(prompt)){
            prompt = "Please enter the password (password is between 8 and 20 inclusive of numbers and letters), " +
                    "If you do not want to set a password, return directly.\nEnter your password:";
        }
        System.out.print(prompt);
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String npwd = null;
            do {
                npwd = reader.readLine('*');
                if (!"".equals(npwd) && !StringUtils.validPassword(npwd)) {
                    System.out.print("Password invalid, password is between 8 and 20 inclusive of numbers and letters.\nEnter your password:");
                }
            } while (!"".equals(npwd) && !StringUtils.validPassword(npwd));
            return npwd;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (!reader.delete()) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  得到用户输入的密码,允许不输入
     *  提示信息为默认
     * @return
     */
    public static String getPwdOptional() {
        return getPwdOptional(null);
    }


    public static Long getLongAmount(String arg) {
        Na na = null;
        try {
            na = Na.parseNuls(arg);
            return na.getValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static String naToNuls(Object object) {
        if (null == object) {
            return null;
        }
        Long na = null;
        if (object instanceof Long) {
            na = (Long) object;
        } else if (object instanceof Integer) {
            na = ((Integer) object).longValue();
        } else {
            return null;
        }
        return (Na.valueOf(na)).toText();
    }

    public static String txTypeExplain(Integer type) {
        if (null == type) {
            return null;
        }
        switch (type) {
            case 1:
                return "coinbase";
            case 2:
                return "transfer";
            case 3:
                return "account_alias";
            case 4:
                return "register_agent";
            case 5:
                return "join_consensus";
            case 6:
                return "cancel_deposit";
            case 7:
                return "yellow_punish";
            case 8:
                return "red_punish";
            case 9:
                return "stop_agent";
            default:
                return null;
        }
    }

    public static String consensusExplain(Integer status) {
        if (null == status) {
            return null;
        }
        switch (status) {
            case 0:
                return "unconsensus";
            case 1:
                return "consensus";

            default:
                return null;
        }
    }

    public static String statusConfirmExplain(Integer status) {
        if (null == status) {
            return null;
        }
        switch (status) {
            case 0:
                return "confirm";
            case 1:
                return "unConfirm";

            default:
                return null;
        }
    }


    /**
     * 根据账户获取密码
     * 1.如果账户有密码, 则让用户输入密码
     * 2.如果账户没有设置密码, 直接返回
     *
     * @param address
     * @param restFul
     * @return RpcClientResult
     */
    public static RpcClientResult getPassword(String address, RestFulUtils restFul) {
       return getPassword(address, restFul, null);
    }

    /**
     * 根据账户获取密码
     * 1.如果账户有密码, 则让用户输入密码
     * 2.如果账户没有设置密码, 直接返回
     *
     * @param address
     * @param restFul
     * @param prompt 自定义提示
     * @return RpcClientResult
     */
    public static RpcClientResult getPassword(String address, RestFulUtils restFul, String prompt) {
        if (StringUtils.isBlank(address)) {
            return RpcClientResult.getFailed("address is wrong");
        }
        RpcClientResult result = restFul.get("/account/encrypted/" + address, null);
        if (result.isSuccess()) {
            String pwd = getPwd(prompt);
            RpcClientResult rpcClientResult = new RpcClientResult();
            rpcClientResult.setSuccess(true);
            rpcClientResult.setData(pwd);
            return rpcClientResult;
        }
        return result;

    }
}
