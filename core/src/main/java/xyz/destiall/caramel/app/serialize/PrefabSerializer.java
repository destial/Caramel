package xyz.destiall.caramel.app.serialize;

import caramel.api.Component;
import caramel.api.components.Transform;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.objects.Prefab;
import caramel.api.objects.PrefabImpl;
import caramel.api.render.Renderer;
import xyz.destiall.java.gson.JsonArray;
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

public final class PrefabSerializer implements JsonSerializer<Prefab>, JsonDeserializer<Prefab> {

    @Override
    public Prefab deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        Prefab prefab = new PrefabImpl(new File(object.get("file").getAsString()));
        prefab.name.set(object.get("name").getAsString());
        prefab.id = object.get("id").getAsInt();

        JsonArray components = object.get("components").getAsJsonArray();
        for (JsonElement c : components) {
            if (!c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, prefab);
            Reflect.setDeclaredField(prefab, "transform", component);
            prefab.addComponent(component);
            break;
        }

        for (JsonElement c : components) {
            if (c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, prefab);
            if (component instanceof Renderer) {
                Renderer renderer = (Renderer) component;
                renderer.build();
            }
            prefab.addComponent(component);
        }

        JsonArray children = object.get("children").getAsJsonArray();
        for (JsonElement c : children) {
            GameObject child = SceneSerializer.GAME_OBJECT_SERIALIZER.deserialize(c, GameObjectImpl.class, jsonDeserializationContext);
            child.parent = prefab.transform;
            prefab.children.add(child);
        }

        return prefab;
    }

    @Override
    public JsonElement serialize(Prefab prefab, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray components = new JsonArray();
        for (Component component : prefab.getComponents()) {
            components.add(SceneSerializer.GSON.toJsonTree(component));
        }
        JsonArray children = new JsonArray();
        for (GameObject child : prefab.children) {
            children.add(SceneSerializer.GSON.toJsonTree(child));
        }
        JsonObject object = new JsonObject();
        object.add("components", components);
        object.add("children", children);
        object.addProperty("name", prefab.name.get());
        object.addProperty("id", prefab.id);
        object.addProperty("file", prefab.getFile().getPath());
        return object;
    }
}
