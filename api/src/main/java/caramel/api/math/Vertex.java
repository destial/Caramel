package caramel.api.math;

import caramel.api.interfaces.Copyable;
import caramel.api.utils.Color;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Used for mesh rendering. Each mesh contains a list of vertex.
 */
public final class Vertex implements Copyable<Vertex> {
    public Vector3f position;
    public Vector2f texCoords;
    public Color color;
    public Vector3f normal;
    public float texSlot;
    public float height;

    public Vertex(float x, float y, float z) {
        position = new Vector3f(x, y, z);
        texCoords = new Vector2f();
        color = new Color();
        normal = new Vector3f();
        texSlot = -1;
    }

    public Vertex(Vertex vertex) {
        position = new Vector3f(vertex.position);
        texCoords = new Vector2f(vertex.texCoords);
        color = new Color(vertex.color);
        normal = new Vector3f(vertex.normal);
        texSlot = vertex.texSlot;
    }

    public Vertex() {
        position = new Vector3f();
        texCoords = new Vector2f();
        color = new Color();
        normal = new Vector3f();
        texSlot = -1;
    }

    public Vertex getMidVertex(Vertex v) {
        return new Vertex((position.x + v.position.x)/2f,(position.y + v.position.y)/2f,(position.z + v.position.z)/2f);
    }

    public void normalize() {
        float len = (float)Math.sqrt(Math.pow(position.x,2) + Math.pow(position.y,2) + Math.pow(position.z,2));
        position.x /= len;
        position.y /= len;
        position.z /= len;
        texCoords.x = (float)(0.5 + Math.atan2(position.z,position.x)/(Math.PI *2));
        texCoords.x = (float)(0.5 - Math.asin(position.y)/Math.PI);
    }

    public void setHeight(float h) {
        height = h;
    }

    public void adjustHeight(float h) {
        double min = (1/3f) + 1;
        height = (height/5 + 1)/2*h ;
        position.x *= height;
        position.y *= height;
        position.z *= height;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "position=" + position +
                ", texCoords=" + texCoords +
                ", color=" + color +
                ", normal=" + normal +
                ", texSlot=" + texSlot +
                '}';
    }

    public static final int SIZE = 3 + 2 + 4 + 3 + 1;

    @Override
    public Vertex copy() {
        return new Vertex(this);
    }
}
