package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Input;
import caramel.api.components.VisualScript;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.objects.SceneImpl;
import caramel.api.utils.Pair;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.type.ImInt;
import imgui.type.ImString;
import xyz.destiall.caramel.app.editor.nodes.GraphNode;
import xyz.destiall.caramel.app.editor.nodes.IntGraphNode;

import java.util.Map;

public final class NodePanel extends Panel {
    private VisualScript currentVisualScript;
    private final ImInt LINK_A = new ImInt();
    private final ImInt LINK_B = new ImInt();

    public NodePanel(SceneImpl scene) {
        super(scene);
        GameObject dummy = new GameObjectImpl(scene);
        currentVisualScript = new VisualScript(dummy);
    }

    public VisualScript getCurrentVisualScript() {
        return currentVisualScript;
    }

    public void setCurrentVisualScript(VisualScript currentVisualScript) {
        this.currentVisualScript = currentVisualScript;
    }

    public static String text(String label, String text) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, 50f);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.setColumnWidth(1, 75f);
        ImString outString = new ImString(text, 256);
        ImGui.inputText("##" + label, outString);

        ImGui.columns(1);
        ImGui.popID();

        return outString.get();
    }

    @Override
    public void __imguiLayer() {
        if (ImGui.begin("NodeEditor")) {
            Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
            Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
            text("text", "text");
            ImGui.sameLine();
            ImNodes.beginNodeEditor();

            for (GraphNode<?> node : currentVisualScript.nodes.values()) {
                ImNodes.beginNode(node.nodeId);

                ImNodes.beginNodeTitleBar();
                ImGui.text(node.getName());
                ImNodes.endNodeTitleBar();

                ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
                ImGui.text("In");
                ImNodes.endInputAttribute();

                ImGui.text(node.getValue().toString());

                ImNodes.beginOutputAttribute(node.getOutputPinId());
                ImGui.text("Out");
                ImNodes.endOutputAttribute();

                ImNodes.endNode();
            }

            for (Map.Entry<Integer, Pair<GraphNode<?>, GraphNode<?>>> link : currentVisualScript.links.entrySet()) {
                GraphNode<?> source = link.getValue().getKey();
                GraphNode<?> target = link.getValue().getValue();
                ImNodes.link(link.getKey(), source.outputNodeId, target.inputPinId);
            }

            final boolean isEditorHovered = ImNodes.isEditorHovered();

            ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
            ImNodes.endNodeEditor();

            if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
                final GraphNode<?> source = currentVisualScript.findByOutput(LINK_A.get());
                final GraphNode<?> target = currentVisualScript.findByInput(LINK_B.get());
                currentVisualScript.link(source, target);
            }

            int[] nodeIds = new int[currentVisualScript.nodes.size()];
            ImNodes.getSelectedNodes(nodeIds);
            int[] linkIds = new int[currentVisualScript.nodes.size()];
            ImNodes.getSelectedLinks(linkIds);

            if (ImGui.isMouseClicked(Input.Mouse.RIGHT)) {
                final int hoveredNode = ImNodes.getHoveredNode();
                final int hoveredLink = ImNodes.getHoveredLink();
                if (hoveredNode != -1) {
                    ImGui.openPopup("node_context");
                    ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), hoveredNode);
                } else if (hoveredLink != -1) {
                    ImGui.openPopup("link_context");
                    ImGui.getStateStorage().setInt(ImGui.getID("delete_link_id"), hoveredLink);
                } else if (isEditorHovered) {
                    ImGui.openPopup("node_editor_context");
                }
            }

            if (ImGui.isPopupOpen("node_context")) {
                final int targetNode = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"));
                if (ImGui.beginPopup("node_context")) {
                    if (ImGui.button("Delete " + currentVisualScript.nodes.get(targetNode).getName())) {
                        currentVisualScript.nodes.remove(targetNode);
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }
            }

            if (ImGui.isPopupOpen("link_context")) {
                final int targetLink = ImGui.getStateStorage().getInt(ImGui.getID("delete_link_id"));
                if (ImGui.beginPopup("link_context")) {
                    if (ImGui.button("Delete Link")) {
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }
            }

            if (ImGui.beginPopup("node_editor_context")) {
                if (ImGui.button("Create New Int Node")) {
                    final IntGraphNode node = currentVisualScript.createIntGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.nodeId, ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                }
                ImGui.endPopup();
            }
        }
        ImGui.end();
    }

}
