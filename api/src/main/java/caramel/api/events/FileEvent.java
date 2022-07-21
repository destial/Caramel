package caramel.api.events;

import xyz.destiall.java.events.Event;

import java.io.File;

public final class FileEvent extends Event {
    private final File file;
    private final Type type;
    public FileEvent(File file, Type type) {
        super(false);
        this.file = file;
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        CREATE, MODIFY, DELETE
    }
}
