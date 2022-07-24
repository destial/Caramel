package caramel.api.render;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform1iv;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

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
        String source = null;
        String path = "assets/shaders/" + filePath;
        FileIO.saveResource(filePath, path);
        try {
            source = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
            Debug.logError("Error could not open file for shader : '" + path +"'");
        }
        String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");
        if (splitString.length < 2) {
            Debug.logError("Error shader '" + path + "' is not a valid shader");
        }

        String[] shadertype = new String[splitString.length - 1];
        int count = 1;
        int startPos = 0;
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
        vertexShader = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(vertexShader, vertexShaderSrc);
        glCompileShader(vertexShader);

        int success = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexShader, GL_INFO_LOG_LENGTH);
            Debug.logError("Vertex compilation failed. Shader path: " + path);
            System.err.println(glGetShaderInfoLog(vertexShader, len));
            return false;
        }

        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(fragmentShader, fragmentShaderSrc);
        glCompileShader(fragmentShader);

        success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentShader, GL_INFO_LOG_LENGTH);
            Debug.logError("Fragment compilation failed. Shader path: " + path);
            System.err.println(glGetShaderInfoLog(fragmentShader, len));
            return false;
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            Debug.logError("Shader linking failed. Shader path: " + path);
            System.err.println(glGetProgramInfoLog(shaderProgram, len));
            return false;
        }

        compiled = true;
        return true;
    }

    public void attach() {
        glUseProgram(shaderProgram);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void invalidate() {
        glDeleteProgram(shaderProgram);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        shaderProgram = 0;
        vertexShader = 0;
        fragmentShader = 0;
        SHADERS.remove(path.substring(0, path.length() - ".glsl".length()));
    }

    public void uploadMat4f(String name, Matrix4f matrix) {
        int loc = glGetUniformLocation(shaderProgram, name);
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        matrix.get(matBuffer);
        glUniformMatrix4fv(loc, false, matBuffer);
    }

    public void uploadVec3f(String name, Vector3f vector) {
        int loc = glGetUniformLocation(shaderProgram, name);
        glUniform3f(loc, vector.x, vector.y, vector.z);
    }

    public void uploadFloat(String name, float f) {
        int loc = glGetUniformLocation(shaderProgram, name);
        glUniform1f(loc, f);
    }

    public void uploadTexture(String name, int id) {
        int loc = glGetUniformLocation(shaderProgram, name);
        glUniform1i(loc, id);
    }

    public void uploadIntArray(String name, int[] array) {
        int loc = glGetUniformLocation(shaderProgram, name);
        glUniform1iv(loc, array);
    }

    public String getPath() {
        return path;
    }

    private static final HashMap<String, Shader> SHADERS = new HashMap<>();

    public static void invalidateAll() {
        for (Shader shader : SHADERS.values()) {
            glDeleteShader(shader.shaderProgram);
            glDeleteShader(shader.vertexShader);
            glDeleteShader(shader.fragmentShader);
            shader.shaderProgram = 0;
            shader.vertexShader = 0;
            shader.fragmentShader = 0;
        }
        SHADERS.clear();
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
        getShader("light");
        getShader("line");
        getShader("text");
    }
}
