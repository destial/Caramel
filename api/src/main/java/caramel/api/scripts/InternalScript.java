package caramel.api.scripts;

import caramel.api.Component;
import caramel.api.objects.GameObject;
import xyz.destiall.java.reflection.Reflect;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class InternalScript {
    private final Class<?> compiledClass;
    private final CharSequence code;
    private final File file;

    public InternalScript(Class<?> compiledClass, File file, CharSequence code) {
        this.compiledClass = compiledClass;
        this.code = code;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public CharSequence getCode() {
        return code;
    }

    public Class<? extends Component> getCompiledClass() {
        return (Class<? extends Component>) compiledClass;
    }

    public Component getAsComponent(GameObject gameObject) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Component> constructor = (Constructor<? extends Component>) Reflect.getConstructor(getCompiledClass(), GameObject.class);
        return constructor.newInstance(gameObject);
    }
}
