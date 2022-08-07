package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;

public class StringGraphNode extends GraphNode<String> {
    public String value = "string";
    public StringGraphNode(int nodeId) {
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
