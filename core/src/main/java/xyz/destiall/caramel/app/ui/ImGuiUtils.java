package xyz.destiall.caramel.app.ui;

import caramel.api.Component;
import caramel.api.interfaces.FileExtensions;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.math.Vector2;
import caramel.api.math.Vector3;
import caramel.api.objects.GameObject;
import caramel.api.objects.SceneImpl;
import caramel.api.render.Animation;
import caramel.api.texture.Spritesheet;
import caramel.api.texture.Texture;
import caramel.api.texture.mesh.Mesh;
import caramel.api.utils.Color;
import caramel.api.utils.FileIO;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NFDPathSet;
import org.lwjgl.util.nfd.NativeFileDialog;
import xyz.destiall.caramel.app.editor.action.EditorAction;
import xyz.destiall.java.reflection.Reflect;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY;

public final class ImGuiUtils {
    private static final float width = 110f;

    public static boolean drawVec2Control(String label, Vector2f values) {
        return drawVec2Control(label, values, 0.0f, width);
    }

    public static boolean drawVec2Control(String label, Vector2f values, float resetValue) {
        return drawVec2Control(label, values, resetValue, width);
    }

    public static boolean drawVec2Control(final String label, final Vector2f values, final float resetValue, final float columnWidth) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, columnWidth);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);

        final float lineHeight = ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        final Vector2f buttonSize = new Vector2f(lineHeight + 3.0f, lineHeight);
        final float widthEach = (ImGui.calcItemWidth() - buttonSize.x * 2.0f) / 2.0f;

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
        final float[] vecValuesX = {values.x};
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
        final float[] vecValuesY = {values.y};
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

        final float lineHeight = ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        final Vector2f buttonSize = new Vector2f(lineHeight + 3.0f, lineHeight);
        final float widthEach = (ImGui.calcItemWidth() - buttonSize.x * 3.0f) / 3.0f;

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
        final float[] vecValuesX = {values.x};
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
        final float[] vecValuesY = {values.y};
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
        final float[] vecValuesZ = {values.z};
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

        final float[] valArr = {value};
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

        final int[] valArr = {value};
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

        final float[] imColor = {color.r, color.g, color.b, color.a};
        if (ImGui.colorEdit4("##colorPicker", imColor)) {
            color.set(imColor[0], imColor[1], imColor[2], imColor[3]);
            res = true;
        }

        ImGui.columns(1);
        ImGui.popID();

        return res;
    }

    public static Component findComponent(String label, GameObject parent, Component previous, Class<? extends Component> clazz) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();
        final String id = clazz.getSimpleName() + "-" + parent.id;

        if (ImGui.button("find")) {
            ImGui.openPopup(id);
        }

        Component find = null;
        if (ImGui.isPopupOpen(id)) {
            if (ImGui.beginPopup(id)) {
                if (ImGui.beginListBox("##List Component")) {
                    final Set<Component> objects = new HashSet<>();
                    for (GameObject root : parent.scene.getGameObjects()) {
                        final Set<? extends Component> components = root.getComponents(clazz);
                        final Set<? extends Component> children = root.getComponentsInChildren(clazz);
                        objects.addAll(components);
                        objects.addAll(children);
                    }

                    for (Component component : objects) {
                        final String name = component.getClass().getSimpleName() + " (" + component.gameObject.name + ")";
                        if (ImGui.selectable(name)) {
                            find = component;
                            ImGui.closeCurrentPopup();
                            break;
                        }
                    }
                }
                if (ImGui.button("Close")) {
                    ImGui.closeCurrentPopup();
                }
                ImGui.endListBox();
            }
            ImGui.endPopup();
        }

        if (previous != null) {
            ImGui.sameLine();
            final String name = previous.getClass().getSimpleName() + " (" + previous.gameObject.name + ")";
            ImGui.text(name);
        }

        ImGui.columns(1);
        ImGui.popID();

        return find;
    }

    public static Mesh findMesh(String label, Mesh previous) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();
        final String id = "mesh_load";

        if (ImGui.button("load")) {
            ImGui.openPopup(id);
        }

        Mesh find = null;
        if (ImGui.isPopupOpen(id)) {
            if (ImGui.beginPopup(id)) {
                if (ImGui.beginListBox("##list mesh")) {
                    for (Class<? extends Mesh> clazz : Mesh.MESHES) {
                        final String name = clazz.getSimpleName().replace("Mesh", "");
                        if (ImGui.selectable(name)) {
                            find = (Mesh) Reflect.newInstance(clazz);
                            ImGui.closeCurrentPopup();
                            break;
                        }
                    }
                }
                if (ImGui.button("Close")) {
                    ImGui.closeCurrentPopup();
                }
                ImGui.endListBox();
            }
            ImGui.endPopup();
        }

        if (previous != null) {
            ImGui.sameLine();
            final String name = previous.name;
            ImGui.text(name);
        }

        ImGui.columns(1);
        ImGui.popID();

        return find;
    }

    public static String inputText(String label, ImString outString, float width) {
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

    public static String inputText(String label, ImString outString) {
        return inputText(label, outString, width);
    }

    public static String inputText(String label, String text, float width) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        final ImString outString = new ImString(text, 256);
        if (ImGui.inputText("##" + label, outString)) {
            ImGui.columns(1);
            ImGui.popID();

            return outString.get();
        }

        ImGui.columns(1);
        ImGui.popID();

        return text;
    }

    public static String inputText(String label, String text) {
        return inputText(label, text, width);
    }

    public static boolean drawQuatControl(String label, Quaternionf values, float resetValue) {
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);

        final float lineHeight = ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        final Vector2f buttonSize = new Vector2f(lineHeight + 3.0f, lineHeight);
        final float widthEach = (ImGui.calcItemWidth() - buttonSize.x * 3.0f) / 3.0f;

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
        final float[] vecValuesX = {values.x};
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
        final float[] vecValuesY = {values.y};
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
        final float[] vecValuesZ = {values.z};
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
        final float[] vecValuesW = {values.w};
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

        final ImBoolean imBoolean = new ImBoolean(value);
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

        final ImInt imInt = new ImInt(id);
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
            final Class<?> type = field.getType();
            final Object value = field.get(component);
            final String name = field.getName();
            final SceneImpl scene = (SceneImpl) component.gameObject.scene;

            final String[] invokeMethods = field.isAnnotationPresent(InvokeOnEdit.class) ? field.getAnnotation(InvokeOnEdit.class).value() : null;

            if (type == boolean.class) {
                final boolean previous = (boolean) value;
                final boolean now = drawCheckBox(name, previous);
                if (previous != now) {
                    final EditorAction action = new EditorAction(scene) {
                        @Override
                        public void undo() {
                            try {
                                field.setBoolean(component, previous);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.setBoolean(component, now);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }
                field.setBoolean(component, now);

            } else if (type == int.class) {
                final int previous = (int) value;
                final int now = dragInt(name, previous);
                if (previous != now) {
                    final EditorAction action = new EditorAction(scene) {
                        @Override
                        public void undo() {
                            try {
                                field.setInt(component, previous);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.setInt(component, now);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }
                field.setInt(component, now);

            } else if (type == float.class) {
                final float previous = (float) value;
                final float now = dragFloat(name, previous);
                if (previous != now) {
                    final EditorAction action = new EditorAction(scene) {
                        @Override
                        public void undo() {
                            try {
                                field.setFloat(component, previous);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.setFloat(component, now);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }
                field.setFloat(component, now);

            } else if (type == String.class) {
                final String previous = (String) value;
                String now = inputText(name, previous);
                if (previous != null && previous.equals(now)) {
                    final EditorAction action = new EditorAction(scene) {
                        @Override
                        public void undo() {
                            try {
                                field.set(component, previous);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.set(component, now);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }
                field.set(component, now);

            } else if (type == Vector3f.class) {
                final Vector3f previous = new Vector3f((Vector3f) value);
                if (drawVec3Control(name, (Vector3f) value, 1f)) {
                    final EditorAction action = new EditorAction(scene) {
                        final Vector3f redo = new Vector3f();
                        @Override
                        public void undo() {
                            redo.set((Vector3f) value);
                            ((Vector3f) value).set(previous);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }

                        @Override
                        public void redo() {
                            ((Vector3f) value).set(redo);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type == Vector3.class) {
                final Vector3 previous = new Vector3((Vector3) value);
                if (drawVec3Control(name, ((Vector3) value).getJoml(), 1f)) {
                    final EditorAction action = new EditorAction(scene) {
                        final Vector3 redo = new Vector3();
                        @Override
                        public void undo() {
                            redo.set((Vector3) value);
                            ((Vector3) value).set(previous);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }

                        @Override
                        public void redo() {
                            ((Vector3) value).set(redo);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type == Vector2f.class) {
                final Vector2f previous = new Vector2f((Vector2f) value);
                if (drawVec2Control(name, (Vector2f) value, 1f)) {
                    final EditorAction action = new EditorAction(scene) {
                        final Vector2f redo = new Vector2f();
                        @Override
                        public void undo() {
                            redo.set((Vector2f) value);
                            ((Vector2f) value).set(previous);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }

                        @Override
                        public void redo() {
                            ((Vector2f) value).set(redo);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type == Vector2.class) {
                final Vector2 previous = new Vector2((Vector2) value);
                if (drawVec2Control(name, ((Vector2) value).getJoml(), 1f)) {
                    final EditorAction action = new EditorAction(scene) {
                        final Vector2 redo = new Vector2();
                        @Override
                        public void undo() {
                            redo.set((Vector2) value);
                            ((Vector2) value).set(previous);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }

                        @Override
                        public void redo() {
                            ((Vector2) value).set(redo);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type == Color.class) {
                final Color previous = new Color((Color) value);
                if (ImGuiUtils.colorPicker4(name, (Color) value)) {
                    final EditorAction action = new EditorAction(scene) {
                        final Color redo = new Color();
                        @Override
                        public void undo() {
                            redo.set((Color) value);
                            ((Color) value).set(previous);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }

                        @Override
                        public void redo() {
                            ((Color) value).set(redo);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
               }

            } else if (type == Quaternionf.class) {
                final Quaternionf previous = new Quaternionf((Quaternionf) value);
                if (drawQuatControl(name, (Quaternionf) value, 0.f)) {
                    final EditorAction action = new EditorAction(scene) {
                        final Quaternionf redo = new Quaternionf();
                        @Override
                        public void undo() {
                            redo.set((Quaternionf) value);
                            ((Quaternionf) value).set(previous);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }

                        @Override
                        public void redo() {
                            ((Quaternionf) value).set(redo);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type == Mesh.class) {
                Mesh mesh = (Mesh) value;
                ImGui.text("shader: " + mesh.getShader().getPath());
                final Mesh newMesh = findMesh(name, mesh);
                if (newMesh != null) {
                    newMesh.setTexture(mesh.getTexture() != null ? mesh.getTexturePath() : null);
                    newMesh.build();
                    final Mesh finalMesh = mesh;
                    final EditorAction action = new EditorAction(scene) {
                        private final Mesh redo = finalMesh;
                        @Override
                        public void undo() {
                            try {
                                field.set(component, redo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.set(component, newMesh);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    field.set(component, newMesh);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                    mesh = newMesh;
                }

                final String path = findFile("texture", "Load Texture", ".png,.jpeg,.jpg");

                if (mesh.getTexture() != null) {
                    ImGui.sameLine();
                    ImGui.text(mesh.getTexturePath());
                }

                final Mesh finalMesh = mesh;
                if (path != null) {
                    final String previous = mesh.getTexture() != null ? mesh.getTexturePath() : null;
                    if (path.isEmpty()) {
                        final EditorAction action = new EditorAction(scene) {
                            @Override
                            public void undo() {
                                try {
                                    finalMesh.setTexture(previous);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void redo() {
                                try {
                                    finalMesh.setTexture(null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        scene.addUndoAction(action);
                        mesh.setTexture(null);
                        if (invokeMethods != null) {
                            for (String method : invokeMethods) {
                                component.sendMessage(method);
                            }
                        }
                    } else {
                        final File absolute = new File(path);
                        final String relative = FileIO.relativize(absolute);
                        if (Texture.getTexture(relative) != null) {
                            final EditorAction action = new EditorAction(scene) {
                                @Override
                                public void undo() {
                                    try {
                                        finalMesh.setTexture(previous);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void redo() {
                                    try {
                                        finalMesh.setTexture(null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            scene.addUndoAction(action);
                            mesh.setTexture(path);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    }
                }

            } else if (type == Spritesheet.class) {
                final Spritesheet sheet = (Spritesheet) value;
                if (sheet != null) {
                    final Map<String, Animation> map = sheet.getAnimations();
                    if (map != null) {
                        List<String> animation = map.values().stream().map(Animation::toString).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                        drawList(name, animation);
                    }

                    final Mesh newMesh = findMesh("mesh", sheet.mesh);
                    if (newMesh != null) {
                        newMesh.setTexture(sheet.mesh.getTexture() != null ? sheet.mesh.getTexturePath() : null);
                        newMesh.build();
                        final Mesh finalMesh = sheet.mesh;
                        final EditorAction action = new EditorAction(scene) {
                            @Override
                            public void undo() {
                                sheet.mesh = newMesh;
                            }

                            @Override
                            public void redo() {
                                sheet.mesh = finalMesh;
                            }
                        };
                        scene.addUndoAction(action);
                        sheet.mesh = newMesh;
                        if (invokeMethods != null) {
                            for (String method : invokeMethods) {
                                component.sendMessage(method);
                            }
                        }
                    }
                }

            } else if (type == Texture.class) {
                final Texture previous = (Texture) value;
                final String path = findFile(name, "Load Texture", ".png,.jpeg,.jpg");

                if (previous != null) {
                    ImGui.sameLine();
                    ImGui.text(previous.getPath());
                }

                if (path != null) {
                    if (path.isEmpty()) {
                        final EditorAction action = new EditorAction(scene) {
                            @Override
                            public void undo() {
                                try {
                                    field.set(component, previous);
                                    if (invokeMethods != null) {
                                        for (String method : invokeMethods) {
                                            component.sendMessage(method);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void redo() {
                                try {
                                    field.set(component, null);
                                    if (invokeMethods != null) {
                                        for (String method : invokeMethods) {
                                            component.sendMessage(method);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        scene.addUndoAction(action);
                        field.set(component, null);
                        if (invokeMethods != null) {
                            for (String method : invokeMethods) {
                                component.sendMessage(method);
                            }
                        }
                    } else {
                        final Texture texture = Texture.getTexture(path);
                        if (texture.buildTexture()) {
                            final EditorAction action = new EditorAction(scene) {
                                @Override
                                public void undo() {
                                    try {
                                        field.set(component, previous);
                                        if (invokeMethods != null) {
                                            for (String method : invokeMethods) {
                                                component.sendMessage(method);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void redo() {
                                    try {
                                        field.set(component, texture);
                                        if (invokeMethods != null) {
                                            for (String method : invokeMethods) {
                                                component.sendMessage(method);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            scene.addUndoAction(action);
                            field.set(component, texture);
                            if (invokeMethods != null) {
                                for (String method : invokeMethods) {
                                    component.sendMessage(method);
                                }
                            }
                        }
                    }
                }

            } else if (type == File.class) {
                final File previous = (File) value;
                final StringBuilder filter = new StringBuilder(".*");
                if (field.isAnnotationPresent(FileExtensions.class)) {
                    String[] values = field.getAnnotation(FileExtensions.class).value();
                    filter.delete(0, filter.length());
                    for (int i = 0; i < values.length; i++) {
                        String v = values[i];
                        filter.append(v);
                        if (i < values.length - 1) {
                            filter.append(",");
                        }
                    }
                }
                final String path = findFile(name, filter.toString());
                if (previous != null) {
                    ImGui.sameLine();
                    ImGui.text(previous.getPath());
                }
                if (path != null) {
                    if (path.isEmpty()) {
                        final EditorAction action = new EditorAction(scene) {
                            @Override
                            public void undo() {
                                try {
                                    field.set(component, previous);
                                    if (invokeMethods != null) {
                                        for (String method : invokeMethods) {
                                            component.sendMessage(method);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void redo() {
                                try {
                                    field.set(component, null);
                                    if (invokeMethods != null) {
                                        for (String method : invokeMethods) {
                                            component.sendMessage(method);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        scene.addUndoAction(action);
                        field.set(component, null);
                        if (invokeMethods != null) {
                            for (String method : invokeMethods) {
                                component.sendMessage(method);
                            }
                        }
                    } else {
                        final File now = new File(path);
                        final EditorAction action = new EditorAction(scene) {
                            @Override
                            public void undo() {
                                try {
                                    field.set(component, previous);
                                    if (invokeMethods != null) {
                                        for (String method : invokeMethods) {
                                            component.sendMessage(method);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void redo() {
                                try {
                                    field.set(component, now);
                                    if (invokeMethods != null) {
                                        for (String method : invokeMethods) {
                                            component.sendMessage(method);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        scene.addUndoAction(action);
                        field.set(component, now);
                        if (invokeMethods != null) {
                            for (String method : invokeMethods) {
                                component.sendMessage(method);
                            }
                        }
                    }
                }

            } else if (Component.class.isAssignableFrom(type)) {
                final Component previous = (Component) value;
                final Component find = findComponent(name, component.gameObject, previous, (Class<? extends Component>) type);
                if (find != null && find != previous) {
                    final EditorAction action = new EditorAction(scene) {
                        @Override
                        public void undo() {
                            try {
                                field.set(component, previous);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.set(component, find);
                                if (invokeMethods != null) {
                                    for (String method : invokeMethods) {
                                        component.sendMessage(method);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    field.set(component, find);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }

            } else if (type.isEnum()) {
                final Enum<?>[] values = (Enum<?>[]) type.getMethod("values").invoke(null);
                final String[] items = new String[values.length];
                int previousItem = 0;
                for (int i = 0; i < values.length; i++) {
                    items[i] = values[i].name();
                    if (field.get(component) == values[i]) {
                        previousItem = i;
                    }
                }
                final int currentItem = drawListSelectableBox(name, previousItem, items);
                if (currentItem != previousItem) {
                    final int finalPreviousItem = previousItem;
                    final EditorAction action = new EditorAction(scene) {
                        @Override
                        public void undo() {
                            try {
                                field.set(component, values[finalPreviousItem]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void redo() {
                            try {
                                field.set(component, values[currentItem]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    scene.addUndoAction(action);
                    field.set(component, values[currentItem]);
                    if (invokeMethods != null) {
                        for (String method : invokeMethods) {
                            component.sendMessage(method);
                        }
                    }
                }
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
        if (ImGui.button("Clear")) {
            ImGui.columns(1);
            ImGui.popID();
            return "";
        }
        ImGui.sameLine();
        if (ImGui.button(button)) {
            ImGui.columns(1);
            ImGui.popID();
            return openFileJava(button, filter);
        }
        ImGui.columns(1);
        ImGui.popID();

        return null;
    }

    public static String openFileJava(String title, String filter) {
        final NFDPathSet pointer = NFDPathSet.callocStack();
        final PointerBuffer pointerBuffer = PointerBuffer.create(pointer.address(), pointer.sizeof());
        final String[] extensions = filter.split(",");
        final String[] wrap = new String[extensions.length];
        int i = 0;
        for (String e : extensions) {
            wrap[i++] = e.substring(1);
        }
        final String f = String.join(",", wrap);
        final int result = NativeFileDialog.NFD_OpenDialog(f, System.getProperty("user.dir"), pointerBuffer);
        if (result == NFD_OKAY) {
            final File file = new File(pointerBuffer.getStringASCII());
            return FileIO.relativize(file);
        }
        return null;
    }

    public static String saveFileJava(String title, String filter) {
        final NFDPathSet pointer = NFDPathSet.calloc();
        final PointerBuffer pointerBuffer = PointerBuffer.create(pointer.address(), pointer.sizeof());
        final String[] extensions = filter.split(",");
        final String[] wrap = new String[extensions.length];
        int i = 0;
        for (String e : extensions) {
            wrap[i++] = e.substring(1);
        }
        final String f = String.join(",", wrap);
        final int result = NativeFileDialog.NFD_SaveDialog(f, System.getProperty("user.dir"), pointerBuffer);
        if (result == NFD_OKAY) {
            final File file = new File(pointerBuffer.getStringASCII());
            return FileIO.relativize(file);
        }
        return null;
    }

    public static String findFile(String label, String filter) {
        return findFile(label, "Load File", filter);
    }

    public static void imguiLayer(Method method, Component component) {
        try {
            final String name = method.getName();
            if (ImGui.button(name)) {
                method.invoke(component);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
