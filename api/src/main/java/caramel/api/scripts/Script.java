package caramel.api.scripts;

import caramel.api.Application;
import caramel.api.Component;
import caramel.api.debug.Debug;
import caramel.api.objects.GameObject;
import caramel.api.objects.Scene;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Script extends Component {
    public Script(final GameObject gameObject) {
        super(gameObject);
    }

    /**
     * Find a {@link GameObject} that matches this name in this {@link Scene}.
     * @param name The name of the {@link GameObject} to find.
     * @return The matching {@link GameObject}, null if none found.
     */
    public GameObject findGameObject(final String name) {
        return gameObject.scene.findGameObject(name);
    }

    /**
     * Find a list of {@link Component} in every GameObjects that this Component Class.
     * @param clazz The {@link Component} class.
     * @return A list of matching {@link GameObject}s. Never null, can be empty.
     */
    public <C extends Component> List<C> findWithComponent(final Class<C> clazz) {
        return gameObject.scene.getGameObjects().stream().filter(g -> g.hasComponent(clazz)).map(g -> g.getComponent(clazz)).collect(Collectors.toList());
    }

    @Override
    public Component clone(final GameObject gameObject, final boolean copyId) {
        final InternalScript internal = Application.getApp().getScriptManager().getScript(getClass().getName());
        try {
            final Component clone = internal.getAsComponent(gameObject);
            for (final Field field : getClass().getFields()) {
                try {
                    if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
                    final boolean prev = field.isAccessible();
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
