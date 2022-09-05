package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;

public class IntegerGraphNode extends GraphNode<Integer> {
    public int value;
    public IntegerGraphNode(final int nodeId) {
        super(nodeId);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(final Integer value) {
        this.value = value;
    }

    @Override
    public boolean execute(final Blueprint script, final Graph graph) {
        return false;
    }
}
