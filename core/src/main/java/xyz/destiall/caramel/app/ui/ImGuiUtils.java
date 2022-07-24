package xyz.destiall.caramel.app.ui;

import caramel.api.Component;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.math.Vector2;
import caramel.api.math.Vector3;
import caramel.api.render.Animation;
import caramel.api.texture.Mesh;
import caramel.api.texture.Spritesheet;
import caramel.api.texture.Texture;
import caramel.api.utils.Color;
import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.callback.ImGuiFileDialogPaneFun;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ImGuiUtils {
    private static final float width = 110f;
    public static final boolean USE_IMGUI_FILE_CHOOSER = false;

    public static boolean drawVec2Control(String label, Vector2f values) {
        return drawVec2Control(label, values, 0.0f, width);
    }

    public static boolean drawVec2Control(String label, Vector2f values, float resetValue) {
        return drawVec2Control(label, values, resetValue, width);
    }

    public static boolean drawVec2Control(String label, Vector2f values, float resetValue, float columnWidth) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, columnWidth);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);

        float lineHeight = ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        Vector2f buttonSize = new Vector2f(lineHeight + 3.0f, lineHeight);
        float widthEach = (ImGui.calcItemWidth() - buttonSize.x * 2.0f) / 2.0f;

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.8f, 0.1f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.9f, 0.2f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.8f, 0.1f, 0.15f, 1.0f);
        boolean edited = false;

        if (ImGui.button("X", buttonSize.x, buttonSize.y)) {
            values.x = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesX = {values.x};
        if (ImGui.dragFloat("##x", vecValuesX, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);
        if (ImGui.button("Y", buttonSize.x, buttonSize.y)) {
            values.y = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesY = {values.y};
        if (ImGui.dragFloat("##y", vecValuesY, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.nextColumn();

        values.x = vecValuesX[0];
        values.y = vecValuesY[0];

        ImGui.popStyleVar();
        ImGui.columns(1);
        ImGui.popID();

        return edited;
    }

    public static boolean drawVec3Control(String label, Vector3f values) {
        return drawVec3Control(label, values, 0.0f, width);
    }

    public static boolean drawVec3Control(String label, Vector3f values, float resetValue) {
        return drawVec3Control(label, values, resetValue, width);
    }

    public static boolean drawVec3Control(String label, Vector3f values, float resetValue, float columnWidth) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, columnWidth);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);

        float lineHeight = ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        Vector2f buttonSize = new Vector2f(lineHeight + 3.0f, lineHeight);
        float widthEach = (ImGui.calcItemWidth() - buttonSize.x * 3.0f) / 3.0f;

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.8f, 0.1f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.9f, 0.2f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.8f, 0.1f, 0.15f, 1.0f);

        boolean edited = false;
        if (ImGui.button("X", buttonSize.x, buttonSize.y)) {
            values.x = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesX = {values.x};
        if (ImGui.dragFloat("##X", vecValuesX, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);
        if (ImGui.button("Y", buttonSize.x, buttonSize.y)) {
            values.y = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesY = {values.y};
        if (ImGui.dragFloat("##Y", vecValuesY, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.25f, 0.8f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.2f, 0.35f, 0.9f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.25f, 0.8f, 1.0f);
        if (ImGui.button("Z", buttonSize.x, buttonSize.y)) {
            values.z = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesZ = {values.z};
        if (ImGui.dragFloat("##Z", vecValuesZ, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.columns(1);

        values.x = vecValuesX[0];
        values.y = vecValuesY[0];
        values.z = vecValuesZ[0];

        ImGui.popStyleVar();
        ImGui.popID();

        return edited;
    }

    public static float dragFloat(String label, float value) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        float[] valArr = {value};
        ImGui.dragFloat("##dragFloat", valArr, 0.1f);

        ImGui.columns(1);
        ImGui.popID();

        return valArr[0];
    }

    public static int dragInt(String label, int value) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        int[] valArr = {value};
        ImGui.dragInt("##dragInt", valArr, 0.1f);

        ImGui.columns(1);
        ImGui.popID();

        return valArr[0];
    }

    public static boolean colorPicker4(String label, Color color) {
        boolean res = false;
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        float[] imColor = {color.r, color.g, color.b, color.a};
        if (ImGui.colorEdit4("##colorPicker", imColor)) {
            color.set(imColor[0], imColor[1], imColor[2], imColor[3]);
            res = true;
        }

        ImGui.columns(1);
        ImGui.popID();

        return res;
    }

    public static String inputText(String label, ImString outString) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.inputText("##" + label, outString);

        ImGui.columns(1);
        ImGui.popID();

        return outString.get();
    }

    public static String inputText(String label, String text) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        ImString outString = new ImString(text, 256);
        if (ImGui.inputText("##" + label, outString)) {
            ImGui.columns(1);
            ImGui.popID();

            return outString.get();
        }

        ImGui.columns(1);
        ImGui.popID();

        return text;
    }

    public static boolean drawQuatControl(String label, Quaternionf values, float resetValue) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);

        float lineHeight = ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        Vector2f buttonSize = new Vector2f(lineHeight + 3.0f, lineHeight);
        float widthEach = (ImGui.calcItemWidth() - buttonSize.x * 3.0f) / 3.0f;

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.8f, 0.1f, 0.15f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.9f, 0.2f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.8f, 0.1f, 0.15f, 1.0f);

        boolean edited = false;

        if (ImGui.button("X", buttonSize.x, buttonSize.y)) {
            values.x = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesX = {values.x};
        if (ImGui.dragFloat("##X", vecValuesX, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);
        if (ImGui.button("Y", buttonSize.x, buttonSize.y)) {
            values.y = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesY = {values.y};
        if (ImGui.dragFloat("##Y", vecValuesY, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.25f, 0.8f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.2f, 0.35f, 0.9f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.25f, 0.8f, 1.0f);
        if (ImGui.button("Z", buttonSize.x, buttonSize.y)) {
            values.z = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesZ = {values.z};
        if (ImGui.dragFloat("##Z", vecValuesZ, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.25f, 0.8f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.2f, 0.35f, 0.9f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.25f, 0.8f, 1.0f);
        if (ImGui.button("W", buttonSize.x, buttonSize.y)) {
            values.w = resetValue;
            edited = true;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesW = {values.w};
        if (ImGui.dragFloat("##W", vecValuesW, 0.1f)) {
            edited = true;
        }
        ImGui.popItemWidth();
        ImGui.columns(1);

        values.x = vecValuesX[0];
        values.y = vecValuesY[0];
        values.z = vecValuesZ[0];
        values.w = vecValuesW[0];

        ImGui.popStyleVar();
        ImGui.popID();

        return edited;
    }

    public static boolean drawCheckBox(String label, boolean value) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        ImBoolean imBoolean = new ImBoolean(value);
        ImGui.checkbox("##checkbox", imBoolean);
        ImGui.columns(1);
        ImGui.popID();

        return imBoolean.get();
    }

    public static int drawListSelectableBox(String label, int id, String[] items) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        ImInt imInt = new ImInt(id);
        if (ImGui.collapsingHeader(items[id])) {
            ImGui.listBox("##listselectablebox", imInt, items);
        }
        ImGui.columns(1);
        ImGui.popID();

        return imInt.get();
    }

    public static void drawList(String label, List<String> items) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        if (ImGui.treeNodeEx(label)) {
            if (ImGui.beginListBox("##list")) {
                for (String item : items) {
                    ImGui.text(item);
                }

                ImGui.endListBox();
            }
            ImGui.treePop();
        }


        ImGui.columns(1);
        ImGui.popID();
    }

    public static void imguiLayer(Field field, Component component) {
        try {
            Class<?> type = field.getType();
            Object value = field.get(component);
            String name = field.getName();
            String[] invokeMethods = null;
            if (field.isAnnotationPresent(InvokeOnEdit.class)) {
                invokeMethods = field.getAnnotation(InvokeOnEdit.class).value();
            }

            if (type == boolean.class) {
                boolean previous = (boolean) value;
                boolean now = drawCheckBox(name, previous);
                if (invokeMethods != null && previous != now) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }
                field.setBoolean(component, now);

            } else if (type == int.class) {
                int previous = (int) value;
                int now = dragInt(name, previous);
                if (invokeMethods != null && previous != now) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }
                field.setInt(component, now);

            } else if (type == float.class) {
                float previous = (float) value;
                float now = dragFloat(name, previous);
                if (invokeMethods != null && previous != now) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }
                field.setFloat(component, now);

            } else if (type == String.class) {
                String previous = (String) value;
                String now = inputText(name, previous);
                if (invokeMethods != null && previous.equals(now)) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }
                field.set(component, now);

            } else if (type == Vector3f.class) {
                if (drawVec3Control(name, (Vector3f) value, 1f) && invokeMethods != null) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }

            } else if (type == Vector3.class) {
                if (drawVec3Control(name, ((Vector3) value).getJoml(), 1f) && invokeMethods != null) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }

            } else if (type == Vector2f.class) {
                if (drawVec2Control(name, (Vector2f) value, 1f) && invokeMethods != null) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }

            } else if (type == Vector2.class) {
                if (drawVec2Control(name, ((Vector2) value).getJoml(), 1f) && invokeMethods != null) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }

            } else if (type == Color.class) {
               if (ImGuiUtils.colorPicker4(name, (Color) value) && invokeMethods != null) {
                   for (String method : invokeMethods) {
                       component.sendMessage(method);
                   }
               }

            } else if (type == Quaternionf.class) {
                if (drawQuatControl(name, (Quaternionf) value, 0.f) && invokeMethods != null) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }

            } else if (type == Mesh.class) {
                Mesh mesh = (Mesh) value;
                ImGui.text("shader: " + mesh.getShader().getPath());
                String path = findFile("texture", "Load Texture", ".png,.jpeg,.jpg");

                if (mesh.getTexture() != null) {
                    ImGui.sameLine();
                    ImGui.text(mesh.getTexturePath());
                }

                if (path != null) {
                    File absolute = new File(path);
                    String relative = new File("").toURI().relativize(absolute.toURI()).getPath();
                    if (Texture.getTexture(relative) != null) {
                        mesh.setTexture(path);
                    }
                }

            } else if (type == Spritesheet.class) {
                Spritesheet sheet = (Spritesheet) value;
                if (sheet != null) {
                    Map<String, Animation> map = sheet.getAnimations();
                    if (map != null) {
                        List<String> animation = map.values().stream().map(Animation::toString).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                        drawList(name, animation);
                    }
                }

            } else if (type == Texture.class) {
                Texture texture = (Texture) value;
                String path = findFile(name, "Load Texture", ".png,.jpeg,.jpg");

                if (texture != null) {
                    ImGui.sameLine();
                    ImGui.text(texture.getPath());
                }

                if (path != null) {
                    texture = Texture.getTexture(path);
                    if (texture.buildTexture()) {
                        field.set(component, texture);
                        if (invokeMethods != null) {
                            for (String method : invokeMethods) {
                                component.sendMessage(method);
                            }
                        }
                    }
                }

            } else if (type == File.class) {
                File file = (File) value;
                String path = findFile(name, ".*");

                if (file != null) {
                    ImGui.sameLine();
                    ImGui.text(file.getPath());
                }

                if (path != null) {
                    field.set(component, new File(path));
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type.isEnum()) {
                Enum<?>[] values = (Enum<?>[]) type.getMethod("values").invoke(null);
                String[] items = new String[values.length];
                int previousItem = 0;
                for (int i = 0; i < values.length; i++) {
                    items[i] = values[i].name();
                    if (field.get(component) == values[i]) {
                        previousItem = i;
                    }
                }
                int currentItem = drawListSelectableBox(name, previousItem, items);
                if (invokeMethods != null && currentItem != previousItem) {
                    for (String method : invokeMethods) {
                        component.sendMessage(method);
                    }
                }
                field.set(component, values[currentItem]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String findFile(String label, String button, String filter) {
        ImGui.pushID(label);
        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        if (ImGui.button(button)) {
            if (USE_IMGUI_FILE_CHOOSER) {
                ImGuiFileDialog.openModal(label, button, filter, ".", new ImGuiFileDialogPaneFun() {
                    @Override
                    public void paneFun(String filter, long userDatas, boolean canContinue) {}
                }, 250, 1, 42, ImGuiFileDialogFlags.None);

            } else {
                ImGui.columns(1);
                ImGui.popID();

                return openFileJava(button, filter);
            }
        }
        ImGui.columns(1);
        ImGui.popID();

        if (USE_IMGUI_FILE_CHOOSER) {
            if (ImGuiFileDialog.display(label, ImGuiFileDialogFlags.None, 800, 600, 800, 600)) {
                if (ImGuiFileDialog.isOk()) {
                    String path = ImGuiFileDialog.getFilePathName();
                    File absolute = new File(path);
                    String relative = new File("").toURI().relativize(absolute.toURI()).getPath();
                    ImGuiFileDialog.close();
                    return relative;
                }
                ImGuiFileDialog.close();
            }
        }

        return null;
    }

    public static String openFileJava(String title, String filter) {
        String[] extensions = filter.split(",");
        JFileChooser chooser = new FileChooser(title, extensions);

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            return new File("").toURI().relativize(file.toURI()).getPath();
        }
        return null;
    }

    public static String saveFileJava(String title, String filter) {
        String[] extensions = filter.split(",");
        JFileChooser chooser = new FileChooser(title, extensions);

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            return new File("").toURI().relativize(file.toURI()).getPath();
        }
        return null;
    }

    public static String findFile(String label, String filter) {
        return findFile(label, "Load File", filter);
    }

    public static void imguiLayer(Method method, Component component) {
        try {
            String name = method.getName();
            if (ImGui.button(name)) {
                method.invoke(component);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
