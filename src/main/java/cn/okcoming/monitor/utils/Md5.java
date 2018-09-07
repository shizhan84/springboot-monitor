package cn.okcoming.monitor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bluces on 2016/11/24.
 */
public class Md5 {
    private static final Logger LOG =
            LoggerFactory.getLogger(Md5.class);

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static String encode(final String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return bytesToHexString(digest.digest(string.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("exception when encode md5, detail",e);
        } catch (UnsupportedEncodingException e) {
            LOG.warn("exception when encode md5, detail",e);
        }
        return null;
    }

    public static String encode(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            return bytesToHexString(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("exception when encode md5, detail",e);
        }
        return null;
    }

    public static String bytesToHexString(byte[] bytes) {
        final char[] buf = new char[bytes.length * 2];

        byte b;
        int c = 0;
        for (int i = 0, z = bytes.length; i < z; i++) {
            b = bytes[i];
            buf[c++] = DIGITS[(b >> 4) & 0xf];
            buf[c++] = DIGITS[b & 0xf];
        }

        return new String(buf);
    }
}
