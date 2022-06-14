package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.math.Vector2;
import xyz.destiall.caramel.api.math.Vector3;
import xyz.destiall.caramel.api.texture.Mesh;
import xyz.destiall.caramel.api.physics.RigidBodyType;
import xyz.destiall.caramel.api.texture.Texture;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ImGuiUtils {

    private static final float width = 110f;

    public static void drawVec2Control(String label, Vector2f values) {
        drawVec2Control(label, values, 0.0f, width);
    }

    public static void drawVec2Control(String label, Vector2f values, float resetValue) {
        drawVec2Control(label, values, resetValue, width);
    }

    public static void drawVec2Control(String label, Vector2f values, float resetValue, float columnWidth) {
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
        if (ImGui.button("X", buttonSize.x, buttonSize.y)) {
            values.x = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesX = {values.x};
        ImGui.dragFloat("##x", vecValuesX, 0.1f);
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);
        if (ImGui.button("Y", buttonSize.x, buttonSize.y)) {
            values.y = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesY = {values.y};
        ImGui.dragFloat("##y", vecValuesY, 0.1f);
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.nextColumn();

        values.x = vecValuesX[0];
        values.y = vecValuesY[0];

        ImGui.popStyleVar();
        ImGui.columns(1);
        ImGui.popID();
    }

    public static void drawVec3Control(String label, Vector3f values) {
        drawVec3Control(label, values, 0.0f, width);
    }

    public static void drawVec3Control(String label, Vector3f values, float resetValue) {
        drawVec3Control(label, values, resetValue, width);
    }

    public static void drawVec3Control(String label, Vector3f values, float resetValue, float columnWidth) {
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
        if (ImGui.button("X", buttonSize.x, buttonSize.y)) {
            values.x = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesX = {values.x};
        ImGui.dragFloat("##X", vecValuesX, 0.1f);
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);
        if (ImGui.button("Y", buttonSize.x, buttonSize.y)) {
            values.y = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesY = {values.y};
        ImGui.dragFloat("##Y", vecValuesY, 0.1f);
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.25f, 0.8f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.2f, 0.35f, 0.9f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.25f, 0.8f, 1.0f);
        if (ImGui.button("Z", buttonSize.x, buttonSize.y)) {
            values.z = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesZ = {values.z};
        ImGui.dragFloat("##Z", vecValuesZ, 0.1f);
        ImGui.popItemWidth();
        ImGui.columns(1);

        values.x = vecValuesX[0];
        values.y = vecValuesY[0];
        values.z = vecValuesZ[0];

        ImGui.popStyleVar();
        ImGui.popID();
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

    public static boolean colorPicker4(String label, Vector4f color) {
        boolean res = false;
        ImGui.pushID(label);

        ImGui.columns(2);
        ImGui.setColumnWidth(0, width);
        ImGui.text(label);
        ImGui.nextColumn();

        float[] imColor = {color.x, color.y, color.z, color.w};
        if (ImGui.colorEdit4("##colorPicker", imColor)) {
            color.set(imColor[0], imColor[1], imColor[2], imColor[3]);
            res = true;
        }

        ImGui.columns(1);
        ImGui.popID();

        return res;
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

    public static void drawQuatControl(String label, Quaternionf values, float resetValue) {
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
        if (ImGui.button("X", buttonSize.x, buttonSize.y)) {
            values.x = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesX = {values.x};
        ImGui.dragFloat("##X", vecValuesX, 0.1f);
        ImGui.popItemWidth();
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);
        if (ImGui.button("Y", buttonSize.x, buttonSize.y)) {
            values.y = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesY = {values.y};
        ImGui.dragFloat("##Y", vecValuesY, 0.1f);
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.25f, 0.8f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.2f, 0.35f, 0.9f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.25f, 0.8f, 1.0f);
        if (ImGui.button("Z", buttonSize.x, buttonSize.y)) {
            values.z = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesZ = {values.z};
        ImGui.dragFloat("##Z", vecValuesZ, 0.1f);
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.sameLine();

        ImGui.pushItemWidth(widthEach);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.25f, 0.8f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.2f, 0.35f, 0.9f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.1f, 0.25f, 0.8f, 1.0f);
        if (ImGui.button("W", buttonSize.x, buttonSize.y)) {
            values.w = resetValue;
        }
        ImGui.popStyleColor(3);

        ImGui.sameLine();
        float[] vecValuesW = {values.w};
        ImGui.dragFloat("##W", vecValuesW, 0.1f);
        ImGui.popItemWidth();
        ImGui.columns(1);

        values.x = vecValuesX[0];
        values.y = vecValuesY[0];
        values.z = vecValuesZ[0];
        values.w = vecValuesW[0];

        ImGui.popStyleVar();
        ImGui.popID();
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

    public static void imguiLayer(Field field, Component component) {
        try {
            Class<?> type = field.getType();
            Object value = field.get(component);
            String name = field.getName();

            if (type == boolean.class) {
                field.setBoolean(component, ImGuiUtils.drawCheckBox(name, (boolean) value));

            } else if (type == int.class) {
                field.setInt(component, ImGuiUtils.dragInt(name, (int) value));

            } else if (type == float.class) {
                field.setFloat(component, ImGuiUtils.dragFloat(name, (float) value));

            } else if (type == String.class) {
                field.set(component, ImGuiUtils.inputText(name, (String) value));

            } else if (type == Vector3f.class) {
                ImGuiUtils.drawVec3Control(name, (Vector3f) value, 1f);

            } else if (type == Vector3.class) {
                ImGuiUtils.drawVec3Control(name, ((Vector3) value).getJoml(), 1f);

            } else if (type == Vector2f.class) {
                ImGuiUtils.drawVec2Control(name, (Vector2f) value, 1f);

            } else if (type == Vector2.class) {
                ImGuiUtils.drawVec2Control(name, ((Vector2) value).getJoml(), 1f);

            } else if (type == Quaternionf.class) {
                ImGuiUtils.drawQuatControl(name, (Quaternionf) value, 0.f);

            } else if (type == Mesh.class) {
                Mesh mesh = (Mesh) value;
                ImGui.text("shader: " + mesh.getShader().getPath());
                if (mesh.getColor() != null) {
                    ImGuiUtils.colorPicker4("color", mesh.getColor());
                }
                String string = ImGuiUtils.inputText("texture:", mesh.getTexture() == null ? "" : mesh.getTexture().getPath());
                ImGui.sameLine();
                if (ImGui.button("apply")) {
                    if (mesh.getTexture() == null || !mesh.getTexture().getPath().equalsIgnoreCase(string)) {
                        Texture texture = new Texture(string);
                        texture.buildTexture();
                        if (texture.isLoaded()) {
                            mesh.setTexture(texture);
                        }
                    }
                }
            } else if (type == RigidBodyType.class) {
                for (RigidBodyType bodyType : RigidBodyType.values()) {
                    if (ImGuiUtils.drawCheckBox(bodyType.name().toLowerCase(), field.get(component) == bodyType)) {
                        field.set(component, bodyType);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
