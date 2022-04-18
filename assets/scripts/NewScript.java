package scripts;

import xyz.destiall.caramel.app.Debug;
import xyz.destiall.caramel.app.input.Input;
import xyz.destiall.caramel.components.Component;
import xyz.destiall.caramel.editor.Time;
import xyz.destiall.caramel.objects.GameObject;

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