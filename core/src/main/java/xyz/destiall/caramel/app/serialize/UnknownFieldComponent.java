package xyz.destiall.caramel.app.serialize;

import caramel.api.Component;

import java.lang.reflect.Field;

public final class UnknownFieldComponent {
    private final Field field;
    private final Component owningComponent;

    public UnknownFieldComponent(Field field, Component owningComponent) {
        this.field = field;
        this.owningComponent = owningComponent;
    }

    public Component getOwningComponent() {
        return owningComponent;
    }

    public Field getField() {
        return field;
    }
}
