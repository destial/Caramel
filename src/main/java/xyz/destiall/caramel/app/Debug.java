package xyz.destiall.caramel.app;

public class Debug {
    public static void log(Object log) {
        System.out.println(log);
    }

    public static void logError(Object error) {
        System.err.println("ERROR: " + error);
    }
}
