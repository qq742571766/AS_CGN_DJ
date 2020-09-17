package com.llvision.glass3.api.test.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Project: glxss_android_movidius_api_test
 * @Description:
 * @Author: haijianming
 * @Time: 2018/12/8 下午1:33
 */
public class FastYUVtoBitmap {
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    public FastYUVtoBitmap(Context context) {
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

    }

    /**
     * yuv数据转BITMAP
     *
     * @param yuvData
     * @param width
     * @param height
     * @return
     */
    public Bitmap convertYUVtoRGB(byte[] yuvData, int width, int height) {
        final byte[] bytes = convertI420ToRGBA(yuvData, width, height);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmpout.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
        return bmpout;
    }

    public byte[] convertI420ToRGBA(byte[] i420Data, int width, int height) {
        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs));
            yuvType.setYuvFormat(ImageFormat.YV12);
            yuvType.setX(width);
            yuvType.setY(height);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        in.copyFrom(i420Data);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        byte[] RGBA = new byte[width * height << 2];
        out.copyTo(RGBA);
        return RGBA;
    }

    /**
     * yuv数据scale成bitmap
     *
     * @param data
     * @param dataWidth
     * @param dataHeight
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public Bitmap convertYUVScaleClip(byte[] data, int dataWidth, int dataHeight, int x, int y, int width, int height) {
        Bitmap bitmap = convertYUVtoRGB(data, dataWidth, dataHeight);
        Bitmap scaleBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        bitmap.recycle();
        return scaleBitmap;
    }

    /**
     * @param bitmap
     * @param path
     */
    public void saveBitmap(Bitmap bitmap, String path) throws Exception {
        FileOutputStream fos = null;
        try {

            final File file = new File(path);
            final File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
