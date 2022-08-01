package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Input;
import caramel.api.objects.SceneImpl;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import xyz.destiall.caramel.app.editor.nodes.BooleanGraphNode;
import xyz.destiall.caramel.app.editor.nodes.DebugGraphNode;
import xyz.destiall.caramel.app.editor.nodes.FloatGraphNode;
import xyz.destiall.caramel.app.editor.nodes.Graph;
import xyz.destiall.caramel.app.editor.nodes.GraphNode;
import xyz.destiall.caramel.app.editor.nodes.IntGraphNode;
import xyz.destiall.caramel.app.editor.nodes.Link;
import xyz.destiall.caramel.app.editor.nodes.MethodGraphNode;
import xyz.destiall.caramel.app.editor.nodes.StartGraphNode;
import xyz.destiall.caramel.app.editor.nodes.StringGraphNode;
import xyz.destiall.caramel.app.editor.nodes.UpdateGraphNode;

public final class NodePanel extends Panel {
    private Graph currentGraph;
    private final ImInt LINK_A = new ImInt();
    private final ImInt LINK_B = new ImInt();

    private static final String CREATE_NODE = "node_create_node";
    private static final String DELETE_NODE = "node_delete_node";
    private static final String DELETE_LINK = "node_delete_link";

    public NodePanel(SceneImpl scene) {
        super(scene);
    }

    public void setCurrentGraph(Graph graph) {
        this.currentGraph = graph;
        if (currentGraph.ini != null) {
            ImNodes.loadCurrentEditorStateFromIniString(currentGraph.ini, currentGraph.ini.length());
        }
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
        if (currentGraph == null) return;

        if (ImGui.begin("NodeEditor")) {
            Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
            Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());

            boolean save = ImGui.button("save");

            if (ImGui.button("close")) {
                currentGraph.save(ImNodes.saveCurrentEditorStateToIniString());
                currentGraph = null;
                ImGui.end();
                return;
            }

            ImGui.sameLine();
            ImNodes.beginNodeEditor();

            float nodeWidth = 100f;

            for (GraphNode<?> node : currentGraph.getNodes()) {
                ImNodes.beginNode(node.getId());

                ImNodes.beginNodeTitleBar();
                ImGui.pushItemWidth(nodeWidth);
                ImString name = node.getName().imString();
                ImGui.inputText("##rename", name, ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.NoHorizontalScroll);
                ImGui.popItemWidth();
                ImNodes.endNodeTitleBar();

                String input = "In";
                String output = "Out";

                if (!(node instanceof UpdateGraphNode) && !(node instanceof StartGraphNode)) {
                    if (node instanceof BooleanGraphNode) {
                        ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
                        ImGui.text("Run");
                        ImNodes.endInputAttribute();

                        ImNodes.beginInputAttribute(((BooleanGraphNode) node).getInputPinLeft(), ImNodesPinShape.CircleFilled);
                        ImGui.text("First");
                        ImNodes.endInputAttribute();

                        ImNodes.beginInputAttribute(((BooleanGraphNode) node).getInputPinRight(), ImNodesPinShape.CircleFilled);
                        ImGui.text("Second");

                    } else if (node instanceof IntGraphNode) {
                        ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
                        int[] ints = {((IntGraphNode) node).getValue()};
                        if (ImGui.dragInt(input, ints)) {
                            ((IntGraphNode) node).setValue(ints[0]);
                        }
                    } else {
                        ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
                        ImGui.text(input);
                    }

                    ImNodes.endInputAttribute();
                }

                if (node instanceof StringGraphNode) {
                    if (node.getValue() != null) {
                        ImString string = new ImString(((StringGraphNode) node).getValue());
                        ImGui.inputText("string", string);
                        ((StringGraphNode) node).setValue(string.get());
                    }
                } else if (node instanceof DebugGraphNode) {
                    ImString string = new ImString(((DebugGraphNode) node).getValue());
                    ImGui.inputText("debug", string);
                    ((DebugGraphNode) node).setValue(string.get());
                }

                if (!(node instanceof DebugGraphNode)) {
                    if (node instanceof BooleanGraphNode) {
                        ImNodes.beginOutputAttribute(((BooleanGraphNode) node).getOutputPinTrue());
                        ImVec2 vec2 = new ImVec2();
                        ImGui.calcTextSize(vec2, "true");
                        ImGui.indent(nodeWidth - vec2.x);
                        ImGui.text("true");
                        ImNodes.endOutputAttribute();

                        ImNodes.beginOutputAttribute(((BooleanGraphNode) node).getOutputPinFalse());
                        ImGui.calcTextSize(vec2, "false");
                        ImGui.indent(nodeWidth - vec2.x);
                        ImGui.text("false");
                    } else {
                        ImNodes.beginOutputAttribute(node.getOutputPinId());
                        ImVec2 vec2 = new ImVec2();
                        ImGui.calcTextSize(vec2, output);
                        ImGui.indent(nodeWidth - vec2.x);
                        ImGui.text(output);
                    }
                    ImNodes.endOutputAttribute();
                }

                ImNodes.endNode();
            }

            for (Link link : currentGraph.getLinks()) {
                ImNodes.link(link.getId(), link.getStart(), link.getEnd());
            }

            final boolean isEditorHovered = ImNodes.isEditorHovered();

            ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
            ImNodes.endNodeEditor();

            if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
                currentGraph.link(LINK_A.get(), LINK_B.get());
            }

