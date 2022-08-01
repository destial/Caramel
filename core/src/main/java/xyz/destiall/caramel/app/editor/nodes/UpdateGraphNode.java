package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;

public class UpdateGraphNode extends GraphNode<Runnable> {
    private Runnable updateFunction;
    public UpdateGraphNode(final int nodeId) {
        super(nodeId);
        setName("Update Node");
    }

    @Override
    public Runnable getValue() {
        return updateFunction;
    }

    @Override
    public void setValue(Runnable value) {
        updateFunction = value;
    }

    @Override
    public boolean execute(VisualScript script, Graph graph) {

        return false;
    }
}
