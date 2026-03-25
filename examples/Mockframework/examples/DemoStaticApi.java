package Mockframework.examples;

public final class DemoStaticApi {
    private DemoStaticApi() {
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static String greet(String name) {
        return "hello-" + name;
    }

    public static String join(String left, String right) {
        return left + ":" + right;
    }

    public static void audit(String message) {
        // demo side effect placeholder
    }

    public static String unstable(String input) {
        return "ok-" + input;
    }
}
