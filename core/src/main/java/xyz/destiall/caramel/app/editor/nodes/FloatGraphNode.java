package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;

public class FloatGraphNode extends GraphNode<Float> {
    public float value;
    public FloatGraphNode(final int nodeId) {
        super(nodeId);
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public void setValue(final Float value) {
        this.value = value;
    }

    @Override
    public boolean execute(final Blueprint script, final Graph graph) {
        return false;
    }
}
