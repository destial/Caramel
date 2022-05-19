package xyz.destiall.caramel.app.serialize;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.editor.Scene;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;
import xyz.destiall.java.gson.JsonArray;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SceneSerializer implements JsonSerializer<Scene>, JsonDeserializer<Scene> {
    protected static final GameObjectSerializer GAME_OBJECT_SERIALIZER = new GameObjectSerializer();
    protected static final ComponentSerializer COMPONENT_SERIALIZER = new ComponentSerializer();

    protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GameObject.class, GAME_OBJECT_SERIALIZER)
            .registerTypeAdapter(Component.class, COMPONENT_SERIALIZER)
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @Override
    public Scene deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Scene scene = new Scene();
        JsonArray array = jsonElement.getAsJsonObject().get("gameObjects").getAsJsonArray();
        for (JsonElement element : array) {
            GameObject gameObject = GAME_OBJECT_SERIALIZER.deserialize(element, GameObject.class, jsonDeserializationContext);
            gameObject.scene = scene;
            scene.getGameObjects().add(gameObject);
        }
        scene.name = jsonElement.getAsJsonObject().get("name").getAsString();
        return scene;
    }

    @Override
    public JsonElement serialize(Scene scene, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        for (GameObject gameObject : scene.getGameObjects()) {
            array.add(GAME_OBJECT_SERIALIZER.serialize(gameObject, GameObject.class, jsonSerializationContext));
        }
        object.addProperty("name", scene.name);
        object.add("gameObjects", array);
        return object;
    }
}
