package xyz.destiall.caramel.app.serialize;

import xyz.destiall.caramel.app.editor.nodes.GraphNode;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;
import xyz.destiall.java.reflection.Reflect;

import java.io.File;
import java.lang.reflect.Type;

public final class NodeSerializer implements JsonDeserializer<GraphNode<?>> {

    @Override
    public GraphNode<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String clazz = jsonElement.getAsJsonObject().get("clazz").getAsString();
        try {
            Class<? extends GraphNode<?>> c = (Class<? extends GraphNode<?>>) Class.forName(clazz);
            return jsonDeserializationContext.deserialize(jsonElement, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
