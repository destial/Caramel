package xyz.destiall.caramel.editor.ui;

import xyz.destiall.caramel.editor.Scene;

public abstract class Panel {
    protected final Scene scene;
    public Panel(Scene scene) {
        this.scene = scene;
    }

    public abstract void imguiLayer();
}
