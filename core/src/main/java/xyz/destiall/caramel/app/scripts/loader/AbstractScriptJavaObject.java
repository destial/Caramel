package xyz.destiall.caramel.app.scripts.loader;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public abstract class AbstractScriptJavaObject extends SimpleJavaFileObject {
    public AbstractScriptJavaObject(final String name, final JavaFileObject.Kind kind) {
        super(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind);
    }
}
