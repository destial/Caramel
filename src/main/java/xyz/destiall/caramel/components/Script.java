package xyz.destiall.caramel.components;

import xyz.destiall.caramel.app.Time;
import xyz.destiall.caramel.objects.GameObject;

public class Script extends Component {
    public Script(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        transform.rotation.rotateX((float) Math.toRadians(180));
    }

    @Override
    public void update() {
        transform.rotation.rotateY(Time.deltaTime * 5f);
    }
}
