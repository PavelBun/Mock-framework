package Mockframework.Demo;

public class TestClock {
    public static long now() {
        return System.currentTimeMillis();
    }
    public static String format(String pattern) {
        return "real-" + pattern;
    }
}