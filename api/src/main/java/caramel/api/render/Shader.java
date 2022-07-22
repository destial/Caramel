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
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

public final class Shader {
    private final String path;
    private String vertexShaderSrc = "";
    private String fragmentShaderSrc = "";

    private int shaderProgram;

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

    public void compile() {
        int vertexId = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(vertexId, vertexShaderSrc);
        glCompileShader(vertexId);

        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);
            Debug.logError("Vertex compilation failed. Shader path: " + path);
            System.err.println(glGetShaderInfoLog(vertexId, len));
            return;
        }

        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(fragmentId, fragmentShaderSrc);
        glCompileShader(fragmentId);

        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);
            Debug.logError("Fragment compilation failed. Shader path: " + path);
            System.err.println(glGetShaderInfoLog(fragmentId, len));
            return;
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexId);
        glAttachShader(shaderProgram, fragmentId);
        glLinkProgram(shaderProgram);

        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            Debug.logError("Shader linking failed. Shader path: " + path);
            System.err.println(glGetProgramInfoLog(shaderProgram, len));
        }
    }

    public void use() {
        glUseProgram(shaderProgram);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void uploadMat4f(String name, Matrix4f matrix) {
        int var = glGetUniformLocation(shaderProgram, name);
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        matrix.get(matBuffer);
        glUniformMatrix4fv(var, false, matBuffer);
    }

    public void uploadVec3f(String name, Vector3f vector) {
        int var = glGetUniformLocation(shaderProgram, name);
        glUniform3f(var, vector.x, vector.y, vector.z);
    }

    public void uploadFloat(String name, float f) {
        int var = glGetUniformLocation(shaderProgram, name);
        glUniform1f(var, f);
    }

    public void uploadTexture(String name, int id) {
        int var = glGetUniformLocation(shaderProgram, name);
        glUniform1i(var, id);
    }

    public String getPath() {
        return path;
    }

    private static final HashMap<String, Shader> shaders = new HashMap<>();

    public static Shader getShader(String name) {
        return shaders.get(name);
    }

    static {
        shaders.put("default", new Shader("default.glsl"));
        shaders.put("color", new Shader("color.glsl"));
        shaders.put("light", new Shader("light.glsl"));
        shaders.put("line", new Shader("line.glsl"));
        shaders.put("text", new Shader("text.glsl"));
        for (Shader shader : shaders.values()) {
            shader.compile();
        }
    }
}
