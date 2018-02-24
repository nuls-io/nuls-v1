/**
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
 */
package io.nuls.core.utils.network;

import io.nuls.core.utils.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vivi
 * @date 2017/11/22.
 */
public class IpUtil {
    private static final Pattern pattern = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");

    public static Set<String> getIps() {
        Set<String> ips = new HashSet<>();
        try {
            ips.add(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
        }
        return ips;
    }

    /**
     * lower is server
     * @param localIP
     * @param remoteIP
     * @return
     */
    public static boolean judgeIsLocalServer(String localIP, String remoteIP) {
        long local = ipToLong(localIP);
        long remote = ipToLong(remoteIP);
        if (local < remote)
            return true;
        return false;
    }

    public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3 - i]);
            //left shifting 24,16,8,0 and bitwise OR
            //1. 192 << 24
            //1. 168 << 16
            //1. 1   << 8
            //1. 2   << 0
            result |= ip << (i * 8);
        }
        return result;
    }
}