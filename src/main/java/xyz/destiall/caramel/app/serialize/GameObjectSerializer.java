package xyz.destiall.caramel.app.serialize;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.render.MeshRenderer;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.render.Renderer;
import xyz.destiall.caramel.api.texture.Mesh;
import xyz.destiall.caramel.app.editor.Scene;
import xyz.destiall.java.gson.JsonArray;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;
import xyz.destiall.java.reflection.Reflect;

import java.lang.reflect.Type;
import java.util.LinkedList;

public class GameObjectSerializer implements JsonSerializer<GameObject>, JsonDeserializer<GameObject> {
    public int maxId = 0;

    @Override
    public GameObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return deserialize(null, jsonElement);
    }

    public GameObject deserialize(Scene scene, JsonElement jsonElement) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        GameObject gameObject = new GameObject(scene);
        gameObject.name = object.get("name").getAsString();
        gameObject.id = object.has("id") ? object.get("id").getAsInt() : ++maxId;

        JsonArray components = object.get("components").getAsJsonArray();
        for (JsonElement c : components) {
            if (!c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, gameObject);
            Reflect.setDeclaredField(gameObject, "transform", component);
            if (maxId < component.id) {
                maxId = component.id;
            }
            gameObject.addComponent(component);

            break;
        }

        JsonArray tags = object.has("tags") ? object.get("tags").getAsJsonArray() : new JsonArray();
        for (JsonElement tag : tags) {
            String t = tag.getAsString();
            gameObject.tags.add(t);
        }

        for (JsonElement c : components) {
            if (c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, gameObject);
            if (component instanceof MeshRenderer) {
                MeshRenderer renderer = (MeshRenderer) component;
                if (renderer.mesh != null) renderer.mesh.build();
            }
            if (maxId < component.id) {
                maxId = component.id;
            }
            gameObject.addComponent(component);
        }

        JsonArray children = object.get("children").getAsJsonArray();
        for (JsonElement c : children) {
            GameObject child = SceneSerializer.GAME_OBJECT_SERIALIZER.deserialize(scene, c);
            child.parent = gameObject.transform;
            gameObject.children.add(child);
        }

        if (maxId < gameObject.id) {
            maxId = gameObject.id;
        }
        return gameObject;
    }

    @Override
    public JsonElement serialize(GameObject gameObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray components = new JsonArray();
        for (Component component : gameObject.getComponents()) {
            components.add(SceneSerializer.GSON.toJsonTree(component));
        }
        JsonArray tags = new JsonArray();
        for (String tag : gameObject.tags) {
            tags.add(tag);
        }
        JsonArray children = new JsonArray();
        for (GameObject child : gameObject.children) {
            children.add(SceneSerializer.GSON.toJsonTree(child));
        }
        JsonObject object = new JsonObject();
        object.add("components", components);
        object.add("children", children);
        object.add("tags", tags);
        object.addProperty("name", gameObject.name);
        object.addProperty("id", gameObject.id);
        return object;
    }
}
