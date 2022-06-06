package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.callback.ImGuiFileDialogPaneFun;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiCond;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.app.editor.Scene;

public class MenuBarPanel extends Panel {
    private boolean openFile;

    public MenuBarPanel(Scene scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.beginMainMenuBar();
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        if (ImGui.beginMenu("File")) {
            if (ImGui.beginMenu("Open Script")) {
                ImGui.setNextWindowSize(800, 200, ImGuiCond.Once);
                ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
                ImGuiFileDialog.openModal("open-script", "Choose File", ".java", ".", new ImGuiFileDialogPaneFun() {
                    @Override
                    public void paneFun(String filter, long userDatas, boolean canContinue) {
                        ImGui.text("Filter: " + filter);
                    }
                }, 250, 1, 42, ImGuiFileDialogFlags.None);
                ImGui.endMenu();
            }
            if (ImGui.menuItem("Save", "CTRL+S")) {
                if (scene.isPlaying()) scene.stop();
                Application.getApp().saveCurrentScene();
            }
            if (ImGui.menuItem("Save As")) {
                ImGuiFileDialog.openModal("save-scene", "Save Scene As", ".json", ".", new ImGuiFileDialogPaneFun() {
                    @Override
                    public void paneFun(String filter, long userDatas, boolean canContinue) {
                        ImGui.text("Filter: " + filter);
                    }
                }, 250, 1, 42, ImGuiFileDialogFlags.None);
            }

            ImGui.endMenu();
        }

        if (ImGuiFileDialog.display("save-scene", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                System.out.println(ImGuiFileDialog.getSelection());
                System.out.println(ImGuiFileDialog.getUserDatas());
            }
            ImGuiFileDialog.close();
        }

        if (!scene.isPlaying() && ImGui.button("Play")) {
            scene.play();
        } else if (scene.isPlaying() && ImGui.button("Stop")) {
            scene.stop();
        }
        ImGui.endMainMenuBar();
    }
}
