package scripts;

import caramel.api.objects.*;
import caramel.api.components.*;
import caramel.api.physics.components.*;
import caramel.api.interfaces.*;
import caramel.api.render.*;
import caramel.api.debug.*;
import caramel.api.audio.*;
import caramel.api.math.*;
import caramel.api.scripts.Script;

public class ButtonScript extends Script {
    private transient Button button;
    public ButtonScript(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {
        button = getComponent(Button.class);
        button.onClick.add(this::onClick);
    }

    // This method is called on every frame
    @Override
    public void update() {
        if (button == null) return;
    }

    public void onClick() {
        Debug.log("click");
    }
}