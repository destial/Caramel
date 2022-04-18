package xyz.destiall.caramel.components;

import xyz.destiall.caramel.app.Debug;
import xyz.destiall.caramel.app.input.Input;
import xyz.destiall.caramel.objects.GameObject;

public class Script extends Component {
    public Script(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        Debug.log(Input.getMouseDeltaX() + " : " + Input.getMouseDeltaY());
    }
}
