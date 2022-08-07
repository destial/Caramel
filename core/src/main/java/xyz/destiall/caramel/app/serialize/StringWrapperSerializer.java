package xyz.destiall.caramel.app.serialize;

import caramel.api.interfaces.StringWrapper;
import caramel.api.objects.StringWrapperImpl;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonPrimitive;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;

import java.io.File;
import java.lang.reflect.Type;

public final class StringWrapperSerializer implements JsonSerializer<StringWrapper>, JsonDeserializer<StringWrapper> {

    @Override
    public StringWrapper deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String string = jsonElement.getAsString();
        return new StringWrapperImpl(string);
    }

    @Override
    public JsonElement serialize(StringWrapper string, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(string.get());
    }
}
