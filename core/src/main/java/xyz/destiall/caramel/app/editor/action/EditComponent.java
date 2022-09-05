package xyz.destiall.caramel.app.editor.action;

import caramel.api.Component;
import caramel.api.objects.Scene;

public abstract class EditComponent<C extends Component> extends EditorAction {
    public final C component;

    public EditComponent(final Scene scene, final C component) {
        super(scene);
        this.component = component;
    }
}
