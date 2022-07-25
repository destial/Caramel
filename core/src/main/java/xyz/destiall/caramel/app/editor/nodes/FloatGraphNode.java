package xyz.destiall.caramel.app.editor.nodes;

public class FloatGraphNode extends GraphNode<Float> {
    public float value;
    public FloatGraphNode(int nodeId, int inputPinId, int outputPintId) {
        super(nodeId, inputPinId, outputPintId);
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public void setValue(Float value) {
        this.value = value;
    }
}
