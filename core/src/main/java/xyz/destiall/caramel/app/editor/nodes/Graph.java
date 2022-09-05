package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.components.Blueprint;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Graph {
    public static final Class<? extends GraphNode<?>>[] NODES = new Class[]{
            StartGraphNode.class, UpdateGraphNode.class, DebugGraphNode.class,
            ComponentGraphNode.class, FloatGraphNode.class, IntegerGraphNode.class,
            StringGraphNode.class, BooleanGraphNode.class
    };
    public static String getLabel(final Class<? extends GraphNode<?>> clazz) {
        final String name = clazz.getSimpleName();
        return name.replace("GraphNode", "");
    }
    private int nextNodeId = 1;
    private int nextLinkId = 1;
    private final Set<GraphNode<?>> nodes;
    private final Set<Link> links;
    public String ini;

    public Graph() {
        nodes = ConcurrentHashMap.newKeySet();
        links = ConcurrentHashMap.newKeySet();
    }

    public void runStart(final Blueprint script) {
        nodes.stream().filter(n -> n instanceof StartGraphNode).forEach(node ->
                links.stream().filter(l -> l.getStart() == node.getOutputPinId()).forEach(l ->
                        runLink(script, l)));
    }

    public void runUpdate(final Blueprint script) {
        nodes.stream().filter(n -> n instanceof UpdateGraphNode).forEach(node ->
                links.stream().filter(l -> l.getStart() == node.getOutputPinId()).forEach(l ->
                        runLink(script, l)));
    }

    public <G extends GraphNode<?>> G createNode(final Class<G> nodeClass) {
        try {
            final G n = nodeClass.getConstructor(int.class).newInstance(nextNodeId++);
            this.nodes.add(n);
            return n;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void link(final int source, final int target) {
        final Link link = new Link(nextLinkId++, source, target);
        links.add(link);
    }

    public GraphNode<?> getNode(final int id) {
        return nodes.stream().filter(n -> n.getId() == id).findFirst().orElse(null);
    }

    public void runLink(final Blueprint script, final Link link) {
        final GraphNode<?> source = findNodeByOutput(link.getStart());
        final GraphNode<?> target = findNodeByInput(link.getEnd());
        if (source == null || target == null) return;

        if (!target.execute(script, this)) {
            final Set<Link> links = findLinksByOutput(target.getOutputPinId());
            for (final Link l : links) {
                runLink(script, l);
            }
        }
    }

    public Set<Link> findLinksByOutput(final int pin) {
        return links.stream().filter(l -> l.getStart() == pin).collect(Collectors.toSet());
    }

    public Set<Link> findLinksByInput(final int pin) {
        return links.stream().filter(l -> l.getEnd() == pin).collect(Collectors.toSet());
    }

    public GraphNode<?> findNodeByOutput(final int pin) {
        return nodes.stream().filter(n -> {
            if (n instanceof BooleanGraphNode) {
                final BooleanGraphNode b = (BooleanGraphNode) n;
                return b.getOutputPinFalse() == pin || b.getOutputPinTrue() == pin;
            }
            return n.getOutputPinId() == pin;
        }).findFirst().orElse(null);
    }

    public GraphNode<?> findNodeByInput(final int pin) {
        return nodes.stream().filter(n -> {
            if (n instanceof BooleanGraphNode) {
                final BooleanGraphNode b = (BooleanGraphNode) n;
                return b.getInputPinLeft() == pin || b.getInputPinRight() == pin;
            }
            return n.getInputPinId() == pin;
        }).findFirst().orElse(null);
    }

    public void unlink(final int id) {
        links.removeIf(l -> l.getId() == id);
    }

    public void deleteNode(final int id) {
        final GraphNode<?> node = getNode(id);
        nodes.remove(node);
    }

    public Collection<Link> getLinks() {
        return links;
    }

    public Collection<GraphNode<?>> getNodes() {
        return nodes;
    }

    public void save(final String ini) {
        this.ini = ini;
    }
}
