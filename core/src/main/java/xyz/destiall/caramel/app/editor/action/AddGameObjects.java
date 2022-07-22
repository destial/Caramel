package xyz.destiall.caramel.app.editor.action;

import caramel.api.objects.GameObject;
import caramel.api.objects.Scene;

import java.util.ArrayList;
import java.util.List;

public final class AddGameObjects extends EditorAction {
    public List<GameObject> added;

    public AddGameObjects(Scene scene) {
        super(scene);
        this.added = new ArrayList<>();
    }

    @Override
    public void undo() {
        if (action == PreviousAction.UNDO) return;
        for (GameObject a : added) {
            scene.destroy(a);
        }
        action = PreviousAction.UNDO;
    }

    @Override
    public void redo() {
        if (action == PreviousAction.REDO) return;
        for (GameObject a : added) {
            scene.addGameObject(a);
        }
        action = PreviousAction.REDO;
    }
}
