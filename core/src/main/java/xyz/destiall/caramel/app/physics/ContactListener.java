package xyz.destiall.caramel.app.physics;

import caramel.api.Component;
import caramel.api.components.RigidBody2D;
import caramel.api.debug.Debug;
import caramel.api.objects.Scene;
import caramel.api.physics.components.Collider;
import caramel.api.physics.components.Contactable2D;
import caramel.api.physics.info.ContactPoint2D;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

public final class ContactListener implements org.jbox2d.callbacks.ContactListener {
    private final Scene scene;

    public ContactListener(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void beginContact(Contact contact) {
        RigidBody2D first = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureA().getBody()).findFirst().get().getComponent(RigidBody2D.class);
        RigidBody2D second = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureB().getBody()).findFirst().get().getComponent(RigidBody2D.class);
        if (first.bounce || second.bounce) {
            contact.setRestitution(1.f);
        }
        ContactPoint2D contactPoint2D = new ContactPoint2D(first, second, contact);
        for (Component component : first.gameObject.getMutableComponents()) {
            try {
                component.onCollisionEnterRaw(contactPoint2D);
                component.onCollisionEnter(second);
                if (component instanceof Contactable2D) {
                    ((Contactable2D) component).onCollisionTrigger(second.getComponent(Collider.class));
                }
            } catch (Exception e) {
                Debug.log(e.getCause());
            }
        }
        for (Component component : second.gameObject.getMutableComponents()) {
            try {
                component.onCollisionEnterRaw(contactPoint2D);
                component.onCollisionEnter(first);
                if (component instanceof Contactable2D) {
                    ((Contactable2D) component).onCollisionTrigger(first.getComponent(Collider.class));
                }
            } catch (Exception e) {
                Debug.log(e.getCause());
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        RigidBody2D first = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureA().getBody()).findFirst().get().getComponent(RigidBody2D.class);
        RigidBody2D second = scene.getGameObjects().stream().filter(g -> g.hasComponent(RigidBody2D.class) && g.getComponent(RigidBody2D.class).rawBody == contact.getFixtureB().getBody()).findFirst().get().getComponent(RigidBody2D.class);
        ContactPoint2D contactPoint2D = new ContactPoint2D(first, second, contact);
        for (Component component : first.gameObject.getMutableComponents()) {
            try {
                component.onCollisionExitRaw(contactPoint2D);
                component.onCollisionExit(second);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Component component : second.gameObject.getMutableComponents()) {
            try {
                component.onCollisionExitRaw(contactPoint2D);
                component.onCollisionExit(first);
            } catch (Exception e) {
                e.printStackTrace();
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
