package caramel.api.utils;

import caramel.api.Application;
import com.google.common.io.Files;
import caramel.api.debug.Debug;
import caramel.api.scripts.InternalScript;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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

    public static ByteBuffer loadResourceBuffer(String path) {
        return ByteBuffer.wrap(loadResource(path));
    }

    public static void saveResource(String path, String targetPath) {
        Path p = Paths.get(targetPath);
        try {
            if (!java.nio.file.Files.exists(p)) {
                File f = p.toFile();
                f.createNewFile();
                InputStream stream = FileIO.class.getResourceAsStream("/" + path);
                if (stream == null) return;
                System.out.println("Writing resource file " + path);
                OutputStream outputStream = new FileOutputStream(f);
                byte[] b = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = stream.read(b)) > 0) {
                    outputStream.write(b, 0, bytesRead);
                }
                outputStream.flush();
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

    public static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        f.delete();
    }

    public static boolean isHidden(File file) {
        return file.isHidden() || file.getName().startsWith(".");
    }
}
