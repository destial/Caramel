package xyz.destiall.caramel.app.editor.action;

import xyz.destiall.caramel.api.objects.Scene;

public abstract class EditorAction {
    protected final Scene scene;
    public PreviousAction action;
    public EditorAction(Scene scene) {
        this.scene = scene;
        action = PreviousAction.REDO;
    }
    public abstract void undo();
    public abstract void redo();

    enum PreviousAction {
        UNDO,
        REDO,
    }
}
