package caramel.api.text;

import org.joml.Vector2f;

public final class CharInfo {
    public final Vector2f[] textureCoordinates = new Vector2f[4];
    public final int sourceX;
    public final int sourceY;
    public final int width;
    public final int height;
    public CharInfo(final int sourceX, final int sourceY, final int width, final int height) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.width = width;
        this.height = height;
    }

    public void calculateTextureCoordinates(final int fontWidth, final int fontHeight) {
        final float x0 = (float) sourceX / (float) fontWidth;
        final float x1 = (float) (sourceX + width) / (float) fontWidth;
        final float y0 = (float) (sourceY - height) / (float) fontHeight;
        final float y1 = (float) (sourceY) / (float) fontHeight;

        textureCoordinates[0] = new Vector2f(x0, y1);
        textureCoordinates[1] = new Vector2f(x1, y0);
    }
}
