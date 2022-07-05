package xyz.destiall.caramel.api.render;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.interfaces.HideInEditor;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.text.CharInfo;
import xyz.destiall.caramel.api.text.TextFont;
import xyz.destiall.caramel.api.texture.Mesh;

public final class Text extends Renderer {

    private transient final Mesh mesh;
    @ShowInEditor public String text;
    @HideInEditor private transient TextFont font;

    public Text(GameObject gameObject) {
        super(gameObject);
        mesh = new Mesh();
        font = new TextFont("assets/arial.TTF", 24);
        font.generateTexture().buildBuffer();
        mesh.setTexture(font.texture);
        mesh.build();
    }

    public void setText(String text) {
        mesh.resetArrays();
        mesh.resetIndices();
        float x = 0;
        float y0 = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                System.out.println("Unknown character " + c);
                continue;
            }

            float x0 = x;
            float x1 = x0 + charInfo.width;
            float y1 = y0 + charInfo.height;

            float ux0 = charInfo.textureCoordinates[0].x;
            float uy0 = charInfo.textureCoordinates[0].y;
            float ux1 = charInfo.textureCoordinates[1].x;
            float uy1 = charInfo.textureCoordinates[1].y;

            int index = i * 4;

            mesh.getVertex(index).texCoords.set(ux0, uy0);
            mesh.getVertex(index).position.set(x1, y0, 0);

            mesh.getVertex(index + 1).texCoords.set(ux0, uy1);
            mesh.getVertex(index + 1).position.set(x1, y1, 0);

            mesh.getVertex(index + 2).texCoords.set(ux1, uy0);
            mesh.getVertex(index + 2).position.set(x0, y0, 0);

            mesh.getVertex(index + 3).texCoords.set(ux1, uy1);
            mesh.getVertex(index + 3).position.set(x0, y1, 0);

            mesh.pushIndex(2, 1, 0, 0, 1, 3);

            x += charInfo.width;
        }
        mesh.setDirty(true);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void start() {

    }

    @FunctionButton
    public void updateText() {
        setText(text);
    }

    @Override
    public void render(Camera camera) {
        mesh.render(transform, camera);
    }
}
