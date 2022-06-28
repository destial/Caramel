package scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;

public class OtherScript extends Component {
    private NewScript reference;
    public OtherScript(GameObject gameObject) {
        super(gameObject);
        try {
            Class<?> c = Class.forName("scripts.NewScript");
            System.out.println(c);
        } catch (Exception e) {}
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }
}