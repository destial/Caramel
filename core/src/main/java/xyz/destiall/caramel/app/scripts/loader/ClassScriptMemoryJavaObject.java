package xyz.destiall.caramel.app.scripts.loader;

import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class ClassScriptMemoryJavaObject extends AbstractScriptJavaObject {
    private transient ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
    private transient byte[] bytes = null;
    private final String className;

    ClassScriptMemoryJavaObject(final String className) {
        super(className, JavaFileObject.Kind.CLASS);
        this.className = className;
    }

    public byte[] getBytes() {
        if (bytes == null) {
            bytes = byteOutputStream.toByteArray();
            byteOutputStream = null;
        }
        return bytes;
    }

    @Override
    public String getName() {
        return className;
    }

    @Override
    public OutputStream openOutputStream() {
        return byteOutputStream;
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(getBytes());
    }
}
