package xyz.destiall.caramel.graphics;

import com.mokiat.data.front.parser.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11.GL_LINES;

public class MeshBuilder {

    public static Mesh createCircle(Vector4f color, float radius, int n) {
        Mesh mesh = new Mesh();
        for (int i = 0; i < n; i++) {
            float x = (float) (radius * Math.sin(2 * Math.PI * i / n));
            float y = (float) (radius * Math.cos(2 * Math.PI * i / n));
            mesh.pushVertex(new Vector3f(x, y, 0), color, new Vector2f(0, 0), new Vector3f(0, 0, 1));
        }
        for (int i = 0; i < (n - 2); i++) {
            mesh.pushIndex(0).pushIndex(i + 1).pushIndex(i + 2);
        }
        return mesh;
    }

    public static Mesh createCube(Vector4f color, float length) {
        Mesh mesh = new Mesh();

        mesh.pushVertex(0.5f * length, -0.5f * length, -0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(-0.5f * length, -0.5f * length, -0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(-0.5f * length, 0.5f * length, -0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(0.5f * length, 0.5f * length, -0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(0.5f * length, -0.5f * length, 0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(0.5f * length, 0.5f * length, 0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(-0.5f * length, 0.5f * length, -0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);
        mesh.pushVertex(-0.5f * length, -0.5f * length, 0.5f * length,   color.x, color.y, color.z, color.w,   1f, 1f,   0f, 0f, 0f);

        mesh.pushIndex(
                1, 3, 0, 1, 3, 2,
                0, 5, 4, 0, 5, 3,
                4, 6, 7, 4, 6, 5,
                7, 2, 1, 7, 2, 6,
                6, 3, 2, 6, 3, 5,
                1, 4, 0, 1, 4, 7
        );

        return mesh;
    }

    public static Mesh createQuad(float length) {
        Mesh mesh = new Mesh();
        mesh.pushVertex( 0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f)
            .pushVertex(-0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 0f,   0f, 0f, 0f)
            .pushVertex( 0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 0f,   0f, 0f, 0f)
            .pushVertex(-0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f);

        mesh.pushIndex(2, 1, 0, 0, 1, 3);
        return mesh;
    }

    public static Mesh createModel(String path) {
        Mesh mesh = new Mesh(true);
        try (InputStream in = new FileInputStream(path)) {
            final IOBJParser parser = new OBJParser();
            final OBJModel model = parser.parse(in);
            for (OBJObject object : model.getObjects()) {
                for (OBJMesh objMesh : object.getMeshes()) {
                    for (OBJFace face : objMesh.getFaces()) {
                        for (OBJDataReference ref : face.getReferences()) {
                            OBJVertex objV = model.getVertex(ref);
                            OBJTexCoord objT = model.getTexCoord(ref);
                            OBJNormal objN = model.getNormal(ref);

                            Vertex v = new Vertex();
                            v.position.set(objV.x, objV.y, objV.z);
                            v.color.set(1f, 1f, 1f, 1f);
                            v.texCoords.set(objT.u, objT.v);
                            v.normal.set(objN.x, objN.y, objN.z);

                            mesh.pushVertex(v);
                            mesh.pushIndex(ref.vertexIndex);//, ref.texCoordIndex, ref.normalIndex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mesh;
    }

    public static Mesh createAxes(float length) {
        Mesh mesh = new Mesh();
        mesh.type = GL_LINES;
        mesh.pushVertex(-length, 0, 0,  1, 0, 0, 1, 0, 0, 0, 0, 0);
        mesh.pushVertex( length, 0, 0,  1, 0, 0, 1, 0, 0, 0, 0, 0);

        mesh.pushVertex(0, -length, 0,  0, 1, 0, 1, 0, 0, 0, 0, 0);
        mesh.pushVertex(0,  length, 0,  0, 1, 0, 1, 0, 0, 0, 0, 0);

        mesh.pushVertex(0, 0, -length,  0, 0, 1, 1, 0, 0, 0, 0, 0);
        mesh.pushVertex(0, 0,  length,  0, 0, 1, 1, 0, 0, 0, 0, 0);

        mesh.pushIndex(0, 1, 2, 3, 4, 5);

        return mesh;
    }
}
