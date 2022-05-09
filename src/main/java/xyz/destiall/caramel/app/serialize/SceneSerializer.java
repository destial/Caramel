package xyz.destiall.caramel.app.serialize;

import com.google.gson.*;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.editor.Scene;

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
        return null;
    }

    @Override
    public JsonElement serialize(Scene scene, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;
    }
}
