package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ExtractStage implements Stage {
    private final File root;
    private final File output;
    private static final int BUFFER = 1024;

    public ExtractStage(File root, File output) {
        this.root = root;
        this.output = output;
    }
    @Override
    public Stage execute() {
        if (output.exists()) {
            FileIO.delete(output);
        }

        Debug.log("Extracting jar");
        // Copy assets & unzip internals inside jar
        try {
            FileIO.copy(new File("assets"), root, (f) -> !f.getName().contains("scripts"), true);
            String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            Debug.log(path);
            ZipInputStream coreZip = new ZipInputStream(Files.newInputStream(new File(path).toPath()));
            ZipEntry zipEntry = coreZip.getNextEntry();
            byte[] data = new byte[BUFFER];
            while (zipEntry != null) {
                File newFile = FileIO.extractFromZip(root, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        Debug.logError("Failed to create directory " + newFile);
                        continue;
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        Debug.logError("Failed to create directory " + parent);
                        continue;
                    }

                    Debug.log("Extracting " + zipEntry.getName());
                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
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

            // Re-write main class to runtime class.
            File manifest = new File(root, "META-INF" + File.separator + "MANIFEST.MF");
            String content = FileIO.readData(manifest);
            content = content.replace("xyz.destiall.caramel.app.Main", "xyz.destiall.caramel.app.MainRuntime");
            FileIO.writeData(manifest, content);

            File pom = new File(root, "META-INF" + File.separator + "maven" + File.separator + "xyz.destiall.gameengine" + File.separator + "build" + File.separator + "pom.xml");
            content = FileIO.readData(pom);
            content = content.replace("xyz.destiall.caramel.app.Main", "xyz.destiall.caramel.app.MainRuntime");
            FileIO.writeData(pom, content);
            Debug.console("Re-writing manifest...");
        } catch (Exception e) {
            Debug.logError("Error while unzipping internals: " + e.getMessage());
            e.printStackTrace();
        }

        return new JarStage(root, output);
    }
}
