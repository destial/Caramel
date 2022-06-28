package scripts;

import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.scripts.Script;
import xyz.destiall.caramel.api.objects.GameObject;

import java.util.List;

public class OtherScript extends Script {
    private NewScript referenceOther;
    public OtherScript(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        List<NewScript> list = findWithComponent(NewScript.class);
        if (list.size() > 0) {
            referenceOther = list.get(0);
        }
    }

    @Override
    public void update() {
        if (referenceOther != null) {
            Debug.log(referenceOther.data);
        }
    }
}