package xyz.destiall.caramel.app.serialize;

import caramel.api.Component;
import caramel.api.objects.SceneImpl;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

public final class FieldComponentSerializer implements JsonSerializer<Component>, JsonDeserializer<Component> {

    @Override
    public Component deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        int id = object.get("id").getAsInt();
        String clazz = object.get("clazz").getAsString();
        String scene = object.get("scene").getAsString();
        Map.Entry<SceneImpl, Map<Integer, Component>> entry = SceneSerializer.COMPONENT_MAP.entrySet().stream().filter(en -> en.getKey().name.equals(scene)).findFirst().orElse(null);
        if (entry == null) return null;
        Map<Integer, Component> componentMap = entry.getValue();
        if (componentMap.containsKey(id)) {
            return componentMap.get(id);
        }
        return null;
    }

    @Override
    public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("id", component.id);
        object.addProperty("clazz", component.getClass().getName());
        object.addProperty("scene", component.gameObject.scene.name);
        return object;
    }
}
