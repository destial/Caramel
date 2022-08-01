package xyz.destiall.caramel.app.editor.nodes;

public class Link {
    private final int id;
    private final int start;
    private final int end;

    public Link(int id, int start, int end) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return start == link.start && end == link.end;
    }
}
