package xyz.destiall.caramel.app.editor.panels;

import caramel.api.debug.DebugImpl;
import caramel.api.objects.SceneImpl;
import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.callback.ImGuiFileDialogPaneFun;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.ui.ImGuiUtils;

import java.io.File;

public final class MenuBarPanel extends Panel {
    public MenuBarPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        ImGui.beginMainMenuBar();
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Open Scene")) {
                if (ImGuiUtils.USE_IMGUI_FILE_CHOOSER) {
                    ImGuiFileDialog.openModal("open-scene", "Open Scene", ".caramel", ".", new ImGuiFileDialogPaneFun() {
                        @Override
                        public void paneFun(String filter, long userDatas, boolean canContinue) {
                        }
                    }, 250, 1, 42, ImGuiFileDialogFlags.None);
                } else {
                    String path = ImGuiUtils.openFileJava("Open Scene", ".caramel");
                    if (path != null) {
                        File file = new File(path);
                        if (!file.exists()) {
                            DebugImpl.logError(file.getPath() + " does not exist!");
                        } else {
                            if (path.endsWith(".caramel")) {
                                if (scene.isPlaying()) scene.stop();
                                SceneImpl s = ApplicationImpl.getApp().loadScene(file);
                                ApplicationImpl.getApp().setTitle(s.name);
                            }
                        }
                    }
                }

            }

            if (ImGui.menuItem("Save", "Ctrl + S", false, !scene.isSaved())) {
                if (scene.isPlaying()) scene.stop();
                ApplicationImpl.getApp().saveCurrentScene();
            }

            if (ImGui.menuItem("Save As", "Ctrl + Shift + S")) {
                if (ImGuiUtils.USE_IMGUI_FILE_CHOOSER) {
                    ImGuiFileDialog.openModal("save-scene", "Save Scene As", ".caramel", ".", new ImGuiFileDialogPaneFun() {
                        @Override
                        public void paneFun(String filter, long userDatas, boolean canContinue) {}
                    }, 250, 1, 42, ImGuiFileDialogFlags.None);

                } else {
                    String path = ImGuiUtils.saveFileJava("Save Scene", ".caramel");
                    if (path != null) {
                        if (!path.toLowerCase().endsWith(".caramel")) {
                            path += ".caramel";
                        }
                        File file = new File(path);
                        String fileName = file.getName();
                        scene.name = fileName.substring(0, file.getName().length() - ".caramel".length());
                        ApplicationImpl.getApp().saveScene(scene, file);
                        ApplicationImpl.getApp().setTitle(scene.name);
                    }
                }
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Edit")) {
            if (ImGui.menuItem("Undo", "Ctrl + Z", false, !scene.isPlaying() && scene.canUndo())) {
                scene.undoLastAction();
            }
            if (ImGui.menuItem("Redo", "Ctrl + Y", false, !scene.isPlaying() && scene.canRedo())) {
                scene.redoLastAction();
            }

            ImGui.endMenu();
        }

        if (ImGuiUtils.USE_IMGUI_FILE_CHOOSER) {
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
        }

        if (!scene.isPlaying() && ImGui.button("Play")) {
            scene.play();
        } else if (scene.isPlaying() && ImGui.button("Stop")) {
            scene.stop();
        }
        ImGui.endMainMenuBar();
    }
}
