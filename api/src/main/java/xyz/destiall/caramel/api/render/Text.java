package xyz.destiall.caramel.api.render;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.text.TextBatch;
import xyz.destiall.caramel.api.text.TextFont;

public final class Text extends Renderer {
    private transient final TextBatch batch;
    @ShowInEditor
    private String text = "New text";

    public Text(GameObject gameObject) {
        super(gameObject);
        batch = new TextBatch(gameObject.scene);
        batch.initBatch();
        batch.font = new TextFont("assets/arial.TTF", 16);
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
        batch.addText(text, transform);
        batch.render(camera);
    }
}
