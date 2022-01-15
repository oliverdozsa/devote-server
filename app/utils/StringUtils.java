package utils;

import java.util.Random;

public class StringUtils {
    public static String redact(String input, int toMaxLength) {
        if (input == null) {
            return null;
        } else if (input.length() <= toMaxLength) {
            return input;
        } else {
            return input.substring(0, toMaxLength);
        }
    }

    public static String createRandomAlphabeticString(int ofLength) {
        int letterACode = 97;
        int letterZCode = 122;

        Random random = new Random();

        return random.ints(letterACode, letterZCode + 1)
                .limit(ofLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
