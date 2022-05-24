package xyz.destiall.caramel.app.scripts;

import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.app.events.FileEvent;
import xyz.destiall.java.timer.Scheduler;
import xyz.destiall.java.timer.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileWatcher implements Runnable {
    private final Map<WatchKey, Path> keyMap = new HashMap<>();
    private final File folder;
    private final Scheduler scheduler;
    private Task watchTask;
    private WatchService watcher;

    public FileWatcher(File folder) {
        this.folder = folder;
        scheduler = new Scheduler();
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher,  StandardWatchEventKinds.ENTRY_CREATE,  StandardWatchEventKinds.ENTRY_DELETE,  StandardWatchEventKinds.ENTRY_MODIFY);
        keyMap.put(key, dir);
    }

    private void registerAll(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void run() {
        try {
            Path path = Paths.get(folder.getAbsolutePath());
            registerAll(path);
            for (;;) {
                WatchKey watchKey;
                try {
                     watchKey = watcher.take();
                } catch (InterruptedException e) {
                    return;
                }
                Path eventDir = keyMap.get(watchKey);
                if (eventDir == null || !eventDir.toFile().exists()) return;
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path eventPath = (Path) event.context();
                    FileEvent fileEvent;
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        fileEvent = new FileEvent(eventPath.toFile(), FileEvent.Type.CREATE);
                        try {
                            Path child = eventDir.resolve(eventPath);
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        fileEvent = new FileEvent(eventPath.toFile(), FileEvent.Type.MODIFY);
                    } else {
                        fileEvent = new FileEvent(eventPath.toFile(), FileEvent.Type.DELETE);
                    }

                    Application.getApp().getEventHandler().call(fileEvent);
                }
                watchKey.reset();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
