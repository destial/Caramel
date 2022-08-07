package xyz.destiall.caramel.app.serialize;

import caramel.api.Component;
import caramel.api.components.Camera;
import caramel.api.components.EditorCamera;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.objects.Prefab;
import caramel.api.objects.PrefabImpl;
import caramel.api.objects.SceneImpl;
import caramel.api.utils.FileIO;
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
import java.util.HashMap;
import java.util.Map;

public final class SceneSerializer implements JsonSerializer<SceneImpl>, JsonDeserializer<SceneImpl> {
    public static final GameObjectSerializer GAME_OBJECT_SERIALIZER = new GameObjectSerializer();
    public static final PrefabSerializer PREFAB_SERIALIZER = new PrefabSerializer();
    public static final ComponentSerializer COMPONENT_SERIALIZER = new ComponentSerializer();

    public static final Map<SceneImpl, Map<Integer, Component>> COMPONENT_MAP = new HashMap<>();

    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GameObject.class, GAME_OBJECT_SERIALIZER)
            .registerTypeAdapter(Prefab.class, PREFAB_SERIALIZER)
            .registerTypeAdapter(Component.class, COMPONENT_SERIALIZER)
            .registerTypeAdapter(File.class, new FileSerializer())
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .create();

    @Override
    public SceneImpl deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        SceneImpl scene = new SceneImpl();
        COMPONENT_MAP.put(scene, new HashMap<>());
        JsonObject object = jsonElement.getAsJsonObject();
        scene.name = object.get("name").getAsString();

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
