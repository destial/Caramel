package xyz.destiall.caramel.api.render;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.text.TextFont;
import xyz.destiall.caramel.api.texture.Mesh;
import xyz.destiall.caramel.api.texture.MeshBuilder;

public final class Text extends Renderer {

    private transient final Mesh mesh;
    @ShowInEditor
    private String text = "New text";

    public Text(GameObject gameObject) {
        super(gameObject);
        mesh = MeshBuilder.createQuad(1);
        TextFont font = new TextFont("assets/arial.TTF", 16);
        font.texture.buildBuffer();
        mesh.setTexture(font.texture);
        mesh.build();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void start() {

    }

    @Override
    public void render(Camera camera) {
        mesh.render(transform, camera);
    }
}
