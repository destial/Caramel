package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;
import caramel.api.debug.Debug;

public class DebugGraphNode extends GraphNode<String> {
    public String value = "Debug";
    public DebugGraphNode(final int nodeId) {
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
        Debug.log(value);
        return false;
    }
}
