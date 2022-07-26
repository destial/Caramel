package caramel.api.texture.mesh;

import caramel.api.math.Vertex;
import caramel.api.texture.Mesh;

import java.util.ArrayList;

public final class IcosahedronMesh extends Mesh {
    private ArrayList<Vertex> baseVertices = new ArrayList<>();
    private ArrayList<Triangle> baseTriangles = new ArrayList<>();
    private final float height;

    public IcosahedronMesh(float radius, int subdivisions) {
        float t = (float)(1 + Math.sqrt(5.0)) / 2.0f;
        height = radius;
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

        v1.adjustHeight(height);
        v2.adjustHeight(height);
        v3.adjustHeight(height);
        v4.adjustHeight(height);
        v5.adjustHeight(height);
        v6.adjustHeight(height);
        v7.adjustHeight(height);
        v8.adjustHeight(height);
        v9.adjustHeight(height);
        v10.adjustHeight(height);
        v11.adjustHeight(height);
        v12.adjustHeight(height);
        v13.adjustHeight(height);
        v14.adjustHeight(height);
        v15.adjustHeight(height);
        v16.adjustHeight(height);

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

    void subdivide() {
        ArrayList<Triangle> newTriangles = new ArrayList<>();
        ArrayList<Vertex> newVertices = new ArrayList<>(baseVertices);
        for(int i=0;i<baseTriangles.size();)
        {
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
        v12.adjustHeight(height);
        Vertex v23 = v2.getMidVertex(v3);
        v23.normalize();
        v23.adjustHeight(height);
        Vertex v31 = v3.getMidVertex(v1);
        v31.normalize();
        v31.adjustHeight(height);

        if(tri.v1.texCoords.x == 0 && tri.v2.texCoords.x == 0)
            v12.texCoords.x = 0;
        if(tri.v2.texCoords.x == 0 && tri.v3.texCoords.x == 0)
            v23.texCoords.x = 0;
        if(tri.v1.texCoords.x == 0 && tri.v3.texCoords.x == 0)
            v31.texCoords.x = 0;

        if(tri.v1.texCoords.x == 1 && tri.v2.texCoords.x == 1)
            v12.texCoords.x = 1;
        if(tri.v2.texCoords.x == 1 && tri.v3.texCoords.x == 1)
            v23.texCoords.x = 1;
        if(tri.v1.texCoords.x == 1 && tri.v3.texCoords.x == 1)
            v31.texCoords.x = 1;

        setPole(tri.v1,tri.v2,v12);
        setPole(tri.v2,tri.v3,v23);
        setPole(tri.v1,tri.v3,v31);

        if(!nVertices.contains(v12))
            nVertices.add(v12);
        else
            v12 = nVertices.get(nVertices.indexOf(v12));

        if(!nVertices.contains(v23))
            nVertices.add(v23);
        else
            v23 = nVertices.get(nVertices.indexOf(v23));

        if(!nVertices.contains(v31))
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
}
