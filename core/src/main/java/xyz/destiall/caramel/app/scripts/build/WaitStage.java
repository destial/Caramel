package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;

public final class WaitStage implements Stage {
    private final Stage stageToExecute;
    public WaitStage(Stage stageToExecute) {
        this.stageToExecute = stageToExecute;
    }

    @Override
    public Stage execute() {
        if (stageToExecute.isReady()) {
            return stageToExecute;
        }
        Debug.log("Waiting on " + stageToExecute.getClass().getSimpleName());
        return this;
    }
}
