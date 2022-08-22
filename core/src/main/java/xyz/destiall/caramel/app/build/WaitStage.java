package xyz.destiall.caramel.app.build;

import caramel.api.debug.Debug;

public final class WaitStage implements Stage {
    private final Stage stageToExecute;
    private final int limit;
    private int counter = -1;

    public WaitStage(Stage stageToExecute, int limit) {
        this.stageToExecute = stageToExecute;
        this.limit = limit;
    }

    public WaitStage(Stage stageToExecute) {
        this(stageToExecute, 10);
    }

    @Override
    public Stage execute() {
        counter++;
        if (stageToExecute.isReady() || counter > limit) {
            return stageToExecute;
        }
        Debug.log("Waiting on " + stageToExecute.getName() + "...");
        return this;
    }
}
