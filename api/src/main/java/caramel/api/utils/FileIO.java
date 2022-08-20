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
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final File ROOT_FILE = new File("");
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
        copy(source, destination, false);
    }

    public static void copy(File source, File destination, boolean force) throws IOException {
        if (!source.exists()) {
            throw new IllegalArgumentException("Source (" + source.getPath() + ") doesn't exist.");
        }

        if (!force && destination.exists()) {
            throw new IllegalArgumentException("Destination (" + destination.getPath() + ") exists.");
        }

        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    public static void copyDirectory(File source, File destination) throws IOException {
        destination.mkdirs();

        File[] files = source.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                copyDirectory(file, new File(destination, file.getName()));
            } else {
                copyFile(file, new File(destination, file.getName()));
            }
        }
    }

    public static void copyFile(File source, File destination) throws IOException {
        Files.copy(source, destination);
    }

    public static boolean delete(File f) {
        if (f.isDirectory()) {
            for (File c : Objects.requireNonNull(f.listFiles())) {
                delete(c);
            }
        }

        return f.delete();
    }

    public static String asRelative(File file) {
        return ROOT.relativize(file.toURI()).getPath();
    }

    public static boolean isHidden(File file) {
        return file.isHidden() || file.getName().startsWith(".");
    }
}
