package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;
import caramel.api.debug.Debug;

import java.util.Objects;
import java.util.Set;

public class BooleanGraphNode extends GraphNode<Boolean> {
    public Object first;
    public Object second;
    public BooleanGraphNode(int nodeId) {
        super(nodeId);
    }

    @Override
    public Boolean getValue() {
        return Objects.equals(first, second);
    }

    public int getInputPinLeft() {
        return getId() << 3;
    }

    public int getInputPinRight() {
        return getId() << 6;
    }

    public int getOutputPinTrue() {
        return getId() << 11;
    }

    public int getOutputPinFalse() {
        return getId() << 12;
    }

    @Override
    public void setValue(Boolean value) {
        return;
    }

    @Override
    public boolean execute(VisualScript script, Graph graph) {
        Set<Link> links1 = graph.findLinksByInput(getInputPinLeft());
        Link firstLink = links1.stream().findFirst().orElse(null);
        if (firstLink == null) return true;

        GraphNode<?> first = graph.findNodeByOutput(firstLink.getStart());

        Set<Link> links2 = graph.findLinksByInput(getInputPinRight());
        Link secondLink = links2.stream().findFirst().orElse(null);
        if (secondLink == null) return true;

        GraphNode<?> second = graph.findNodeByOutput(secondLink.getStart());

        if (first == null || second == null) return true;

        setFirst(first.getValue());
        setSecond(second.getValue());

        if (getValue()) {
            Set<Link> trueOutputs = graph.findLinksByOutput(getOutputPinTrue());
            System.out.println(trueOutputs);
            for (Link l : trueOutputs) {
                graph.runLink(script, l);
            }
        } else {
            Set<Link> falseOutputs = graph.findLinksByOutput(getOutputPinFalse());
            for (Link l : falseOutputs) {
                graph.runLink(script, l);
            }
        }
        return true;
    }

    public void setFirst(Object first) {
        this.first = first;
    }

    public void setSecond(Object second) {
        this.second = second;
    }
}
