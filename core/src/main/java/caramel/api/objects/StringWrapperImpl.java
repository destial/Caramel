package caramel.api.objects;

import caramel.api.interfaces.StringWrapper;
import imgui.type.ImString;

import java.util.Objects;

public final class StringWrapperImpl implements StringWrapper {
    private final ImString imString;

    public StringWrapperImpl(final String s) {
        imString = new ImString(s);
    }

    public StringWrapperImpl() {
        imString = new ImString();
    }

    public ImString imString() {
        return imString;
    }

    @Override
    public void set(String s) {
        imString.set(s);
    }

    @Override
    public String get() {
        return imString.get();
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o instanceof String) return Objects.equals(get(), o);
        if (o instanceof ImString) return Objects.equals(imString, o);
        return false;
    }
}
