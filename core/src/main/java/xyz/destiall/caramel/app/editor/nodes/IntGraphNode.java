package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;

public class IntGraphNode extends GraphNode<Integer> {
    public int value;
    public IntGraphNode(int nodeId) {
        super(nodeId);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean execute(VisualScript script, Graph graph) {

        return false;
    }
}
