package caramel.api.texture.mesh;

import caramel.api.math.Vertex;
import caramel.api.texture.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;

public final class IcosahedronMesh extends Mesh {
    private static final float t = (float)(1 + Math.sqrt(5.0)) / 2.0f;;
    private ArrayList<Vertex> baseVertices = new ArrayList<>();
    private ArrayList<Triangle> baseTriangles = new ArrayList<>();
    private final float radius;
    private final int subdivisions;

    public IcosahedronMesh() {
        this(1, 2);
    }

    public IcosahedronMesh(float radius, int subdivisions) {
        name = "Icosahedron";
        this.subdivisions = subdivisions;
        this.radius = radius;
        Vertex v1 = new Vertex(-1, t, 0.0f);
        Vertex v2 = new Vertex(1, t, 0.0f);
        Vertex v3 = new Vertex(-1, -t, 0.0f);
        Vertex v4 = new Vertex(1, -t, 0.0f);

        Vertex v5 = new Vertex(0.0f, -1, t);
        Vertex v6 = new Vertex(0.0f, 1, t);
        Vertex v7 = new Vertex(0.0f, -1, -t);
        Vertex v8 = new Vertex(0.0f, 1, -t);

        Vertex v9 = new Vertex(t,0.0f,-1);
        Vertex v10 = new Vertex(t, 0.0f, 1);
        Vertex v11 = new Vertex(-t, 0.0f, -1);
        Vertex v12 = new Vertex(-t, 0.0f, 1);

        Vertex v13 = new Vertex(v1);
        Vertex v14 = v11.getMidVertex(v12);
        Vertex v15 = new Vertex(v14);
        Vertex v16 = new Vertex(v3);

        v1.normalize();
        v2.normalize();
        v3.normalize();
        v4.normalize();
        v5.normalize();
        v6.normalize();
        v7.normalize();
        v8.normalize();
        v9.normalize();
        v10.normalize();
        v11.normalize();
        v12.normalize();
        v13.normalize();
        v13.texCoords.x = 0;
        v14.normalize();
        v14.texCoords.x = 0;
        v15.normalize();
        v15.texCoords.x = 1;
        v16.normalize();
        v16.texCoords.x = 0;

        v1.adjustHeight(this.radius);
        v2.adjustHeight(this.radius);
        v3.adjustHeight(this.radius);
        v4.adjustHeight(this.radius);
        v5.adjustHeight(this.radius);
        v6.adjustHeight(this.radius);
        v7.adjustHeight(this.radius);
        v8.adjustHeight(this.radius);
        v9.adjustHeight(this.radius);
        v10.adjustHeight(this.radius);
        v11.adjustHeight(this.radius);
        v12.adjustHeight(this.radius);
        v13.adjustHeight(this.radius);
        v14.adjustHeight(this.radius);
        v15.adjustHeight(this.radius);
        v16.adjustHeight(this.radius);

        baseVertices.add(v1);
        baseVertices.add(v2);
        baseVertices.add(v3);
        baseVertices.add(v4);
        baseVertices.add(v5);
        baseVertices.add(v6);
        baseVertices.add(v7);
        baseVertices.add(v8);
        baseVertices.add(v9);
        baseVertices.add(v10);
        baseVertices.add(v11);
        baseVertices.add(v12);
        baseVertices.add(v13);
        baseVertices.add(v14);
        baseVertices.add(v15);
        baseVertices.add(v16);

        baseTriangles.add(new Triangle(v1,v6,v12));
        baseTriangles.add(new Triangle(v1,v2,v6));// pole
        baseTriangles.add(new Triangle(v13,v8,v2));// pole
        baseTriangles.add(new Triangle(v11,v8,v13));//baseTriangles.add(new Triangle(v1,v8,v11));
        baseTriangles.add(new Triangle(v1,v12,v15));//baseTriangles.add(new Triangle(v1,v11,v12));

        baseTriangles.add(new Triangle(v4,v10,v9));
        baseTriangles.add(new Triangle(v4,v9,v7));
        baseTriangles.add(new Triangle(v4,v7,v16));//pole
        baseTriangles.add(new Triangle(v4,v3,v5));//pole
        baseTriangles.add(new Triangle(v4,v5,v10));

        baseTriangles.add(new Triangle(v6,v5,v12));
        baseTriangles.add(new Triangle(v5,v3,v12));
        baseTriangles.add(new Triangle(v3,v15,v12));//baseTriangles.add(new Triangle(v3,v12,v11));
        baseTriangles.add(new Triangle(v11,v7,v8));
        baseTriangles.add(new Triangle(v8,v7,v9));

        baseTriangles.add(new Triangle(v8,v9,v2));
        baseTriangles.add(new Triangle(v2,v9,v10));
        baseTriangles.add(new Triangle(v10,v6,v2));
        baseTriangles.add(new Triangle(v10,v5,v6));
        baseTriangles.add(new Triangle(v11,v16,v7));//baseTriangles.add(new Triangle(v11,v7,v3));

        baseTriangles.add(new Triangle(v13,v11,v14));
        baseTriangles.add(new Triangle(v16,v14,v11));
        for (int i = 0; i < subdivisions; i++) {
            subdivide();
        }

        vertexArray.addAll(baseVertices);

        for (Triangle tr : baseTriangles) {
            elementArray.add(baseVertices.indexOf(tr.v1));
            elementArray.add(baseVertices.indexOf(tr.v2));
            elementArray.add(baseVertices.indexOf(tr.v3));
        }
    }

