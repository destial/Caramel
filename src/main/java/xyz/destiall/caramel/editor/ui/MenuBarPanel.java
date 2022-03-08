package xyz.destiall.caramel.editor.ui;

import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.callback.ImGuiFileDialogPaneFun;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiCond;
import xyz.destiall.caramel.editor.Scene;

public class MenuBarPanel extends Panel {
    private boolean openFile;

    public MenuBarPanel(Scene scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.beginMainMenuBar();
        if (ImGui.button("File")) {
            openFile = !openFile;
        }
        if (openFile) {
            ImGui.nextColumn();
            ImGui.beginListBox("##file");
            if (ImGui.selectable("Open Script")) {
                ImGui.setNextWindowSize(800, 200, ImGuiCond.Once);
                ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
                ImGuiFileDialog.openModal("browse-key", "Choose File", ".java", ".", new ImGuiFileDialogPaneFun() {
                    @Override
                    public void paneFun(String filter, long userDatas, boolean canContinue) {
                        ImGui.text("Filter: " + filter);
                    }
                }, 250, 1, 42, ImGuiFileDialogFlags.None);
            }
            ImGui.endListBox();
        }
        if (!scene.isPlaying() && ImGui.button("Play")) {
            scene.play();
        } else if (scene.isPlaying() && ImGui.button("Stop")) {
            scene.stop();
        }
        ImGui.endMainMenuBar();
    }
}
