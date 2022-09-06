package caramel.api.render;

import caramel.api.Component;
import caramel.api.interfaces.Render;
import caramel.api.objects.GameObject;

public abstract class Renderer extends Component implements Render {
    public State renderState = State.WORLD;

    public Renderer(final GameObject gameObject) {
        super(gameObject);
    }

    public abstract void build();

    public State getRenderState() {
        if (renderState == null) {
            renderState = Renderer.State.WORLD;
        }
        return renderState;
    }

    public enum State {
        WORLD,
        UI
    }
}
