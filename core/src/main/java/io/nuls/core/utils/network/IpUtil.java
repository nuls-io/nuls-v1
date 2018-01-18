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

        String chinaz = "http://ip.chinaz.com";

        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read + "\r\n");
            }
        } catch (Exception e) {
            Log.error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }


        Matcher m = pattern.matcher(inputLine.toString());
        if (m.find()) {
            ips.add(m.group(1));
        }
        return ips;
    }
}
