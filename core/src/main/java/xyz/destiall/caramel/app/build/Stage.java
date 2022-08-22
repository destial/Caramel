package xyz.destiall.caramel.app.build;

public interface Stage {
    default boolean isReady() {
        return true;
    }

    default String getName() {
        return getClass().getSimpleName();
    }
    Stage execute();
}
