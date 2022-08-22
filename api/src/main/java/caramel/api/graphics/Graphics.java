package caramel.api.graphics;

public final class Graphics {
    private Graphics() {}

    public static void set(GL30 api) {
        if (Graphics.api != null) return;
        Graphics.api = api;
    }

    private static GL30 api;
    public static GL30 get() {
        return api;
    }
}
