package scripts;

import xyz.destiall.caramel.app.Time;
import xyz.destiall.caramel.objects.GameObject;
import xyz.destiall.caramel.components.Component;

public class NewScript extends Component {
    public NewScript(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        transform.rotation.rotateX(Time.deltaTime);
    }
}