package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class JarStage implements Stage {
    private final File root;
    private final File output;
    private static final int BUFFER = 1024;

    public JarStage(File root, File output) {
        this.root = root;
        this.output = output;
    }

    @Override
    public Stage execute() {
        if (output.exists()) {
            FileIO.delete(output);
        }

        // Add all asset files
        List<File> files = new ArrayList<>(FileIO.traverse(new File("assets")).stream().filter(f -> !f.getName().equals("scripts") && !f.getName().endsWith(".java") && !f.getName().endsWith(".class")).collect(Collectors.toList()));

        // Unzip core and api jar inside internals
        try {
            String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            ZipInputStream coreZip = new ZipInputStream(new FileInputStream(path));
            ZipEntry zipEntry = coreZip.getNextEntry();
            byte[] data = new byte[BUFFER];
            while (zipEntry != null) {
                File newFile = newFile(root, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

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

            File manifest = new File(root, "META-INF" + File.separator + "MANIFEST.MF");
            String content = FileIO.readData(manifest);
            content = content.replace("xyz.destiall.caramel.app.Main", "xyz.destiall.caramel.app.MainRuntime");
            FileIO.writeData(manifest, content);

            File pom = new File(root, "META-INF" + File.separator + "maven" + File.separator + "xyz.destiall.gameengine" + File.separator + "build" + File.separator + "pom.xml");
            content = FileIO.readData(pom);
            content = content.replace("xyz.destiall.caramel.app.Main", "xyz.destiall.caramel.app.MainRuntime");
            FileIO.writeData(pom, content);
        } catch (Exception e) {
            Debug.logError("Error while unzipping internals: " + e.getMessage());
            e.printStackTrace();
        }

        // Add all internal files
        files.addAll(FileIO.traverse(root));

        if (files.size() == 0) {
            Debug.logError("Nothing to jar!");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(output);
            ZipOutputStream zos = new ZipOutputStream(fos);
            byte[] data = new byte[BUFFER];
            for (File file : files) {
                String path = file.toString();
                if (path.contains("temp")) {
                    path = path.replace("temp" + File.separator, "");
                }
                if (file.isDirectory()) {
                    ZipEntry ze = new ZipEntry(path + File.separator);
                    zos.putNextEntry(ze);
                    zos.closeEntry();
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis, BUFFER);
                    Debug.log("Compiling " + path);
                    ZipEntry ze = new ZipEntry(path);
                    zos.putNextEntry(ze);
                    int count;
                    while ((count = bis.read(data, 0, BUFFER)) != -1) {
                        zos.write(data, 0, count);
                    }
                    bis.close();
                    zos.closeEntry();
                }
            }
            zos.close();
        } catch(IOException e) {
            Debug.logError("Error while zipping: " + e.getMessage());
            e.printStackTrace();
        }

        return new CleanStage(root);
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
