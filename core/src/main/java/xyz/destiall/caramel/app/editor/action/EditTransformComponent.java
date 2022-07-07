package xyz.destiall.caramel.app.editor.action;

import org.joml.Vector3f;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.objects.Scene;

public final class EditTransformComponent extends EditComponent<Transform> {
    public Vector3f pos1;
    public Vector3f rot1;
    public Vector3f sca1;

    public Vector3f pos2;
    public Vector3f rot2;
    public Vector3f sca2;

    public EditTransformComponent(Scene scene, Transform transform) {
        super(scene, transform);
        pos1 = new Vector3f(transform.position);
        rot1 = new Vector3f(transform.rotation);
        sca1 = new Vector3f(transform.scale);
    }

    @Override
    public void undo() {
        if (action == PreviousAction.UNDO) return;
        pos2 = new Vector3f(component.position);
        rot2 = new Vector3f(component.rotation);
        sca2 = new Vector3f(component.scale);

        component.position.set(pos1);
        component.rotation.set(rot1);
        component.scale.set(sca1);

        action = PreviousAction.UNDO;
    }

    @Override
    public void redo() {
        if (action == PreviousAction.REDO) return;
        component.position.set(pos2);
        component.rotation.set(rot2);
        component.scale.set(sca2);

        action = PreviousAction.REDO;
    }
}
