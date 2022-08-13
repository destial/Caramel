package xyz.destiall.caramel.app.editor.managers;

import caramel.api.components.RigidBody2D;
import caramel.api.components.RigidBody3D;
import caramel.api.events.SceneStopEvent;
import caramel.api.objects.GameObject;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;

import java.util.Set;

public final class BodyManager implements Listener {

    @EventHandler
    public void onSceneStop(SceneStopEvent e) {
        for (GameObject go : e.getScene().getGameObjects()) {
            Set<RigidBody2D> rigid2dBodies = go.getComponentsInChildren(RigidBody2D.class);
            rigid2dBodies.addAll(go.getComponents(RigidBody2D.class));
            for (RigidBody2D rb : rigid2dBodies) {
                rb.rawBody = null;
            }

            Set<RigidBody3D> rigid3dBodies = go.getComponentsInChildren(RigidBody3D.class);
            rigid3dBodies.addAll(go.getComponents(RigidBody3D.class));
            for (RigidBody3D rb : rigid3dBodies) {
                rb.rawBody = null;
            }
        }
    }
}
