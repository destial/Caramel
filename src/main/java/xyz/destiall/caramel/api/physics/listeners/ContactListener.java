package xyz.destiall.caramel.api.physics.listeners;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.components.Collider;
import xyz.destiall.caramel.app.editor.Scene;

public class ContactListener implements org.jbox2d.callbacks.ContactListener {
    private final Scene scene;

    public ContactListener(Scene scene) {
        this.scene = scene;
    }
    @Override
    public void beginContact(Contact contact) {
        RigidBody2D first = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureA().getBody()).findFirst().get().getComponent(RigidBody2D.class);
        RigidBody2D second = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureB().getBody()).findFirst().get().getComponent(RigidBody2D.class);

        for (Component component : first.gameObject.getComponents()) {
            if (component instanceof Contactable2D) {
                component.onCollisionEnter(second);
                ((Contactable2D) component).onCollisionTrigger(second.getComponent(Collider.class));
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        RigidBody2D first = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureA().getBody()).findFirst().get().getComponent(RigidBody2D.class);
        RigidBody2D second = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureB().getBody()).findFirst().get().getComponent(RigidBody2D.class);

        for (Component component : first.gameObject.getComponents()) {
            if (component instanceof Contactable2D) {
                component.onCollisionExit(second);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
