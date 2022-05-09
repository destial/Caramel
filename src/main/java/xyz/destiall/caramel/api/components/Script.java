package xyz.destiall.caramel.api.components;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Debug;
import xyz.destiall.caramel.api.GameObject;

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
