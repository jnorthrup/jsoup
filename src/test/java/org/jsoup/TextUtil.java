package org.jsoup;

import java.util.regex.Pattern;

/**
 Text utils to ease testing

 @author Jonathan Hedley, jonathan@hedley.net */
public final class TextUtil {
    private static final Pattern COMPILE = Pattern.compile("\\n\\s*");

    private TextUtil() {
    }

    public static String stripNewlines(String text) {
        String text1 = COMPILE.matcher(text).replaceAll("");
        return text1;
    }
}
