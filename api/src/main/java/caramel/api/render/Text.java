package caramel.api.render;

import caramel.api.components.Camera;
import caramel.api.interfaces.HideInEditor;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.text.TextFont;
import caramel.api.text.TextMesh;
import caramel.api.utils.Color;

public final class Text extends Renderer {

    @HideInEditor private transient TextMesh mesh;
    @HideInEditor private transient TextFont font;
    @ShowInEditor public String text = "New Text";

    @ShowInEditor
    @InvokeOnEdit("setColor")
    public Color color = new Color(1f, 1f, 1f, 1f);

    @InvokeOnEdit("updateFontSize")
    public int fontSize = 24;

    public Text(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void build() {}

    private void setColor() {
        mesh.setColor(color);
    }

    public String getText() {
        return text;
    }

    @Override
    public void start() {}

    private void updateFontSize() {
        if (font != null) {
            font.invalidate();
        }
        font = new TextFont("assets/fonts/arial.TTF", fontSize == 0 ? fontSize = 24 : fontSize);
        font.generateTexture().buildBuffer();
        mesh.setFont(font);
    }

    @Override
    public void render(Camera camera) {
        if (mesh == null) {
            mesh = new TextMesh();
            setColor();
            updateFontSize();
        }

        mesh.addText(text);
        mesh.render(transform, camera);
    }
}