            int[] nodeIds = new int[currentGraph.getNodes().size()];
            ImNodes.getSelectedNodes(nodeIds);
            int[] linkIds = new int[currentGraph.getNodes().size()];
            ImNodes.getSelectedLinks(linkIds);

            if (ImGui.isMouseClicked(Input.Mouse.RIGHT)) {
                final int hoveredNode = ImNodes.getHoveredNode();
                final int hoveredLink = ImNodes.getHoveredLink();
                if (hoveredNode != -1) {
                    ImGui.openPopup(DELETE_NODE);
                    ImGui.getStateStorage().setInt(ImGui.getID(DELETE_NODE), hoveredNode);
                } else if (hoveredLink != -1) {
                    ImGui.openPopup(DELETE_LINK);
                    ImGui.getStateStorage().setInt(ImGui.getID(DELETE_LINK), hoveredLink);
                } else if (isEditorHovered) {
                    ImGui.openPopup(CREATE_NODE);
                }
            }

            if (ImGui.isPopupOpen(DELETE_NODE)) {
                final int targetNode = ImGui.getStateStorage().getInt(ImGui.getID(DELETE_NODE));
                if (ImGui.beginPopup(DELETE_NODE)) {
                    GraphNode<?> node = currentGraph.getNode(targetNode);
                    if (node != null) {
                        if (ImGui.button("Delete " + node.getName())) {
                            currentGraph.deleteNode(targetNode);
                            ImGui.closeCurrentPopup();
                        }
                    }
                    ImGui.endPopup();
                }
            }

            if (ImGui.isPopupOpen(DELETE_LINK)) {
                final int targetLink = ImGui.getStateStorage().getInt(ImGui.getID(DELETE_LINK));
                if (ImGui.beginPopup(DELETE_LINK)) {
                    if (ImGui.button("Delete Link")) {
                        currentGraph.unlink(targetLink);
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }
            }

            if (ImGui.beginPopup(CREATE_NODE)) {
                if (ImGui.button("Create New Int Node")) {
                    final IntGraphNode node = currentGraph.createIntGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New Float Node")) {
                    final FloatGraphNode node = currentGraph.createFloatGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New String Node")) {
                    final StringGraphNode node = currentGraph.createStringGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New Boolean Node")) {
                    final BooleanGraphNode node = currentGraph.createBooleanGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New Method Node")) {
                    final MethodGraphNode node = currentGraph.createMethodGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New Start Node")) {
                    final StartGraphNode node = currentGraph.createStartGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New Update Node")) {
                    final UpdateGraphNode node = currentGraph.createUpdateGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                } else if (ImGui.button("Create New Debug Node")) {
                    final DebugGraphNode node = currentGraph.createDebugGraphNode();
                    ImNodes.setNodeScreenSpacePos(node.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                    ImGui.closeCurrentPopup();
                }
                ImGui.endPopup();
            }

            if (save) {
                currentGraph.save(ImNodes.saveCurrentEditorStateToIniString());
            }
        }
        ImGui.end();
    }
}
