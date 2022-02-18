package android.util;

public class Base64 {

    public static String encodeToString(byte[] input, int flags) {
        return android.util.Base64.encodeToString(input, flags);
    }

    public static byte[] decode(String str, int flags) {
        return android.util.Base64.decode(str, flags);
    }

}