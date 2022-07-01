package xyz.destiall.caramel.app.events;

import xyz.destiall.java.events.Event;

import java.io.File;

public class FileEvent extends Event {
    private final File file;
    private final Type type;
    public FileEvent(File file, Type type) {
        super(true);
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
