package xyz.destiall.caramel.app.editor.nodes;

public class Link {
    private final int id;
    private final int start;
    private final int end;

    public Link(final int id, final int start, final int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getId() {
        return id;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return start == link.start && end == link.end;
    }
}
