package com.lancaiwu.sleep.utils;

import android.os.SystemClock;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by lancaiwu on 2019/1/21.
 */

public class IOUtils {

    public static String readText(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    result.append(lineTxt);
                }
                read.close();
            } else {
                //文件不存在
                //throw new IllegalStateException("没有文件");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result.toString();
    }

    /**
     * 将字符串写出到指定的文件中
     *
     * @param filePath 要写出到文件路径，如/xxx/123.txt
     * @param text     要写出的字符串
     * @param append   写出的文字是否追加在原有的文字的后面
     */
    public static void writeText(String filePath, String text, boolean append) {
        BufferedOutputStream bufferedOutputStream = null;

        String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        File fileDir = new File(dirPath);
        if (!fileDir.exists()) {//如果文件夹不存在就创建文件夹
            fileDir.mkdirs();
        }

        try {

            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            int size = new FileInputStream(file).available() / 1024; //结果以KB为单位
            if (size > 300) {
                file.delete();
                SystemClock.sleep(300);
                if (!file.exists()) {
                    file.createNewFile();
                }
            }

            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath, append));
            byte b[] = text.getBytes();
            bufferedOutputStream.write(b, 0, b.length);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
