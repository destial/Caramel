package xyz.destiall.caramel.app.serialize;

import com.google.gson.*;
import xyz.destiall.caramel.api.Component;

import java.lang.reflect.Type;

public class ComponentSerializer implements JsonSerializer<Component>, JsonDeserializer<Component> {
    private final Gson defaultGson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public Component deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {

        return null;
    }
}
