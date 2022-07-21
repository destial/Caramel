package xyz.destiall.caramel.app.editor.action;

import caramel.api.Component;
import caramel.api.objects.Scene;

public abstract class EditComponent<C extends Component> extends EditorAction {
    public C component;

    public EditComponent(Scene scene, C component) {
        super(scene);
        this.component = component;
    }
}
