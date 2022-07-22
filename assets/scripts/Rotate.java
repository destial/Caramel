package scripts;

import caramel.api.objects.*;
import caramel.api.scripts.Script;

public class Rotate extends Script {
    public Rotate(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {

    }

    // This method is called on every frame
    @Override
    public void update() {
        transform.rotation.z += 1/60f;
    }
}