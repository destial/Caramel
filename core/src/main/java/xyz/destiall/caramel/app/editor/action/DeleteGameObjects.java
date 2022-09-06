package xyz.destiall.caramel.app.editor.action;

import caramel.api.objects.GameObject;
import caramel.api.objects.Scene;

import java.util.ArrayList;
import java.util.List;

public final class DeleteGameObjects extends EditorAction {
    public final List<GameObject> deleted;

    public DeleteGameObjects(final Scene scene) {
        super(scene);
        this.deleted = new ArrayList<>();
    }

    @Override
    public void undo() {
        if (action == PreviousAction.UNDO) return;
        for (final GameObject d : deleted) {
            scene.addGameObject(d);
        }
        action = PreviousAction.UNDO;
    }

    @Override
    public void redo() {
        if (action == PreviousAction.REDO) return;
        for (final GameObject d : deleted) {
            scene.destroy(d);
        }
        action = PreviousAction.REDO;
    }
}
