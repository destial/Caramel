package caramel.api.text;

import caramel.api.texture.Texture;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class TextFont {
    private final Map<Integer, CharInfo> characterMap;
    private final String filepath;
    private final int fontSize;

    public Texture texture;

    public TextFont(final String filepath, final int fontSize) {
        this.filepath = filepath;
        this.fontSize = fontSize;
        this.characterMap = new HashMap<>();
    }

    public CharInfo getCharacter(final int codepoint) {
        return characterMap.getOrDefault(codepoint, new CharInfo(0, 0, 0, 0));
    }

    public Texture generateTexture() {
        this.characterMap.clear();
        final Font font = new Font(filepath, Font.PLAIN, fontSize);

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final FontMetrics fontMetrics = g2d.getFontMetrics();

        final int estimatedWidth = (int)Math.sqrt(font.getNumGlyphs()) * font.getSize() + 1;
        int width = 0;
        int height = fontMetrics.getHeight();
        int x = 0;
        int y = (int)(fontMetrics.getHeight() * 1.4f);

        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                final CharInfo charInfo = new CharInfo(x, y, fontMetrics.charWidth(i), fontMetrics.getHeight());
                characterMap.put(i, charInfo);
                width = Math.max(x + fontMetrics.charWidth(i), width);

                x += charInfo.width;
                if (x > estimatedWidth) {
                    x = 0;
                    y += fontMetrics.getHeight() * 1.4f;
                    height += fontMetrics.getHeight() * 1.4f;
                }
            }
        }
        height += fontMetrics.getHeight() * 1.4f;
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                final CharInfo info = characterMap.get(i);
                info.calculateTextureCoordinates(width, height);
                g2d.drawString("" + (char)i, info.sourceX, info.sourceY);
            }
        }
        g2d.dispose();

        texture = uploadTexture(img);
        return texture;
    }

    public void invalidate() {
        if (texture != null) {
            texture.invalidate();
            characterMap.clear();
        }
    }

    private Texture uploadTexture(final BufferedImage image) {
        final int[] pixels = new int[image.getHeight() * image.getWidth()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x=0; x < image.getWidth(); x++) {
                final int pixel = pixels[y * image.getWidth() + x];
                final byte alphaComponent = (byte)((pixel >> 24) & 0xFF);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
            }
        }
        buffer.flip();

        final Texture texture = new Texture(image.getWidth(), image.getHeight(), buffer);
        texture.setPath(filepath);
        Texture.getTextures().add(texture);
        return texture;
    }
}
