package scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.GameObject;

public class NewScript extends Component {
    public NewScript(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        transform.rotation.rotateX((float) Math.toRadians(180));
    }

    @Override
    public void update() {
        transform.rotation.rotateY(Time.deltaTime * 1.3f);
    }
}