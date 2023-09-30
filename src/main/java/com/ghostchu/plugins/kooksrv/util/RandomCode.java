package com.ghostchu.plugins.kooksrv.util;

import java.util.Random;

public class RandomCode {
    public static String generateCode(int len){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len ; i++) {
            result.append(randomChar());
        }
        return result.toString();
    }

    public static char randomChar() {
        Random r = new Random();
        String s = "ABCDEFGHIJKLMNPQRSTUVWXYZ0123456789";
        return s.charAt(r.nextInt(s.length()));
    }
}
