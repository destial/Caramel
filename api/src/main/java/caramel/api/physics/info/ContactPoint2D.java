package caramel.api.physics.info;

import caramel.api.components.RigidBody2D;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Represents a contact between 2 {@link RigidBody2D}s.
 */
public final class ContactPoint2D {
    private final Contact contact;
    private final RigidBody2D a, b;
    public ContactPoint2D(final RigidBody2D a, final RigidBody2D b, final Contact contact) {
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
