package xyz.destiall.caramel.app.build;

import caramel.api.debug.Debug;
import caramel.api.objects.Scene;
import caramel.api.objects.SceneImpl;
import caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.java.gson.JsonArray;
import xyz.destiall.java.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ExtractStage implements Stage {
    private final File root;
    private final File output;
    private static final int BUFFER = 1024;

    public ExtractStage(final File root, final File output) {
        this.root = root;
        this.output = output;
    }
    @Override
    public Stage execute() {
        // Delete the output jar, if it exists
        FileIO.delete(output);

        Debug.log("Extracting jar...");
        // Copy assets & unzip internals inside jar
        try {
            Debug.log("Copying assets...");
            FileIO.copy(new File("assets" + File.separator), root, (f) -> !f.getName().contains("scripts") && !f.getName().toLowerCase().endsWith(".caramel"), true);

            final String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            final ZipInputStream coreZip = new ZipInputStream(Files.newInputStream(new File(path).toPath()));
            ZipEntry zipEntry = coreZip.getNextEntry();
            byte[] data = new byte[BUFFER];
            while (zipEntry != null) {
                final File newFile = new File(root, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        Debug.logError("Failed to create directory " + newFile);
                        continue;
                    }
                } else {
                    // fix for Windows-created archives
                    final File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        Debug.logError("Failed to create directory " + parent);
                        continue;
                    }
                    // write file content
                    final FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = coreZip.read(data)) > 0) {
                        fos.write(data, 0, len);
                    }
                    fos.close();

                }
                zipEntry = coreZip.getNextEntry();
            }
            coreZip.closeEntry();
            coreZip.close();

            // Re-write main class to runtime class
            Debug.log("Re-writing manifest...");
            final File manifest = new File(root, "META-INF" + File.separator + "MANIFEST.MF");
            String content = FileIO.readData(manifest);
            content = content.replace("xyz.destiall.caramel.app.Main", "xyz.destiall.caramel.app.MainRuntime");
            FileIO.writeData(manifest, content);

            final File pom = new File(root, "META-INF" + File.separator + "maven" + File.separator + "xyz.destiall.gameengine" + File.separator + "build" + File.separator + "pom.xml");
            content = FileIO.readData(pom);
            content = content.replace("xyz.destiall.caramel.app.Main", "xyz.destiall.caramel.app.MainRuntime");
            FileIO.writeData(pom, content);

            // Copy scenes
            Debug.log("Copying scenes...");
            final JsonArray sceneData = new JsonArray();
            for (final SceneImpl scene : ApplicationImpl.getApp().getSceneLoader().getScenes()) {
                ApplicationImpl.getApp().getSceneLoader().saveScene(scene, scene.getFile());
                FileIO.copy(scene.getFile(), new File(root, scene.getFile().getName()), true);
                sceneData.add(scene.getFile().getName());
            }

            // Writing build config
            Debug.log("Writing build configuration...");
            final File config = new File(root, "config.json");
            final JsonObject object = new JsonObject();
            object.addProperty("width", ApplicationImpl.getApp().getWidth());
            object.addProperty("height", ApplicationImpl.getApp().getHeight());
            object.addProperty("windowPosX", ApplicationImpl.getApp().getWinPosX());
            object.addProperty("windowPosY", ApplicationImpl.getApp().getWinPosY());
            object.add("scenes", sceneData);
            FileIO.writeData(config, ApplicationImpl.getApp().getSerializer().toJson(object));
        } catch (Exception e) {
            Debug.logError("Error while unzipping internals: " + e.getMessage());
            e.printStackTrace();
            return new CleanStage(root);
        }

        return new PackStage(root, output);
    }
}
