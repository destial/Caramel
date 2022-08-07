package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;

public class StartGraphNode extends GraphNode<Runnable> {
    private Runnable startFunction;
    public StartGraphNode(final int nodeId) {
        super(nodeId);
        setName("Start Node");
    }

    @Override
    public Runnable getValue() {
        return startFunction;
    }

    @Override
    public void setValue(Runnable value) {
        startFunction = value;
    }

    @Override
    public boolean execute(VisualScript script, Graph graph) {

        return false;
    }
}
