package xyz.destiall.caramel.editor;

import xyz.destiall.caramel.app.Application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class CreateScript {

    private static final String BASE =
            "package scripts;\n" +
            "\n" +
            "import xyz.destiall.caramel.objects.GameObject;\n" +
            "import xyz.destiall.caramel.components.Component;\n" +
            "\n" +
            "public class ${name} extends Component {\n" +
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

    public static File create(String className) {
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
}
