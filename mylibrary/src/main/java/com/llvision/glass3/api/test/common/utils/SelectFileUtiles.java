package com.llvision.glass3.api.test.common.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.llvision.support.uvc.utils.UriHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @Project: glxss_android_movidius_api_test
 * @Description:
 * @Author: haijianming
 * @Time: 2018/12/12 下午12:30
 */
public class SelectFileUtiles {
    public static String getModelPath(Context mContext, Uri uri){
        final String path = UriHelper.getPath(mContext, uri);
        if (path != null) {
            String[] pathSplit = path.split("/");

        }
        if (Strings.isNullOrEmpty(path)
                || path.endsWith("jpg")
                || path.endsWith("jpeg")
                || path.endsWith("png")
                || path.endsWith("txt")
                || path.endsWith("pdf")
                || path.endsWith("doc")
                || path.endsWith("docx")
                || path.endsWith("xls")
                || path.endsWith("xlsx")
                || path.endsWith("ppt")) {
            return null;
        }
        return null;
    }
    /**
     * 获取SD下的应用目录
     */
    public static String getExternalStoragePath(Context context,String addfilePath) {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        String ROOT_DIR = "Android/data/" + context.getPackageName();
        sb.append(ROOT_DIR);
        sb.append(File.separator);
        sb.append(addfilePath);
        sb.append(File.separator);
        return sb.toString();
    }

    private static boolean copyFile(String oldPath, String newPath) {
        if (oldPath == null || "".equals(oldPath) || newPath == null) {
            return false;
        }

        File curFile = new File(oldPath);
        if (!curFile.exists()) {
            return false;
        }
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (!oldfile.exists()){
                oldfile.mkdirs();
            }
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        return false;
        }
    return true;
    }
}
