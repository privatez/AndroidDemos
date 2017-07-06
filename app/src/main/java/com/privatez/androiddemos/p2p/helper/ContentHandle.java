package com.privatez.androiddemos.p2p.helper;

import com.privatez.androiddemos.p2p.bean.ContentInfo;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by private on 2017/5/16.
 */

public class ContentHandle {

    /**
     * 头部分割字符
     */
    public static final String HEAD_SPERATOR = "::";

    /**
     * 字节数组长度
     */
    public static final int HEADER_SIZE = 1024;

    public static final String UTF_8 = "UTF_8";


    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public ContentHandle(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    public static byte[] generate(String schem, long contentLength) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(schem);
        stringBuffer.append(HEAD_SPERATOR);
        stringBuffer.append(contentLength);
        final int cover = HEADER_SIZE - stringBuffer.toString().getBytes().length;
        if (stringBuffer.length() < HEADER_SIZE) {
            for (int i = 0; i < cover; i++) {
                stringBuffer.append(" ");
            }
        }

        return stringBuffer.toString().getBytes();
    }

    public static ContentInfo parse(byte[] content) {
        String str = new String(content);
        str.trim();
        final String[] strs = new String(content).trim().split(HEAD_SPERATOR);
        final String schem = strs[0];
        final long size = Long.valueOf(strs[1]);
        return new ContentInfo(schem, size);
    }

}
