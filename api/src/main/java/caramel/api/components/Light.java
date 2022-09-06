package caramel.api.components;

import caramel.api.Component;
import caramel.api.interfaces.HideInEditor;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;

/**
 * Unused component. Was meant to be for 3D environments, but that has halted.
 */
public final class Light extends Component {
    @InvokeOnEdit("setDirty") public Vector3f color;
    @HideInEditor public boolean dirty = false;

    public Light(final GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {}

    public void setDirty() {
        this.dirty = true;
    }
}
