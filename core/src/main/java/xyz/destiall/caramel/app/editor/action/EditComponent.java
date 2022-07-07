package xyz.destiall.caramel.app.editor.action;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.Scene;

import java.util.ArrayList;
import java.util.List;

public abstract class EditComponent<C extends Component> extends EditorAction {
    public C component;

    public EditComponent(Scene scene, C component) {
        super(scene);
        this.component = component;
    }
}
