package xyz.destiall.caramel.app.editor.action;

import caramel.api.Component;
import caramel.api.objects.GameObject;

import java.util.ArrayList;
import java.util.List;

public final class AddComponents extends EditorAction {
    private final GameObject gameObject;
    public final List<Component> added;

    public AddComponents(final GameObject gameObject) {
        super(gameObject.scene);
        this.gameObject = gameObject;
        this.added = new ArrayList<>();
    }

    @Override
    public void undo() {
        if (action == PreviousAction.UNDO) return;
        for (final Component a : added) {
            gameObject.removeComponent(a.getClass());
        }
        action = PreviousAction.UNDO;
    }

    @Override
    public void redo() {
        if (action == PreviousAction.REDO) return;
        for (final Component a : added) {
            gameObject.addComponent(a);
        }
        action = PreviousAction.REDO;
    }
}
