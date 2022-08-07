package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;

public class FloatGraphNode extends GraphNode<Float> {
    public float value;
    public FloatGraphNode(int nodeId) {
        super(nodeId);
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public boolean execute(VisualScript script, Graph graph) {
        return false;
    }
}
