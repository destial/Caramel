package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;

public class MethodGraphNode extends GraphNode<String> {
    public String value;
    public MethodGraphNode(int nodeId) {
        super(nodeId);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean execute(VisualScript script, Graph graph) {

        return false;
    }
}
