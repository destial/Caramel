package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;

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
    public void setValue(final Runnable value) {
        startFunction = value;
    }

    @Override
    public boolean execute(final Blueprint script, final Graph graph) {

        return false;
    }
}
