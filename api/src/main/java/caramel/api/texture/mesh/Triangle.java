package caramel.api.texture.mesh;

import caramel.api.math.Vertex;
import org.joml.Vector3f;

public class Triangle {
    public final Vertex v1,v2,v3;
    public final Vector3f normal;
    public Triangle(Vertex vert1, Vertex vert2, Vertex vert3) {
        v1 = vert1;
        v2 = vert2;
        v3 = vert3;

        Vertex v12 = new Vertex(v2);
        v12.position.sub(v1.position);
        Vertex v23 = new Vertex(v3);
        v23.position.sub(v2.position);
        normal = v12.position.cross(v23.position);
        normal.normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass()) {
            Vertex vert = (Vertex) o;
            return vert.equals(v1) || vert.equals(v2) || vert.equals(v3);
        } else {
            Triangle tri = (Triangle) o;
            return tri.v1.equals(v1) || tri.v2.equals(v2) || tri.v3.equals(v3);
        }
    }
}
