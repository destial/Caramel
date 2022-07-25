package xyz.destiall.caramel.app.editor.nodes;

public class StringGraphNode extends GraphNode<String> {
    public String value;
    public StringGraphNode(int nodeId, int inputPinId, int outputPintId) {
        super(nodeId, inputPinId, outputPintId);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
