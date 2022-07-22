package xyz.destiall.caramel.app.serialize;

import xyz.destiall.java.gson.*;

import java.io.File;
import java.lang.reflect.Type;

public final class FileSerializer implements JsonSerializer<File>, JsonDeserializer<File> {

    @Override
    public File deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String path = jsonElement.getAsJsonObject().get("path").getAsString();
        return new File(path);
    }

    @Override
    public JsonElement serialize(File file, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("path", file.getPath());
        return object;
    }
}
