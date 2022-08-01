package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;
import caramel.api.debug.Debug;

public class DebugGraphNode extends GraphNode<String> {
    public String value;
    public DebugGraphNode(int nodeId) {
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
        Debug.log("Debugger");
        return false;
    }
}
