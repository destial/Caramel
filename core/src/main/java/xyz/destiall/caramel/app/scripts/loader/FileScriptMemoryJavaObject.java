package xyz.destiall.caramel.app.scripts.loader;

import javax.tools.JavaFileObject;
import java.io.File;

public final class FileScriptMemoryJavaObject extends AbstractScriptJavaObject {
    private final File origin;
    private final String code;
    private final String className;

    FileScriptMemoryJavaObject(final File origin, final String className, final JavaFileObject.Kind kind, final String code) {
        super(className, kind);
        this.className = className;
        this.origin = origin;
        this.code = code;
    }

    @Override
    public String getName() {
        return className;
    }

    public File getOrigin() {
        return origin;
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
        return code;
    }

    public String getCode() {
        return code;
    }
}
