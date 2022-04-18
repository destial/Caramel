package xyz.destiall.caramel.app.scripts;

import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.app.events.FileEvent;
import xyz.destiall.java.timer.Scheduler;
import xyz.destiall.java.timer.Task;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

public class FileWatcher implements Runnable {
    private final File folder;
    private final Scheduler scheduler;
    private Task watchTask;

    public FileWatcher(File folder) {
        this.folder = folder;
        scheduler = new Scheduler();
    }

    public void watch() {
        if (folder.exists()) {
           watchTask = scheduler.runTask(this);
        }
    }

    public void destroy() {
        watchTask.cancel();
        scheduler.cancelAll();
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(folder.getAbsolutePath());
            Map<WatchKey, Path> keyMap = new HashMap<>();
            keyMap.put(path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE), path);
            WatchKey watchKey = watchService.take();

            while (watchKey.reset() && Application.getApp().isRunning()) {
                Path eventDir = keyMap.get(watchKey);
                if (!eventDir.toFile().exists()) continue;
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path eventPath = (Path) event.context();
                    FileEvent fileEvent;
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) fileEvent = new FileEvent(eventPath.toFile(), FileEvent.Type.CREATE);
                    else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) fileEvent = new FileEvent(eventPath.toFile(), FileEvent.Type.MODIFY);
                    else fileEvent = new FileEvent(eventPath.toFile(), FileEvent.Type.DELETE);
                    Application.getApp().getEventHandler().call(fileEvent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
