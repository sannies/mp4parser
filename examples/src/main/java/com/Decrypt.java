package com;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Decrypt {

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static String encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[(l << 1)];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }
        return new String(out);
    }

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte enc[] = new byte[]{(byte) 0x9d, (byte) 0xbb, (byte) 0xed, (byte) 0x89, 0x4d, (byte) 0xd6, 0x3c, 0x3b, (byte) 0x88, 0x1a, 0x7a, 0x59, 0x6f, (byte) 0xb9, (byte) 0xeb, 0x24, (byte) 0xa6, 0x62, (byte) 0xe9, 0x22};
        Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(new byte[16], "AES"), new IvParameterSpec(new byte[16]));
        byte[] plain = c.doFinal(enc);
        System.err.println(encodeHex(plain));
    }
}
