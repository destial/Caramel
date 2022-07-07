package xyz.destiall.caramel.app.scripts.loader;

import javax.tools.JavaFileObject;

public final class FileScriptMemoryJavaObject extends AbstractScriptJavaObject {
    private final Object origin;
    private final String code;

    FileScriptMemoryJavaObject(Object origin, String className, JavaFileObject.Kind kind, String code) {
        super(className, kind);

        this.origin = origin;
        this.code = code;
    }

    public Object getOrigin() {
        return origin;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
