package xyz.destiall.caramel.app.serialize;

import xyz.destiall.caramel.api.ComponentImpl;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.api.objects.Prefab;
import xyz.destiall.caramel.api.objects.PrefabImpl;
import xyz.destiall.caramel.app.editor.EditorCamera;
import xyz.destiall.caramel.app.editor.SceneImpl;
import xyz.destiall.caramel.api.utils.FileIO;
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

import java.io.File;
import java.lang.reflect.Type;

public final class SceneSerializer implements JsonSerializer<SceneImpl>, JsonDeserializer<SceneImpl> {
    static final GameObjectSerializer GAME_OBJECT_SERIALIZER = new GameObjectSerializer();
    private static final PrefabSerializer PREFAB_SERIALIZER = new PrefabSerializer();
    static final ComponentSerializer COMPONENT_SERIALIZER = new ComponentSerializer();

    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GameObjectImpl.class, GAME_OBJECT_SERIALIZER)
            .registerTypeAdapter(PrefabImpl.class, PREFAB_SERIALIZER)
            .registerTypeAdapter(ComponentImpl.class, COMPONENT_SERIALIZER)
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .create();

    @Override
    public SceneImpl deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        SceneImpl scene = new SceneImpl();
        JsonObject object = jsonElement.getAsJsonObject();
        JsonArray fabs = object.get("prefabs").getAsJsonArray();
        for (JsonElement element : fabs) {
            String path = element.getAsString();
            File file = new File(path);
            if (file.exists()) {
                PrefabImpl prefab = GSON.fromJson(FileIO.readData(file), PrefabImpl.class);
                scene.getPrefabs().add(prefab);
            }
        }

        JsonArray array = object.get("gameObjects").getAsJsonArray();
        for (JsonElement element : array) {
            GameObject gameObject = GAME_OBJECT_SERIALIZER.deserialize(scene, element);
            scene.getGameObjects().add(gameObject);

            if (gameObject.hasComponent(Camera.class)) {
                scene.setGameCamera(gameObject.getComponent(Camera.class));
            }
        }

        scene.name = object.get("name").getAsString();

        EditorCamera camera = GAME_OBJECT_SERIALIZER.deserialize(scene, object.get("editorCamera")).getComponent(EditorCamera.class);
        scene.setEditorCamera(camera);

        scene.entityIds.set(GAME_OBJECT_SERIALIZER.maxId);
        return scene;
    }

    @Override
    public JsonElement serialize(SceneImpl scene, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        for (GameObject gameObject : scene.getGameObjects()) {
            array.add(GAME_OBJECT_SERIALIZER.serialize(gameObject, GameObjectImpl.class, jsonSerializationContext));
        }
        object.addProperty("name", scene.name);
        object.add("gameObjects", array);

        JsonElement editorCamera = GAME_OBJECT_SERIALIZER.serialize(scene.getEditorCamera().gameObject, GameObjectImpl.class, jsonSerializationContext);
        object.add("editorCamera", editorCamera);

        JsonArray prefabArray = new JsonArray();
        for (Prefab prefab : scene.getPrefabs()) {
            prefabArray.add(prefab.getFile().getPath());
            FileIO.writeData(prefab.getFile(), GSON.toJson(prefab));
        }
        object.add("prefabs", prefabArray);
        return object;
    }
}
