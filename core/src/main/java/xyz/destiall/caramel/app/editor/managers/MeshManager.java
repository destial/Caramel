package xyz.destiall.caramel.app.editor.managers;

import caramel.api.events.SceneStopEvent;
import caramel.api.objects.GameObject;
import caramel.api.render.MeshRenderer;
import caramel.api.render.SpriteRenderer;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;

import java.util.Set;

public final class MeshManager implements Listener {

    @EventHandler
    public void onSceneStop(final SceneStopEvent e) {
        for (final GameObject gameObject : e.getScene().getGameObjects()) {
            final Set<MeshRenderer> renderers = gameObject.getComponentsInChildren(MeshRenderer.class);
            renderers.addAll(gameObject.getComponents(MeshRenderer.class));
            for (final MeshRenderer renderer: renderers) {
                if (renderer.mesh == null) continue;
                renderer.mesh.invalidate();
            }
            final Set<SpriteRenderer> spriteRenderers = gameObject.getComponentsInChildren(SpriteRenderer.class);
            spriteRenderers.addAll(gameObject.getComponents(SpriteRenderer.class));
            for (final SpriteRenderer spriteRenderer : spriteRenderers) {
                if (spriteRenderer.spritesheet.mesh == null) continue;
                spriteRenderer.spritesheet.mesh.invalidate();
            }
        }
    }
}
