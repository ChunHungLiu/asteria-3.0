package com.asteria.utility;

public final class BufferUtils {

    /**
     * The default constructor.
     * 
     * @throws InstantiationError
     *             if this class is instantiated.
     */
    private BufferUtils() {
        throw new InstantiationError("This class cannot be instantiated!");
    }

    /**
     * Converts an array of bytes to an {@code int}.
     * 
     * @param data
     *            the array of bytes.
     * @return the newly constructed {@code int}.
     */
    public static int hexToInt(byte[] data) {
        int value = 0;
        int n = 1000;
        for (int i = 0; i < data.length; i++) {
            int num = (data[i] & 0xFF) * n;
            value += num;
            if (n > 1) {
                n = n / 1000;
            }
        }
        return value;
    }
}
