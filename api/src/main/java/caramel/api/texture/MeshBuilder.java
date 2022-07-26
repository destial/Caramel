package caramel.api.texture;

import caramel.api.texture.mesh.IcosahedronMesh;
import org.joml.Vector3f;
import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import java.nio.FloatBuffer;

import static org.jbox2d.common.MathUtils.TWOPI;
import static org.lwjgl.opengl.GL11.GL_LINES;

public final class MeshBuilder {

    public static Mesh createCircle(float radius, int n) {
        Mesh mesh = new Mesh();
        mesh.pushVertex(0f, 0f, 0f,   1f, 1f, 1f, 1f,   0.5f, 0.5f,   0f, 0f, 1f,   0f);

        for (int i = 0; i <= n; i++) {
            float x = (float) (radius * Math.sin(TWOPI * i / n));
            float y = (float) (radius * Math.cos(TWOPI * i / n));
            mesh.pushVertex(x, y, 0f,   1f, 1f, 1f, 1f,   ((x / radius) + 1) / 2f, 1 - ((y / radius) + 1) / 2f,   0f, 0f, 1f,   0f);
        }

        for (int i = 0; i < n; i++) {
            mesh.pushIndex(0).pushIndex(i + 1).pushIndex(i + 2);
        }
        return mesh;
    }

    public static Mesh createIcosahedron(float radius, int subdivisions) {
        return new IcosahedronMesh(radius, subdivisions);
    }

    private static int getMiddleVertex(int vertexIndex1, int vertexIndex2, Mesh mesh, float radius) {
        Vector3f newVertex = new Vector3f(mesh.getVertex(vertexIndex1).position).add(mesh.getVertex(vertexIndex2).position);
        newVertex.div(2);
        newVertex.normalize();
        newVertex.mul(radius);

        int middleVertexIndex = mesh.vertexArray.size();
        mesh.getVertex(middleVertexIndex).position = newVertex;

        return middleVertexIndex;
    }

    public static Mesh createCube(float length) {
        Mesh mesh = new Mesh();
        ParShapesMesh parShapesMesh = ParShapes.par_shapes_create_cube();
        if (parShapesMesh == null) return mesh;
        FloatBuffer points = parShapesMesh.points(parShapesMesh.npoints() * 3);
        FloatBuffer normals = parShapesMesh.normals(parShapesMesh.npoints() * 3);
        FloatBuffer texCoords = parShapesMesh.tcoords(parShapesMesh.npoints() * 2);

        int t = 0;
        int index = 0;
        for (int i = 0; i < parShapesMesh.npoints(); i+=3) {
            float x = points.get(i);
            float y = points.get(i + 1);
            float z = points.get(i + 2);

            float nx = 0f;
            float ny = 0f;
            float nz = 0f;
            if (normals != null) {
                nx = normals.get(i);
                ny = normals.get(i + 1);
                nz = normals.get(i + 2);
            }

            float u = 0f;
            float v = 0f;

            if (texCoords != null) {
                u = texCoords.get(t++);
                v = texCoords.get(t++);
            }

            mesh.pushVertex(x * length, y * length, z * length,   1f, 1f, 1f, 1f,   u, v,   nx, ny, nz,   0f);
            mesh.pushIndex(index++);
        }

        return mesh;
    }

    public static Mesh createQuad(float length) {
        Mesh mesh = new Mesh();
        mesh.pushVertex( 0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f,   0f)
            .pushVertex(-0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 0f,   0f, 0f, 0f,   0f)
            .pushVertex( 0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 0f,   0f, 0f, 0f,   0f)
            .pushVertex(-0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f,   0f);

        mesh.pushIndex(2, 1, 0, 0, 1, 3);
        return mesh;
    }

    public static Mesh createAxes(float length) {
        Mesh mesh = new Mesh();
        mesh.type = GL_LINES;
        mesh.pushVertex(-length, 0, 0,  1, 0, 0, 1, 0, 0, 0, 0, 0,   0f);
        mesh.pushVertex( length, 0, 0,  1, 0, 0, 1, 0, 0, 0, 0, 0,   0f);

        mesh.pushVertex(0, -length, 0,  0, 1, 0, 1, 0, 0, 0, 0, 0,   0f);
        mesh.pushVertex(0,  length, 0,  0, 1, 0, 1, 0, 0, 0, 0, 0,   0f);

        mesh.pushVertex(0, 0, -length,  0, 0, 1, 1, 0, 0, 0, 0, 0,   0f);
        mesh.pushVertex(0, 0,  length,  0, 0, 1, 1, 0, 0, 0, 0, 0,   0f);

        mesh.pushIndex(0, 1, 2, 3, 4, 5);

        return mesh;
    }
}
