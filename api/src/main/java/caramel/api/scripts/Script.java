package caramel.api.scripts;

import caramel.api.Application;
import caramel.api.Component;
import caramel.api.debug.Debug;
import caramel.api.objects.GameObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Script extends Component {
    public Script(GameObject gameObject) {
        super(gameObject);
    }

    protected GameObject findGameObject(String name) {
        return gameObject.scene.findGameObject(name);
    }

    protected <C extends Component> List<C> findWithComponent(Class<C> clazz) {
        return gameObject.scene.getGameObjects().stream().filter(g -> g.hasComponent(clazz)).map(g -> g.getComponent(clazz)).collect(Collectors.toList());
    }

    @Override
    public Component clone(GameObject gameObject, boolean copyId) {
        InternalScript internal = Application.getApp().getScriptManager().getScript(getClass().getName());
        try {
            Component clone = internal.getAsComponent(gameObject);
            for (Field field : getClass().getFields()) {
                try {
                    if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
                    boolean prev = field.isAccessible();
                    field.setAccessible(true);

                    field.set(clone, field.get(this));
                    field.setAccessible(prev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            clone.id = copyId ? id : gameObject.scene.generateId();
            return clone;
        } catch (Exception e) {
            Debug.log(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
