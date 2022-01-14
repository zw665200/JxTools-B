package com.wx.tools.utils;

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/1/29 15:33
 */
public class Binascii {
    private static final char charGlyph_[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String hexlify(byte[] bytes) {
        StringBuilder hexAscii = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            hexAscii.append(charGlyph_[(b & 0xf0) >> 4]);
            hexAscii.append(charGlyph_[b & 0x0f]);
        }
        return hexAscii.toString();
    }

    public static byte[] unhexlify(String asciiHex) {
        if (asciiHex.length() % 2 != 0) {
            throw new RuntimeException("Input to unhexlify must have even-length");
        }

        int len = asciiHex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(asciiHex.charAt(i), 16) << 4) +
                    Character.digit(asciiHex.charAt(i + 1), 16));
        }
        return data;
    }

    public static int hexToDecimal(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static String hexToBinary(String hex) {
        return Integer.toBinaryString(Integer.parseInt(hex, 16));
    }

    public static int binaryToDecimal(String binary) {
        return Integer.parseInt(binary, 2);
    }

}
