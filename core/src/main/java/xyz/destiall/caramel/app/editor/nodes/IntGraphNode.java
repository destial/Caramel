package xyz.destiall.caramel.app.editor.nodes;

public class IntGraphNode extends GraphNode<Integer> {
    public int value;
    public IntGraphNode(int nodeId, int inputPinId, int outputPintId) {
        super(nodeId, inputPinId, outputPintId);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }
}
