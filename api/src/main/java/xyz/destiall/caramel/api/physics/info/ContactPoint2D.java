package xyz.destiall.caramel.api.physics.info;

import org.jbox2d.dynamics.contacts.Contact;
import xyz.destiall.caramel.api.components.RigidBody2D;

public final class ContactPoint2D {
    private final Contact contact;
    private final RigidBody2D a, b;
    public ContactPoint2D(RigidBody2D a, RigidBody2D b, Contact contact) {
        this.a = a;
        this.b = b;
        this.contact = contact;
    }

    public RigidBody2D getA() {
        return a;
    }

    public RigidBody2D getB() {
        return b;
    }

    public Contact getContact() {
        return contact;
    }
}
