package xyz.destiall.caramel.app.editor.nodes;

public abstract class GraphNode<T> {
    public final int nodeId;
    public final int inputPinId;
    public final int outputPinId;

    public int outputNodeId = -1;

    public GraphNode(final int nodeId, final int inputPinId, final int outputPintId) {
        this.nodeId = nodeId;
        this.inputPinId = inputPinId;
        this.outputPinId = outputPintId;
    }

    public int getInputPinId() {
        return inputPinId;
    }

    public int getOutputPinId() {
        return outputPinId;
    }

    public String getName() {
        return "Node " + (char) (64 + nodeId);
    }

    public abstract T getValue();
    public abstract void setValue(T value);
}
