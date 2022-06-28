package xyz.destiall.caramel.api.scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Script extends Component {
    public Script(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void imguiLayer() {

    }

    protected GameObject findGameObject(String name) {
        return gameObject.scene.findGameObject(name);
    }

    protected <C extends Component> List<C> findWithComponent(Class<C> clazz) {
        return gameObject.scene.getGameObjects().stream().filter(g -> g.hasComponent(clazz)).map(g -> g.getComponent(clazz)).collect(Collectors.toList());
    }
}
