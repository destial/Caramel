package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;

import java.util.Objects;
import java.util.Set;

public class BooleanGraphNode extends GraphNode<Boolean> {
    public Object first;
    public Object second;
    public BooleanGraphNode(final int nodeId) {
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
    public void setValue(final Boolean value) {

    }

    @Override
    public boolean execute(final Blueprint script, final Graph graph) {
        final Set<Link> links1 = graph.findLinksByInput(getInputPinLeft());
        final Link firstLink = links1.stream().findFirst().orElse(null);
        if (firstLink == null) return true;

        final GraphNode<?> first = graph.findNodeByOutput(firstLink.getStart());

        final Set<Link> links2 = graph.findLinksByInput(getInputPinRight());
        final Link secondLink = links2.stream().findFirst().orElse(null);
        if (secondLink == null) return true;

        final GraphNode<?> second = graph.findNodeByOutput(secondLink.getStart());

        if (first == null || second == null) return true;

        setFirst(first.getValue());
        setSecond(second.getValue());

        if (getValue()) {
            final Set<Link> trueOutputs = graph.findLinksByOutput(getOutputPinTrue());
            for (final Link l : trueOutputs) {
                graph.runLink(script, l);
            }
        } else {
            final Set<Link> falseOutputs = graph.findLinksByOutput(getOutputPinFalse());
            for (final Link l : falseOutputs) {
                graph.runLink(script, l);
            }
        }
        return true;
    }

    public void setFirst(final Object first) {
        this.first = first;
    }

    public void setSecond(final Object second) {
        this.second = second;
    }
}
