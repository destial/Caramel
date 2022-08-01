package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.VisualScript;
import caramel.api.debug.Debug;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Graph {
    private int nextNodeId = 1;
    private int nextLinkId = 1;
    private final Set<GraphNode<?>> nodes;
    private final Set<Link> links;
    public String ini = "";

    public Graph() {
        nodes = ConcurrentHashMap.newKeySet();
        links = ConcurrentHashMap.newKeySet();
    }

    public void runStart(VisualScript script) {
        nodes.stream().filter(n -> n instanceof StartGraphNode).forEach(node ->
                links.stream().filter(l -> l.getStart() == node.getOutputPinId()).forEach(l ->
                        runLink(script, l)));
    }

    public void runUpdate(VisualScript script) {
        nodes.stream().filter(n -> n instanceof UpdateGraphNode).forEach(node ->
                links.stream().filter(l -> l.getStart() == node.getOutputPinId()).forEach(l ->
                        runLink(script, l)));
    }

    public IntGraphNode createIntGraphNode() {
        final IntGraphNode node = new IntGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public StringGraphNode createStringGraphNode() {
        final StringGraphNode node = new StringGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public StartGraphNode createStartGraphNode() {
        final StartGraphNode node = new StartGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public UpdateGraphNode createUpdateGraphNode() {
        final UpdateGraphNode node = new UpdateGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public MethodGraphNode createMethodGraphNode() {
        final MethodGraphNode node = new MethodGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public DebugGraphNode createDebugGraphNode() {
        final DebugGraphNode node = new DebugGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public BooleanGraphNode createBooleanGraphNode() {
        final BooleanGraphNode node = new BooleanGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public FloatGraphNode createFloatGraphNode() {
        final FloatGraphNode node = new FloatGraphNode(nextNodeId++);
        this.nodes.add(node);
        return node;
    }

    public void link(int source, int target) {
        Link link = new Link(nextLinkId++, source, target);
        links.add(link);
    }

    public GraphNode<?> getNode(int id) {
        return nodes.stream().filter(n -> n.getId() == id).findFirst().orElse(null);
    }

    public void runLink(VisualScript script, Link link) {
        GraphNode<?> source = findNodeByOutput(link.getStart());
        GraphNode<?> target = findNodeByInput(link.getEnd());
        if (source == null || target == null) return;

        if (!target.execute(script, this)) {
            Set<Link> links = findLinksByOutput(target.getOutputPinId());
            for (Link l : links) {
                runLink(script, l);
            }
        }
    }

    public Set<Link> findLinksByOutput(int pin) {
        return links.stream().filter(l -> l.getStart() == pin).collect(Collectors.toSet());
    }

    public Set<Link> findLinksByInput(int pin) {
        return links.stream().filter(l -> l.getEnd() == pin).collect(Collectors.toSet());
    }

    public GraphNode<?> findNodeByOutput(int pin) {
        return nodes.stream().filter(n -> {
            if (n instanceof BooleanGraphNode) {
                BooleanGraphNode b = (BooleanGraphNode) n;
                return b.getOutputPinFalse() == pin || b.getOutputPinTrue() == pin;
            }
            return n.getOutputPinId() == pin;
        }).findFirst().orElse(null);
    }

    public GraphNode<?> findNodeByInput(int pin) {
        return nodes.stream().filter(n -> {
            if (n.getInputPinId() == pin) {
                return true;
            }
            if (n instanceof BooleanGraphNode) {
                BooleanGraphNode b = (BooleanGraphNode) n;
                return b.getInputPinLeft() == pin || b.getInputPinRight() == pin;
            }
            return false;
        }).findFirst().orElse(null);
    }

    public void unlink(int id) {
        links.removeIf(l -> l.getId() == id);
    }

    public Collection<Link> getLinks() {
        return links;
    }

    public Collection<GraphNode<?>> getNodes() {
        return nodes;
    }

    public void save(String ini) {
        this.ini = ini;
    }
}
