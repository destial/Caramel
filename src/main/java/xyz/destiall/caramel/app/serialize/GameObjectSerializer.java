package xyz.destiall.caramel.app.serialize;

import com.google.gson.*;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.java.reflection.Reflect;

import java.lang.reflect.Type;

public class GameObjectSerializer implements JsonSerializer<GameObject>, JsonDeserializer<GameObject> {

    @Override
    public GameObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        GameObject gameObject = new GameObject(Application.getApp().getCurrentScene());

        JsonArray components = object.get("components").getAsJsonArray();
        for (JsonElement c : components) {
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, type, jsonDeserializationContext);
            if (component instanceof Transform) {
                Reflect.setDeclaredField(gameObject, "transform", component);
            }
            gameObject.addComponent(component);
        }

        JsonArray children = object.get("children").getAsJsonArray();
        for (JsonElement c : children) {
            GameObject child = SceneSerializer.GAME_OBJECT_SERIALIZER.deserialize(c, type, jsonDeserializationContext);
            gameObject.children.add(child);
            child.parent = gameObject.transform;
        }

        gameObject.name = object.get("name").getAsString();
        return gameObject;
    }

    @Override
    public JsonElement serialize(GameObject gameObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray components = new JsonArray();
        for (Component component : gameObject.getComponents()) {
            components.add(SceneSerializer.GSON.toJsonTree(component));
        }
        JsonArray children = new JsonArray();
        for (GameObject child : gameObject.children) {
            children.add(SceneSerializer.GSON.toJsonTree(child));
        }
        JsonObject object = new JsonObject();
        object.add("components", components);
        object.add("children", children);
        object.addProperty("name", gameObject.name);
        object.addProperty("scene", gameObject.scene.name);

        return null;
    }
}
