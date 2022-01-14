package utils;

public class StringUtils {
    public static String redact(String input, int toLength) {
        if (input != null && input.length() <= toLength) {
            return input;
        } else {
            return input.substring(0, toLength);
        }
    }
}
