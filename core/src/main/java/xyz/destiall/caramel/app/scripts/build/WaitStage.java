package xyz.destiall.caramel.app.scripts.build;

public final class WaitStage implements Stage {
    private final Stage stageToExecute;
    public WaitStage(Stage stageToExecute) {
        this.stageToExecute = stageToExecute;
    }

    @Override
    public Stage execute() {
        return stageToExecute.isReady() ? stageToExecute : this;
    }
}
