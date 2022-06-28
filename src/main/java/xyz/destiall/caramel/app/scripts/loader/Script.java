package xyz.destiall.caramel.app.scripts.loader;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.java.reflection.Reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Script {
    private final String name;
    private final Class<?> compiledClass;

    public Script(String name, Class<?> compiledClass) {
        this.name = name;
        this.compiledClass = compiledClass;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Component> getCompiledClass() {
        return (Class<? extends Component>) compiledClass;
    }

    public Component getAsComponent(GameObject gameObject) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Component> constructor = (Constructor<? extends Component>) Reflect.getConstructor(getCompiledClass(), GameObject.class);
        return constructor.newInstance(gameObject);
    }
}
