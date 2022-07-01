package xyz.destiall.caramel.api.render;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.Render;
import xyz.destiall.caramel.api.texture.Mesh;

import java.util.LinkedList;
import java.util.List;

public abstract class Renderer extends Component implements Render {
    public Renderer(GameObject gameObject) {
        super(gameObject);
    }
}
