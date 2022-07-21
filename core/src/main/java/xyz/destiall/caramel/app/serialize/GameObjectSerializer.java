package xyz.destiall.caramel.app.serialize;

import caramel.api.Component;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.render.MeshRenderer;
import caramel.api.components.Transform;
import caramel.api.objects.SceneImpl;
import xyz.destiall.java.gson.JsonArray;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;

import java.lang.reflect.Type;

public final class GameObjectSerializer implements JsonSerializer<GameObject>, JsonDeserializer<GameObject> {
    public int maxId = 0;

    @Override
    public GameObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return deserialize(null, jsonElement);
    }

    public GameObject deserialize(SceneImpl scene, JsonElement jsonElement) throws JsonParseException {
        GameObject gameObject = new GameObjectImpl();
        gameObject.scene = scene;

        JsonObject object = jsonElement.getAsJsonObject();
        gameObject.name.set(object.get("name").getAsString());
        gameObject.id = object.has("id") ? object.get("id").getAsInt() : ++maxId;

        JsonArray components = object.get("components").getAsJsonArray();
        for (JsonElement c : components) {
            if (!c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, gameObject);
            if (component == null) continue;
            gameObject.transform = (Transform) component;
            gameObject.addComponent(gameObject.transform);
            if (maxId < component.id) {
                maxId = component.id;
            }

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
            if (component == null) continue;
            if (component instanceof MeshRenderer) {
                MeshRenderer renderer = (MeshRenderer) component;
                if (renderer.mesh != null) renderer.mesh.build();
            }
            gameObject.addComponent(component);
            if (maxId < component.id) {
                maxId = component.id;
            }
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
            JsonElement element = SceneSerializer.COMPONENT_SERIALIZER.serialize(component, component.getClass(), jsonSerializationContext);
            components.add(element);
        }
        JsonArray tags = new JsonArray();
        for (String tag : gameObject.tags) {
            tags.add(tag);
        }
        JsonArray children = new JsonArray();
        for (GameObject child : gameObject.children) {
            JsonElement element = SceneSerializer.GAME_OBJECT_SERIALIZER.serialize(child, child.getClass(), jsonSerializationContext);
            children.add(element);
        }
        JsonObject object = new JsonObject();
        object.add("components", components);
        object.add("children", children);
        object.add("tags", tags);
        object.addProperty("name", gameObject.name.get());
        object.addProperty("id", gameObject.id);
        return object;
    }
}
