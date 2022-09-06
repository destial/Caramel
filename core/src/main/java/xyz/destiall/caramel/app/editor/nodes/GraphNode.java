package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;
import caramel.api.objects.StringWrapperImpl;

public abstract class GraphNode<T> {
    private final String clazz = getClass().getName();
    private final int nodeId;
    private StringWrapperImpl name;

    public GraphNode(final int nodeId) {
        this.nodeId = nodeId;
        this.name = new StringWrapperImpl("Node " + (char) (64 + nodeId));
    }

    public int getOutputPinId() {
        return nodeId << 24;
    }

    public int getInputPinId() {
        return nodeId << 8;
    }

    public int getId() {
        return nodeId;
    }

    public StringWrapperImpl getName() {
        if (name == null) {
            name = new StringWrapperImpl("Node " + (char) (64 + nodeId));
        }
        return name;
    }

    public abstract T getValue();
    public abstract void setValue(final T value);

    public void setName(final String s) {
        this.name.set(s);
    }

    public abstract boolean execute(final Blueprint script, final Graph graph);
}
