package scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.Debug;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.app.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List keysPressed = Input.getKeysPressed().stream().map(k -> new Pair<>(Input.Key.getKeyName(k), k)).collect(Collectors.toList());
        System.out.println(keysPressed);
    }
}