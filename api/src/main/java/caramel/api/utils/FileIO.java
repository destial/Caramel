package caramel.api.utils;

import caramel.api.Application;
import caramel.api.debug.Debug;
import caramel.api.scripts.InternalScript;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class FileIO {
    private FileIO() {}

    private static final String BASE =
            "package scripts;\n" +
            "\n" +
            "import caramel.api.objects.*;\n" +
            "import caramel.api.components.*;\n" +
            "import caramel.api.physics.components.*;\n" +
            "import caramel.api.interfaces.*;\n" +
            "import caramel.api.render.*;\n" +
            "import caramel.api.debug.*;\n" +
            "import caramel.api.audio.*;\n" +
            "import caramel.api.math.*;\n" +
            "import caramel.api.sound.*;\n" +
            "import caramel.api.text.*;\n" +
            "import caramel.api.texture.*;\n" +
            "import caramel.api.texture.mesh.*;\n" +
            "import caramel.api.utils.*;\n" +
            "import caramel.api.*;\n" +
            "import caramel.api.scripts.Script;\n" +
            "\n" +
            "public class ${name} extends Script {\n" +
            "    public ${name}(GameObject gameObject) {\n" +
            "        super(gameObject);\n" +
            "    }\n" +
            "\n" +
            "    // This method is called on the first frame\n" +
            "    @Override\n" +
            "    public void start() {\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    // This method is called on every frame\n" +
            "    @Override\n" +
            "    public void update() {\n" +
            "\n" +
            "    }\n" +
            "}";

    public static final URI ROOT = new File("").toURI();
    public static final File ROOT_FILE = new File(System.getProperty("user.dir"));
    private static final Pattern NAME_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z][A-Za-z0-9_$]*)");
    private static final Pattern NAME_FINAL_PATTERN = Pattern.compile("public\\s+final\\s+class\\s+([A-Za-z][A-Za-z0-9_$]*)");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([A-Za-z][A-Za-z0-9_$.]*)");

    public static String getPackage(String script) {
        String fullPackage = null;
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(script);
        if (packageMatcher.find()) {
            fullPackage = packageMatcher.group(1);
        }
        return fullPackage;
    }

    public static String getFullName(File file, String script) {
        String fullPackage = getPackage(script);

        Matcher nameMatcher = NAME_PATTERN.matcher(script);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (fullPackage == null) {
                return name;
            } else {
                return fullPackage + "." + name;
            }
        }

        nameMatcher = NAME_FINAL_PATTERN.matcher(script);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (fullPackage == null) {
                return name;
            } else {
                return fullPackage + "." + name;
            }
        }

        return "scripts." + file.getName().substring(0, file.getName().length() - ".java".length());
    }

    public static InternalScript writeScript(String className) {
        File scriptFolder = new File("assets/scripts/");
        if (!scriptFolder.exists()) scriptFolder.mkdir();
        String contents = BASE.replace("${name}", className);
        File scriptFile = new File(scriptFolder, className + ".java");
        if (scriptFile.exists()) return null;
        try {
            FileWriter write = new FileWriter(scriptFile);
            InternalScript internalScript = Application.getApp().getScriptManager().reloadScript(scriptFile, contents);

            BufferedWriter buffer = new BufferedWriter(write);
            buffer.write(contents);
            buffer.flush();
            buffer.close();
            return internalScript;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeData(File location, String contents) {
        try (FileWriter write = new FileWriter(location); BufferedWriter buffer = new BufferedWriter(write)) {
            buffer.write(contents);
            return true;
        } catch (Exception e) {
            Debug.log(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] loadResource(String path) {
        try {
            InputStream stream = FileIO.class.getResourceAsStream("/" + path);
            if (stream == null) return null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] b = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = stream.read(b)) > 0) {
                outputStream.write(b, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveResource(String path, String targetPath) {
        Path p = Paths.get(targetPath);
        try {
            if (!java.nio.file.Files.exists(p)) {
                File f = p.toFile();
                f.createNewFile();
                InputStream stream = FileIO.class.getResourceAsStream("/" + path);
                if (stream == null) return;
                Debug.console("Writing resource file " + path);
                OutputStream outputStream = new FileOutputStream(f);
                byte[] b = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = stream.read(b)) > 0) {
                    outputStream.write(b, 0, bytesRead);
                }
                stream.close();
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readData(File file) {
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder contents = new StringBuilder();
            while (scanner.hasNextLine()) {
                contents.append(scanner.nextLine()).append("\n");
            }
            return contents.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<File> traverse(File root) {
        List<File> list = new ArrayList<>();
        traverse(root, list);
        return list;

    }

    private static void traverse(File root, List<File> files) {
        File[] list = root.listFiles();
        if (list == null || list.length == 0) {
            return;
        }

        for (File f : list) {
            files.add(f);
            if (f.isDirectory()) {
                traverse(f, files);
            }
        }
    }

    public static void copy(File source, File destination) throws IOException {
        copy(source, destination, null, false);
    }

    public static void copy(File source, File destination, Predicate<File> filter) throws IOException {
        copy(source, destination, filter, false);
    }

    public static void copy(File source, File destination, boolean force) throws IOException {
        copy(source, destination, null, force);
    }

    public static void copy(File source, File destination, Predicate<File> filter, boolean force) throws IOException {
        if (filter == null || filter.test(source)) {
            if (!source.exists()) {
                throw new IllegalArgumentException("Source (" + source.getPath() + ") doesn't exist.");
            }

            if (!force && destination.exists()) {
                throw new IllegalArgumentException("Destination (" + destination.getPath() + ") exists.");
            }

            if (source.isDirectory()) {
                File destDir = new File(destination, source.getName() + File.separator);
                destDir.mkdir();
                copyDirectory(source, destDir, filter);
            } else {
                copyFile(source, destination, filter);
            }
        }
    }

    public static void copyDirectory(File source, File destination, Predicate<File> filter) throws IOException {
        if (filter == null || filter.test(source)) {
            destination.mkdirs();

            File[] files = source.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    copyDirectory(file, new File(destination, file.getName()), filter);
                } else {
                    copyFile(file, new File(destination, file.getName()), filter);
                }
            }
        }
    }

    public static void copyFile(File source, File destination, Predicate<File> filter) throws IOException {
        if (filter == null || filter.test(source)) {
            Files.copy(source, destination);
        }
    }

    public static boolean delete(File f) {
        return delete(f, null);
    }

    public static boolean delete(File f, Predicate<File> filter) {
        if (f.exists() && (filter == null || filter.test(f))) {
            if (f.isDirectory()) {
                for (File c : Objects.requireNonNull(f.listFiles())) {
                    delete(c, filter);
                }
            }

            return f.delete();
        }
        return false;
    }

    public static String readUTFFromZip(File source, String path) throws IOException {
        ZipFile file = new ZipFile(source);
        ZipInputStream coreZip = new ZipInputStream(java.nio.file.Files.newInputStream(source.toPath()));
        ZipEntry zipEntry = coreZip.getNextEntry();
        StringBuilder contents = new StringBuilder();
        while (zipEntry != null) {
            if (zipEntry.getName().equals(path)) {
                InputStream stream = file.getInputStream(zipEntry);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNext()) {
                    contents.append(scanner.nextLine()).append("\n");
                }
                break;
            }
            zipEntry = coreZip.getNextEntry();
        }
        coreZip.closeEntry();
        coreZip.close();
        file.close();
        return contents.toString();
    }

    public static void extract(File source, File dest, String internalPath) throws IOException {
        ZipInputStream coreZip = new ZipInputStream(java.nio.file.Files.newInputStream(source.toPath()));
        ZipEntry zipEntry = coreZip.getNextEntry();
        byte[] data = new byte[1024];
        while (zipEntry != null) {
            if (internalPath.startsWith(zipEntry.getName()) || zipEntry.getName().startsWith(internalPath)) {
                File newFile = new File(dest, zipEntry.getName());
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
            }
            zipEntry = coreZip.getNextEntry();
        }
        coreZip.closeEntry();
        coreZip.close();
    }

    public static String relativize(File file) {
        return ROOT.relativize(file.toURI()).getPath();
    }

    public static void move(File source, File destination, boolean force) throws IOException {
        move(source, destination, null, force);
    }

    public static void move(File source, File destination, Predicate<File> filter, boolean force) throws IOException {
        if (filter == null || filter.test(source)) {
            if (!source.exists()) {
                throw new IllegalArgumentException("Source (" + source.getPath() + ") doesn't exist.");
            }

            if (!force && destination.exists()) {
                throw new IllegalArgumentException("Destination (" + destination.getPath() + ") exists.");
            }

            if (source.isDirectory()) {
                File destDir = new File(destination, source.getName() + File.separator);
                destDir.mkdir();
                moveDirectory(source, destDir, filter);
            } else {
                moveFile(source, destination, filter);
            }
        }
    }

    public static void moveDirectory(File source, File destination, Predicate<File> filter) throws IOException {
        if (filter == null || filter.test(source)) {
            destination.mkdirs();

            File[] files = source.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (file.isDirectory()) {
                    moveDirectory(file, new File(destination, file.getName()), filter);
                } else {
                    moveFile(file, new File(destination, file.getName()), filter);
                }
            }

            FileIO.delete(source);
        }
    }

    public static void moveFile(File source, File destination, Predicate<File> filter) throws IOException {
        if (filter == null || filter.test(source)) {
            Files.move(source, destination);
        }
    }
}