    public void subdivide() {
        ArrayList<Triangle> newTriangles = new ArrayList<>();
        ArrayList<Vertex> newVertices = new ArrayList<>(baseVertices);
        for (int i = 0; i < baseTriangles.size(); ) {
            Triangle aTriangle = baseTriangles.get(i);
            baseTriangles.remove(aTriangle);
            subdivideTri(aTriangle, newTriangles, newVertices);
        }
        baseTriangles = newTriangles;
        baseVertices = newVertices;
    }

    void subdivideTri(Triangle tri, ArrayList<Triangle> nTriangles, ArrayList<Vertex> nVertices) {
        Vertex v1 = new Vertex(tri.v1);
        v1.normalize();
        Vertex v2 = new Vertex(tri.v2);
        v2.normalize();
        Vertex v3 = new Vertex(tri.v3);
        v3.normalize();

        Vertex v12 = v1.getMidVertex(v2);
        v12.normalize();
        v12.adjustHeight(radius);
        Vertex v23 = v2.getMidVertex(v3);
        v23.normalize();
        v23.adjustHeight(radius);
        Vertex v31 = v3.getMidVertex(v1);
        v31.normalize();
        v31.adjustHeight(radius);

        if (tri.v1.texCoords.x == 0 && tri.v2.texCoords.x == 0)
            v12.texCoords.x = 0;
        if (tri.v2.texCoords.x == 0 && tri.v3.texCoords.x == 0)
            v23.texCoords.x = 0;
        if (tri.v1.texCoords.x == 0 && tri.v3.texCoords.x == 0)
            v31.texCoords.x = 0;

        if (tri.v1.texCoords.x == 1 && tri.v2.texCoords.x == 1)
            v12.texCoords.x = 1;
        if (tri.v2.texCoords.x == 1 && tri.v3.texCoords.x == 1)
            v23.texCoords.x = 1;
        if (tri.v1.texCoords.x == 1 && tri.v3.texCoords.x == 1)
            v31.texCoords.x = 1;

        setPole(tri.v1,tri.v2,v12);
        setPole(tri.v2,tri.v3,v23);
        setPole(tri.v1,tri.v3,v31);

        if (!nVertices.contains(v12))
            nVertices.add(v12);
        else
            v12 = nVertices.get(nVertices.indexOf(v12));

        if (!nVertices.contains(v23))
            nVertices.add(v23);
        else
            v23 = nVertices.get(nVertices.indexOf(v23));

        if (!nVertices.contains(v31))
            nVertices.add(v31);
        else
            v31 = nVertices.get(nVertices.indexOf(v31));

        nTriangles.add(new Triangle(tri.v1,v12,v31));
        nTriangles.add(new Triangle(tri.v2,v23,v12));
        nTriangles.add(new Triangle(tri.v3,v31,v23));
        nTriangles.add(new Triangle(v12,v23,v31));
    }

    void setPole(Vertex v1, Vertex v2, Vertex v12) {
        v12.texCoords.x = (v1.texCoords.x + v2.texCoords.x)/2;
    }

    public static class Triangle {
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

    @Override
    public Mesh copy() {
        IcosahedronMesh mesh = new IcosahedronMesh(radius, subdivisions);
        mesh.name = name;
        mesh.drawArrays = drawArrays;
        mesh.type = type;
        mesh.texture = texture;
        mesh.shader = shader;
        mesh.dirty = dirty;
        for (Vertex vertex : vertexArray) {
            Vertex copy = new Vertex();
            copy.position.set(vertex.position);
            copy.normal.set(vertex.normal);
            copy.color.set(vertex.color);
            copy.texCoords.set(vertex.texCoords);
            copy.texSlot = vertex.texSlot;
            mesh.vertexArray.add(copy);
        }
        mesh.elementArray.addAll(elementArray);
        mesh.build(withIndices);
        return mesh;
    }
}
