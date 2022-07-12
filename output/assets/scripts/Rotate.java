package scripts;

import xyz.destiall.caramel.api.objects.*;
import xyz.destiall.caramel.api.scripts.Script;
import xyz.destiall.caramel.api.Time;

public class Rotate extends Script {
    private float z;
    public Rotate(GameObject gameObject) {
        super(gameObject);
        z = 0;
    }

    // This method is called on the first frame
    @Override
    public void start() {

    }

    // This method is called on every frame
    @Override
    public void update() {
        transform.rotation.z += Time.deltaTime * 2;
    }
}