package xyz.destiall.caramel.app.editor.nodes;

public class BooleanGraphNode extends GraphNode<Boolean> {
    public boolean value;
    public BooleanGraphNode(int nodeId, int inputPinId, int outputPintId) {
        super(nodeId, inputPinId, outputPintId);
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }
}
