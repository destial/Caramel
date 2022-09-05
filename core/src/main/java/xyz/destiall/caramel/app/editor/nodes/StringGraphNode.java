package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;

public class StringGraphNode extends GraphNode<String> {
    public String value = "string";
    public StringGraphNode(final int nodeId) {
        super(nodeId);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean execute(final Blueprint script, final Graph graph) {
        return false;
    }
}
