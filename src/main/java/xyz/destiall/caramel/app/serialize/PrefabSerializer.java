package xyz.destiall.caramel.app.serialize;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.components.MeshRenderer;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.mesh.Mesh;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.Prefab;
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

public class PrefabSerializer implements JsonSerializer<Prefab>, JsonDeserializer<Prefab> {

    @Override
    public Prefab deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        Prefab prefab = (Prefab) Reflect.newInstance(Prefab.class, new File(object.get("file").getAsString()));
        prefab.name = object.get("name").getAsString();
        prefab.id = object.get("id").getAsInt();

        JsonArray components = object.get("components").getAsJsonArray();
        for (JsonElement c : components) {
            if (!c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, prefab);
            Reflect.setDeclaredField(prefab, "transform", component);
            Component.ENTITY_IDS.updateAndGet(i -> Math.max(i, component.id));
            prefab.addComponent(component);
            break;
        }

        for (JsonElement c : components) {
            if (c.getAsJsonObject().get("clazz").getAsString().equals(Transform.class.getName())) continue;
            Component component = SceneSerializer.COMPONENT_SERIALIZER.deserialize(c, prefab);
            if (component instanceof MeshRenderer) {
                Mesh mesh = ((MeshRenderer) component).mesh;
                if (mesh != null) mesh.build();
            }
            Component.ENTITY_IDS.updateAndGet(i -> Math.max(i, component.id));
            prefab.addComponent(component);
        }
        Component.ENTITY_IDS.updateAndGet(i -> Math.max(i, prefab.id));

        JsonArray children = object.get("children").getAsJsonArray();
        for (JsonElement c : children) {
            GameObject child = SceneSerializer.GAME_OBJECT_SERIALIZER.deserialize(c, GameObject.class, jsonDeserializationContext);
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
        object.addProperty("name", prefab.name);
        object.addProperty("id", prefab.id);
        object.addProperty("file", prefab.getFile().getPath());
        return object;
    }
}