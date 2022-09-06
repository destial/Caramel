package xyz.destiall.caramel.app.editor.panels;

import caramel.api.debug.DebugImpl;
import caramel.api.objects.SceneImpl;
import caramel.api.render.Shader;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;
import xyz.destiall.caramel.app.ui.ImGuiUtils;

import java.io.File;
import java.util.Map;

public final class MenuBarPanel extends Panel {
    private boolean shaderTools = false;
    public MenuBarPanel(final SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        if (ImGui.beginMainMenuBar()) {
            Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
            Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("New Scene")) {
                    final SceneImpl newScene = ApplicationImpl.getApp().getSceneLoader().newScene();
                    ApplicationImpl.getApp().setTitle(newScene.name);
                }

                if (ImGui.menuItem("Open Scene")) {
                    final String path = ImGuiUtils.openFileJava("Open Scene", ".caramel");
                    if (path != null) {
                        final File file = new File(path);
                        if (!file.exists()) {
                            DebugImpl.logError(file.getPath() + " does not exist!");
                        } else {
                            if (path.endsWith(".caramel")) {
                                if (scene.isPlaying()) scene.stop();
                                final SceneImpl s = ApplicationImpl.getApp().loadScene(file);
                                ApplicationImpl.getApp().setTitle(s.name);
                            }
                        }
                    }
                }

                if (ImGui.menuItem("Save", "Ctrl + S", false, !scene.isSaved())) {
                    if (scene.isPlaying()) scene.stop();
                    ApplicationImpl.getApp().saveCurrentScene();
                }

                if (ImGui.menuItem("Save As", "Ctrl + Shift + S")) {
                    String path = ImGuiUtils.saveFileJava("Save Scene", ".caramel");
                    if (path != null) {
                        if (!path.toLowerCase().endsWith(".caramel")) {
                            path += ".caramel";
                        }
                        final File file = new File(path);
                        final String fileName = file.getName();
                        scene.name = fileName.substring(0, file.getName().length() - ".caramel".length());
                        ApplicationImpl.getApp().saveScene(scene, file);
                        ApplicationImpl.getApp().setTitle(scene.name);
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

            if (ImGui.beginMenu("Tools")) {
                if (ImGui.menuItem("Shaders")) {
                    shaderTools = true;
                }
                ImGui.endMenu();
            }

            if (shaderTools && ImGui.begin("Shader Tools", ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoSavedSettings)) {
                for (final Map.Entry<String, Shader> entry : Shader.getShaders().entrySet()) {
                    if (ImGui.button("Recompile")) {
                        entry.getValue().recompile();
                    }
                    ImGui.sameLine();
                    ImGui.text(entry.getKey());
                }
                ImGui.separator();
                if (ImGui.button("Close")) {
                    shaderTools = false;
                }
                ImGui.end();
            }

            if (ImGui.button("Build")) {
                String path = ImGuiUtils.saveFileJava("Build", ".jar");
                if (path != null) {
                    if (!path.toLowerCase().endsWith(".jar")) {
                        path += ".jar";
                    }
                    final File out = new File(path);
                    ((EditorScriptManager) ApplicationImpl.getApp().getScriptManager()).build(out);
                }
            }

            if (!scene.isPlaying() && ImGui.button("Play")) {
                scene.play();
            } else if (scene.isPlaying() && ImGui.button("Stop")) {
                scene.stop();
            }
        }
        ImGui.endMainMenuBar();
    }
}
