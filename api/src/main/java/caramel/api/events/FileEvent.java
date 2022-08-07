package caramel.api.events;

import xyz.destiall.java.events.Event;

import java.io.File;
import java.util.Objects;

/**
 * This event is called whenever a script file is modified / created / deleted.
 * This is most useful in recompiling scripts on the fly.
 */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEvent event = (FileEvent) o;
        return Objects.equals(file, event.file) && type == event.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, type);
    }

    public enum Type {
        CREATE, MODIFY, DELETE
    }
}
