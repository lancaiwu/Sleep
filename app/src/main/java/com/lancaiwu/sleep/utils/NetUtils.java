package com.lancaiwu.sleep.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lancaiwu on 2019/1/23.
 */

public class NetUtils {
    private static String TIME_URL = "http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp";

    public static String getNetTime() {
        try {
            String address = TIME_URL;
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setUseCaches(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.7 Safari/537.36"); //设置浏览器ua 保证不出现503
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                // 将流转化为字符串
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String tmpString = "";
                StringBuilder retJSON = new StringBuilder();
                while ((tmpString = reader.readLine()) != null) {
                    retJSON.append(tmpString).append("\n");
                }

                JSONObject jsonObject = new JSONObject(retJSON.toString());
                String time = jsonObject.getJSONObject("data").getString("t");

                Log.e("lanc", "当前时间提示：" + time);
                return time;
            } else {

            }
        } catch (Exception e) {
          e.printStackTrace();

        }
        return null;
    }
}
