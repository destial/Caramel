package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.callback.ImGuiFileDialogPaneFun;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import xyz.destiall.caramel.api.debug.DebugImpl;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.SceneImpl;

import java.io.File;

public class MenuBarPanel extends Panel {
    public MenuBarPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.beginMainMenuBar();
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Open Scene")) {
                ImGuiFileDialog.openModal("open-scene", "Open Scene", ".caramel", ".", new ImGuiFileDialogPaneFun() {
                    @Override
                    public void paneFun(String filter, long userDatas, boolean canContinue) {}
                }, 250, 1, 42, ImGuiFileDialogFlags.None);
            }
            if (ImGui.menuItem("Save", "Ctrl + S")) {
                if (scene.isPlaying()) scene.stop();
                ApplicationImpl.getApp().saveCurrentScene();
            }

            if (ImGui.menuItem("Save As")) {
                ImGuiFileDialog.openModal("save-scene", "Save Scene As", ".caramel", ".", new ImGuiFileDialogPaneFun() {
                    @Override
                    public void paneFun(String filter, long userDatas, boolean canContinue) {}
                }, 250, 1, 42, ImGuiFileDialogFlags.None);
            }
            if (ImGui.menuItem("Build", "Ctrl + B")) {

            }

            ImGui.endMenu();
        }

        if (ImGuiFileDialog.display("save-scene", ImGuiFileDialogFlags.None, 800, 600, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                File file = new File(ImGuiFileDialog.getFilePathName());

                String fileName = ImGuiFileDialog.getCurrentFileName();
                scene.name = fileName.substring(0, file.getName().length() - ImGuiFileDialog.getCurrentFilter().length());
                ApplicationImpl.getApp().saveScene(scene, file);
                ApplicationImpl.getApp().setTitle(scene.name);
            }
            ImGuiFileDialog.close();
        } else if (ImGuiFileDialog.display("open-scene", ImGuiFileDialogFlags.None, 800, 600, 800, 600)) {
            File file = new File(ImGuiFileDialog.getFilePathName());
            if (!file.exists()) {
                DebugImpl.logError(file.getPath() + " does not exist!");
            } else {
                if (scene.isPlaying()) scene.stop();
                SceneImpl s = ApplicationImpl.getApp().loadScene(file);
                ApplicationImpl.getApp().setTitle(s.name);
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