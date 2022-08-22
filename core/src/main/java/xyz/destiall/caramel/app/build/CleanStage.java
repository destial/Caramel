package xyz.destiall.caramel.app.build;

import caramel.api.utils.FileIO;

import java.io.File;

public class CleanStage implements Stage {
    private final File output;

    public CleanStage(File output) {
        this.output = output;
    }

    @Override
    public Stage execute() {
        FileIO.delete(output);
        return null;
    }
}
