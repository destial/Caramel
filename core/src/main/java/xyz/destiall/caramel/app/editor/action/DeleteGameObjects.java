package xyz.destiall.caramel.app.editor.action;

import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.Scene;

import java.util.ArrayList;
import java.util.List;

public final class DeleteGameObjects extends EditorAction {
    public List<GameObject> deleted;

    public DeleteGameObjects(Scene scene) {
        super(scene);
        this.deleted = new ArrayList<>();
    }

    @Override
    public void undo() {
        if (action == PreviousAction.UNDO) return;
        for (GameObject d : deleted) {
            scene.addGameObject(d);
        }
        action = PreviousAction.UNDO;
    }

    @Override
    public void redo() {
        if (action == PreviousAction.REDO) return;
        for (GameObject d : deleted) {
            scene.destroy(d);
        }
        action = PreviousAction.REDO;
    }
}
