package xyz.destiall.caramel.app.scripts;

import caramel.api.events.FileEvent;
import xyz.destiall.caramel.app.ApplicationImpl;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FileWatcher implements Runnable {
    private final Map<WatchKey, Path> keyMap = new ConcurrentHashMap<>();
    private final File folder;
    private final Scheduler scheduler;
    private Task watchTask;
    private WatchService watcher;

    public FileWatcher(final File folder) {
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

    private void register(final Path dir) throws IOException {
        if (keyMap.containsValue(dir)) {
            keyMap.entrySet().removeIf(en -> {
                if (en.getValue().equals(dir)) {
                    en.getKey().cancel();
                    return true;
                }
                return false;
            });
        }
        final WatchKey key = dir.register(watcher,  StandardWatchEventKinds.ENTRY_CREATE,  StandardWatchEventKinds.ENTRY_DELETE,  StandardWatchEventKinds.ENTRY_MODIFY);
        keyMap.put(key, dir);
    }

    private void registerAll(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private FileEvent lastEvent;
    private long lastMillis;

    @Override
    public void run() {
        try {
            final Path path = Paths.get(folder.getAbsolutePath());
            registerAll(path);
            for (;;) {
                final Set<String> polled = new HashSet<>();
                WatchKey watchKey;
                try {
                     watchKey = watcher.take();
                } catch (InterruptedException e) {
                    return;
                }
                final Path eventDir = keyMap.get(watchKey);
                if (eventDir == null || !eventDir.toFile().exists()) return;
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    final WatchEvent.Kind<?> kind = event.kind();
                    final Path eventPath = (Path) event.context();
                    final File toFile = eventPath.toFile();
                    FileEvent fileEvent;
                    if (polled.add(toFile.getPath())) {
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            fileEvent = new FileEvent(toFile, FileEvent.Type.CREATE);
                            try {
                                final Path child = eventDir.resolve(eventPath);
                                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                    registerAll(child);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            fileEvent = new FileEvent(toFile, FileEvent.Type.MODIFY);
                        } else {
                            fileEvent = new FileEvent(toFile, FileEvent.Type.DELETE);
                        }

                        if (fileEvent.equals(lastEvent) && lastMillis >= (System.currentTimeMillis() - 1000)) {
                            continue;
                        }
                        lastEvent = fileEvent;
                        lastMillis = System.currentTimeMillis();

                        ApplicationImpl.getApp().getEventHandler().call(fileEvent);
                    }
                }
                watchKey.reset();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
