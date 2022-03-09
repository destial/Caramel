package xyz.destiall.caramel.app.events;

import xyz.destiall.java.events.Event;

import java.io.File;

public class FileEvent extends Event {
    private final File file;
    private final Kind kind;
    public FileEvent(File file, Kind kind) {
        this.file = file;
        this.kind = kind;
    }

    public File getFile() {
        return file;
    }

    public Kind getKind() {
        return kind;
    }

    public enum Kind {
        CREATE, MODIFY, DELETE
    }
}
