package scripts;

import caramel.api.*;
import caramel.api.objects.*;
import caramel.api.components.*;
import caramel.api.debug.*;
import caramel.api.audio.*;
import caramel.api.math.*;
import caramel.api.render.*;
import caramel.api.scripts.Script;

public class ScoreScript extends Script {
    private transient Text text;
    private int score;
    public ScoreScript(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {
        text = getComponent(Text.class);
        score = 0;
    }

    // This method is called on every frame
    @Override
    public void update() {
        if (text == null) return;

        if (Input.isKeyPressed(Input.Key.SPACE)) {
            text.text = "Score: " + (++score);
        }
    }
}