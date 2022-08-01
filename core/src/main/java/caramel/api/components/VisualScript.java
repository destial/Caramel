package caramel.api.components;

import caramel.api.Component;
import caramel.api.interfaces.FunctionButton;
import caramel.api.interfaces.HideInEditor;
import caramel.api.objects.GameObject;
import caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.editor.nodes.Graph;
import xyz.destiall.caramel.app.editor.panels.NodePanel;

public final class VisualScript extends Component {
    @HideInEditor public Graph graph;

    public VisualScript(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        getGraph().runStart(this);

    }

    @Override
    public void update() {
        getGraph().runUpdate(this);
    }

    @FunctionButton
    public void openNodePanel() {
        SceneImpl scene = (SceneImpl) gameObject.scene;
        scene.getEditorPanel(NodePanel.class).setCurrentGraph(getGraph());
    }

    public Graph getGraph() {
        if (graph == null) {
            graph = new Graph();
            graph.createStartGraphNode();
            graph.createUpdateGraphNode();
        }
        return graph;
    }
}
