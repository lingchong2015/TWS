package curry.stephen.tws.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by LingChong on 2016/6/20 0020.
 */
public class MD5Helper {

    private static final String HEX_NUMBER_STRING = "0123456789ABCDEF";
    private static final int SALT_LENGTH = 12;

    private static byte[] hexStringToBytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] hexChars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (HEX_NUMBER_STRING.indexOf(hexChars[pos]) << 4
                    | HEX_NUMBER_STRING.indexOf(hexChars[pos + 1]));
        }
        return result;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }

    public static boolean validatePassword(String password, String passwordInDb) {
        byte[] pwdInDb = hexStringToBytes(passwordInDb);
        byte[] salt = new byte[SALT_LENGTH];

        System.arraycopy(pwdInDb, 0, salt, 0, SALT_LENGTH);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(salt);
        try {
            messageDigest.update(password.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] digest = messageDigest.digest();

        byte[] digestInDb = new byte[pwdInDb.length - SALT_LENGTH];
        System.arraycopy(pwdInDb, SALT_LENGTH, digestInDb, 0, digestInDb.length);
        if (Arrays.equals(digest, digestInDb)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getEncryptedPassword(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(salt);
        try {
            messageDigest.update(password.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] digest = messageDigest.digest();

        byte[] pwd = new byte[digest.length + SALT_LENGTH];
        System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH);
        System.arraycopy(digest, 0, pwd, SALT_LENGTH, digest.length);
        return bytesToHexString(pwd);
    }
}
