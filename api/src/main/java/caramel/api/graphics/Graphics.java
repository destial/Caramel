package caramel.api.graphics;

public final class Graphics {
    private static GL30 api;

    public Graphics(GL30 api) {
        if (Graphics.api != null) return;
        Graphics.api = api;
    }

    public static GL30 get() {
        return api;
    }
}
