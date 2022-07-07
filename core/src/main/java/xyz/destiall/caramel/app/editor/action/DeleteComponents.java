package xyz.destiall.caramel.app.editor.action;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;

import java.util.ArrayList;
import java.util.List;

public final class DeleteComponents extends EditorAction {
    private final GameObject gameObject;
    public List<Component> deleted;

    public DeleteComponents(GameObject gameObject) {
        super(gameObject.scene);
        this.gameObject = gameObject;
        this.deleted = new ArrayList<>();
    }

    @Override
    public void undo() {
        if (action == PreviousAction.UNDO) return;
        for (Component a : deleted) {
            gameObject.addComponent(a);
        }
        action = PreviousAction.UNDO;
    }

    @Override
    public void redo() {
        if (action == PreviousAction.REDO) return;
        for (Component a : deleted) {
            gameObject.removeComponent(a.getClass());
        }
        action = PreviousAction.REDO;
    }
}
