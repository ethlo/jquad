package com.ethlo.quadkey;

public class Assert {

    public static void isTrue(boolean b, String s) {
        if (!b) {
            throw new IllegalArgumentException(s);
        }
    }
}
