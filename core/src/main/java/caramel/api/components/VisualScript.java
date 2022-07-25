package caramel.api.components;

import caramel.api.Component;
import caramel.api.objects.GameObject;
import caramel.api.utils.Pair;
import xyz.destiall.caramel.app.editor.nodes.GraphNode;
import xyz.destiall.caramel.app.editor.nodes.IntGraphNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VisualScript extends Component {
    private int nextNodeId = 1;
    private int nextLinkId = 1;
    private int nextPinId = 100;
    public final Map<Integer, GraphNode<?>> nodes = new ConcurrentHashMap<>();
    public final Map<Integer, Pair<GraphNode<?>, GraphNode<?>>> links = new ConcurrentHashMap<>();

    public VisualScript(GameObject gameObject) {
        super(gameObject);
        createIntGraphNode();
        createIntGraphNode();
    }

    public IntGraphNode createIntGraphNode() {
        final IntGraphNode node = new IntGraphNode(nextNodeId++, nextPinId++, nextPinId++);
        this.nodes.put(node.nodeId, node);
        return node;
    }

    public int link(GraphNode<?> source, GraphNode<?> target) {
        if (source != null && target != null && source.outputNodeId != target.nodeId) {
            source.outputNodeId = target.nodeId;
            links.put(nextLinkId++, new Pair<>(source, target));
            return nextLinkId;
        }
        return -1;
    }

    public GraphNode<?> findByInput(final long inputPinId) {
        for (GraphNode<?> node : nodes.values()) {
            if (node.getInputPinId() == inputPinId) {
                return node;
            }
        }
        return null;
    }

    public GraphNode<?> findByOutput(final long outputPinId) {
        for (GraphNode<?> node : nodes.values()) {
            if (node.getOutputPinId() == outputPinId) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }
}
