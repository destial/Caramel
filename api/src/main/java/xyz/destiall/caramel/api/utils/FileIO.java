package xyz.destiall.caramel.api.utils;

import com.google.common.io.Files;
import xyz.destiall.caramel.api.Application;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public final class FileIO {
    private FileIO() {}

    private static final String BASE =
            "package scripts;\n" +
            "\n" +
            "import xyz.destiall.caramel.api.objects.GameObject;\n" +
            "import xyz.destiall.caramel.api.scripts.Script;\n" +
            "\n" +
            "public class ${name} extends Script {\n" +
            "    public ${name}(GameObject gameObject) {\n" +
            "        super(gameObject);\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void start() {\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void update() {\n" +
            "\n" +
            "    }\n" +
            "}";

    public static File writeScript(String className) {
        File scriptFolder = new File("assets/scripts/");
        if (!scriptFolder.exists()) scriptFolder.mkdir();
        String contents = BASE.replace("${name}", className);
        File scriptFile = new File(scriptFolder, className + ".java");
        if (scriptFile.exists()) return scriptFile;
        try {
            FileWriter write = new FileWriter(scriptFile);
            BufferedWriter buffer = new BufferedWriter(write);
            buffer.write(contents);
            buffer.close();

            Application.getApp().getScriptManager().reloadScript(scriptFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scriptFile;
    }

    public static boolean writeData(File location, String contents) {
        try (FileWriter write = new FileWriter(location)) {
            try (BufferedWriter buffer = new BufferedWriter(write)) {
                buffer.write(contents);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
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

    private static void copyDirectory(File source, File destination) throws IOException {
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

    private static void copyFile(File source, File destination) throws IOException {
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