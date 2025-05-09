package caramel.api.render;

import caramel.api.debug.Debug;
import caramel.api.graphics.Graphics;
import caramel.api.utils.FileIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static caramel.api.graphics.GL20.GL_COMPILE_STATUS;
import static caramel.api.graphics.GL20.GL_FALSE;
import static caramel.api.graphics.GL20.GL_FRAGMENT_SHADER;
import static caramel.api.graphics.GL20.GL_INFO_LOG_LENGTH;
import static caramel.api.graphics.GL20.GL_LINK_STATUS;
import static caramel.api.graphics.GL20.GL_VERTEX_SHADER;

public final class Shader {
    private transient String vertexShaderSrc = "";
    private transient String fragmentShaderSrc = "";
    private transient boolean compiled = false;
    private transient int shaderProgram;
    private transient int vertexShader;
    private transient int fragmentShader;

    private final String path;

    private Shader(String filePath) {
        this.path = filePath;
        loadSource();
    }

    public void loadSource() {
        if (compiled) return;
        String source = null;
        final String path = "assets/shaders/" + this.path;
        FileIO.saveResource(this.path, path);
        try {
            source = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
            Debug.logError("Error could not open file for shader : '" + path +"'");
        }
        final String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");
        if (splitString.length < 2) {
            Debug.logError("Error shader '" + path + "' is not a valid shader");
        }

        String[] shadertype = new String[splitString.length - 1];
        int count = 1;
        int startPos;
        int endPos = 0;
        while (count < splitString.length) {
            startPos = source.indexOf("#type", endPos) + 6;
            endPos = source.indexOf("\r\n", startPos);
            shadertype[count - 1] = source.substring(startPos, endPos).trim();
            switch (shadertype[count - 1]) {
                case "vertex":
                    vertexShaderSrc = splitString[count];
                    break;
                case "fragment":
                    fragmentShaderSrc = splitString[count];
                    break;
                default:
                    Debug.logError("Error shader '" + path + "' has invalid types");
                    break;
            }
            ++count;
        }
    }

    public boolean compile() {
        if (compiled) return false;
        vertexShader = Graphics.get().glCreateShader(GL_VERTEX_SHADER);

        Graphics.get().glShaderSource(vertexShader, vertexShaderSrc);
        Graphics.get().glCompileShader(vertexShader);

        int success = Graphics.get().glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = Graphics.get().glGetShaderi(vertexShader, GL_INFO_LOG_LENGTH);
            Debug.logError("Vertex compilation failed. Shader path: " + path);
            System.err.println(Graphics.get().glGetShaderInfoLog(vertexShader, len));
            return false;
        }

        fragmentShader = Graphics.get().glCreateShader(GL_FRAGMENT_SHADER);

        Graphics.get().glShaderSource(fragmentShader, fragmentShaderSrc);
        Graphics.get().glCompileShader(fragmentShader);

        success = Graphics.get().glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            final int len = Graphics.get().glGetShaderi(fragmentShader, GL_INFO_LOG_LENGTH);
            Debug.logError("Fragment compilation failed. Shader path: " + path);
            System.err.println(Graphics.get().glGetShaderInfoLog(fragmentShader, len));
            return false;
        }

        shaderProgram = Graphics.get().glCreateProgram();
        Graphics.get().glAttachShader(shaderProgram, vertexShader);
        Graphics.get().glAttachShader(shaderProgram, fragmentShader);
        Graphics.get().glLinkProgram(shaderProgram);

        success = Graphics.get().glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            final int len = Graphics.get().glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            Debug.logError("Shader linking failed. Shader path: " + path);
            System.err.println(Graphics.get().glGetProgramInfoLog(shaderProgram, len));
            return false;
        }

        compiled = true;
        return true;
    }

    public void recompile() {
        detach();
        invalidate(false);
        loadSource();
        compile();
    }

    public void attach() {
        Graphics.get().glUseProgram(shaderProgram);
    }

    public void detach() {
        Graphics.get().glUseProgram(0);
    }

    public void invalidate(boolean remove) {
        if (shaderProgram != 0) Graphics.get().glDeleteProgram(shaderProgram);
        if (vertexShader != 0) Graphics.get().glDeleteShader(vertexShader);
        if (fragmentShader != 0) Graphics.get().glDeleteShader(fragmentShader);
        shaderProgram = 0;
        vertexShader = 0;
        fragmentShader = 0;
        if (remove) SHADERS.remove(path.substring(0, path.length() - ".glsl".length()));
        compiled = false;
    }

    public void uploadMat4f(String name, Matrix4f matrix) {
        final int loc = Graphics.get().glGetUniformLocation(shaderProgram, name);
        final FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        matrix.get(matBuffer);
        Graphics.get().glUniformMatrix4fv(loc, false, matBuffer);
    }

    public void uploadVec3f(String name, Vector3f vector) {
        final int loc = Graphics.get().glGetUniformLocation(shaderProgram, name);
        Graphics.get().glUniform3f(loc, vector.x, vector.y, vector.z);
    }

    public void uploadFloat(String name, float f) {
        final int loc = Graphics.get().glGetUniformLocation(shaderProgram, name);
        Graphics.get().glUniform1f(loc, f);
    }

    public void uploadTexture(String name, int id) {
        final int loc = Graphics.get().glGetUniformLocation(shaderProgram, name);
        Graphics.get().glUniform1i(loc, id);
    }

    public void uploadIntArray(String name, int[] array) {
        final int loc = Graphics.get().glGetUniformLocation(shaderProgram, name);
        Graphics.get().glUniform1iv(loc, array);
    }

    public String getPath() {
        return path;
    }

    private static final Map<String, Shader> SHADERS = new HashMap<>();

    public static void invalidateAll() {
        for (final Shader shader : SHADERS.values()) {
            Graphics.get().glDeleteShader(shader.shaderProgram);
            Graphics.get().glDeleteShader(shader.vertexShader);
            Graphics.get().glDeleteShader(shader.fragmentShader);
            shader.shaderProgram = 0;
            shader.vertexShader = 0;
            shader.fragmentShader = 0;
        }
        SHADERS.clear();
    }

    public static Map<String, Shader> getShaders() {
        return SHADERS;
    }

    public static Shader getShader(String name) {
        Shader shader = SHADERS.get(name);
        if (shader == null) {
            shader = new Shader(name + ".glsl");
            if (shader.compile()) {
                SHADERS.put(name, shader);
            }
        }
        return shader;
    }
    static {
        getShader("default");
        getShader("color");
        //getShader("light");
        getShader("line");
        getShader("text");
    }
}
