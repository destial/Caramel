package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Input;
import caramel.api.objects.SceneImpl;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import xyz.destiall.caramel.app.editor.nodes.BooleanGraphNode;
import xyz.destiall.caramel.app.editor.nodes.DebugGraphNode;
import xyz.destiall.caramel.app.editor.nodes.FloatGraphNode;
import xyz.destiall.caramel.app.editor.nodes.Graph;
import xyz.destiall.caramel.app.editor.nodes.GraphNode;
import xyz.destiall.caramel.app.editor.nodes.IntegerGraphNode;
import xyz.destiall.caramel.app.editor.nodes.Link;
import xyz.destiall.caramel.app.editor.nodes.ComponentGraphNode;
import xyz.destiall.caramel.app.editor.nodes.StartGraphNode;
import xyz.destiall.caramel.app.editor.nodes.StringGraphNode;
import xyz.destiall.caramel.app.editor.nodes.UpdateGraphNode;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;

public final class NodePanel extends Panel {
    private Graph currentGraph;
    private final ImInt LINK_A = new ImInt();
    private final ImInt LINK_B = new ImInt();

    private static final String CREATE_NODE = "node_create_node";
    private static final String DELETE_NODE = "node_delete_node";
    private static final String DELETE_LINK = "node_delete_link";

    public NodePanel(final SceneImpl scene) {
        super(scene);
    }

    public void setCurrentGraph(final Graph graph) {
        this.currentGraph = graph;
        if (currentGraph.ini != null) {
            ImNodes.loadCurrentEditorStateFromIniString(currentGraph.ini, currentGraph.ini.length());
        }
    }

    public static String text(final String label, final String text) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, 50f);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.setColumnWidth(1, 75f);
        final ImString outString = new ImString(text, 256);
        ImGui.inputText("##" + label, outString);

        ImGui.columns(1);
        ImGui.popID();

        return outString.get();
    }

    @Override
    public void __imguiLayer() {
        if (currentGraph == null) return;

        int flags = 0;
        if (window.getScriptManager() instanceof EditorScriptManager && ((EditorScriptManager) window.getScriptManager()).isRecompiling()) {
            flags |= ImGuiWindowFlags.NoInputs | ImGuiWindowFlags.NoMouseInputs;
        }
        if (ImGui.begin("NodeEditor", flags)) {
            Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
            Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());

            final boolean save = ImGui.button("save");

            if (ImGui.button("close")) {
                currentGraph.save(ImNodes.saveCurrentEditorStateToIniString());
                currentGraph = null;
                ImGui.end();
                return;
            }

            ImGui.sameLine();
            ImNodes.beginNodeEditor();

            final float nodeWidth = 100f;

            for (final GraphNode<?> node : currentGraph.getNodes()) {
                ImNodes.beginNode(node.getId());

                ImNodes.beginNodeTitleBar();
                ImGui.pushItemWidth(nodeWidth);
                final ImString name = node.getName().imString();
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

                    } else if (node instanceof IntegerGraphNode) {
                        ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
                        final int[] ints = {((IntegerGraphNode) node).getValue()};
                        if (ImGui.dragInt(input, ints)) {
                            ((IntegerGraphNode) node).setValue(ints[0]);
                        }
                    } else {
                        ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
                        ImGui.text(input);
                    }

                    ImNodes.endInputAttribute();
                }

                if (node instanceof StringGraphNode) {
                    if (node.getValue() != null) {
                        final ImString string = new ImString(((StringGraphNode) node).getValue());
                        ImGui.inputText("string", string);
                        ((StringGraphNode) node).setValue(string.get());
                    }
                } else if (node instanceof DebugGraphNode) {
                    final ImString string = new ImString(((DebugGraphNode) node).getValue());
                    ImGui.inputText("debug", string);
                    ((DebugGraphNode) node).setValue(string.get());
                }

                if (!(node instanceof DebugGraphNode)) {
                    if (node instanceof BooleanGraphNode) {
                        ImNodes.beginOutputAttribute(((BooleanGraphNode) node).getOutputPinTrue());
                        final ImVec2 vec2 = new ImVec2();
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
                        final ImVec2 vec2 = new ImVec2();
                        ImGui.calcTextSize(vec2, output);
                        ImGui.indent(nodeWidth - vec2.x);
                        ImGui.text(output);
                    }
                    ImNodes.endOutputAttribute();
                }

                ImNodes.endNode();
            }

            for (final Link link : currentGraph.getLinks()) {
                ImNodes.link(link.getId(), link.getStart(), link.getEnd());
            }

            final boolean isEditorHovered = ImNodes.isEditorHovered();

            ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
            ImNodes.endNodeEditor();

            if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
                currentGraph.link(LINK_A.get(), LINK_B.get());
            }

            final int[] nodeIds = new int[currentGraph.getNodes().size()];
            ImNodes.getSelectedNodes(nodeIds);
            final int[] linkIds = new int[currentGraph.getNodes().size()];
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
                    final GraphNode<?> node = currentGraph.getNode(targetNode);
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
                for (final Class<? extends GraphNode<?>> node : Graph.NODES) {
                    if (ImGui.button("Create new " + Graph.getLabel(node) + " Node")) {
                        final GraphNode<?> graphNode = currentGraph.createNode(node);
                        if (graphNode == null) continue;
                        ImNodes.setNodeScreenSpacePos(graphNode.getId(), ImGui.getMousePosX(), ImGui.getMousePosY());
                        ImGui.closeCurrentPopup();;
                    }
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
