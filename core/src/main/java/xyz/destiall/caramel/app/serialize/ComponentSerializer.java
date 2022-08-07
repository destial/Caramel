package xyz.destiall.caramel.app.serialize;

import caramel.api.Component;
import caramel.api.debug.Debug;
import caramel.api.debug.DebugImpl;
import caramel.api.interfaces.StringWrapper;
import caramel.api.objects.GameObject;
import caramel.api.objects.StringWrapperImpl;
import caramel.api.scripts.InternalScript;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.nodes.GraphNode;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;
import xyz.destiall.java.gson.JsonDeserializationContext;
import xyz.destiall.java.gson.JsonDeserializer;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParseException;
import xyz.destiall.java.gson.JsonSerializationContext;
import xyz.destiall.java.gson.JsonSerializer;
import xyz.destiall.java.reflection.Reflect;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ComponentSerializer implements JsonSerializer<Component>, JsonDeserializer<Component> {
    private final Gson defaultGson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(File.class, new FileSerializer())
            .registerTypeAdapter(StringWrapper.class, new StringWrapperSerializer())
            .registerTypeAdapter(StringWrapperImpl.class, new StringWrapperSerializer())
            .registerTypeAdapter(Component.class, FIELD_COMPONENT_SERIALIZER)
            .registerTypeAdapter(GraphNode.class, new NodeSerializer())
            .setPrettyPrinting().create();

    public static final FieldComponentSerializer FIELD_COMPONENT_SERIALIZER = new FieldComponentSerializer();
    public static final Map<Integer, List<UnknownFieldComponent>> FIELD_MAP = new HashMap<>();

    @Override
    public Component deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String clazz = jsonElement.getAsJsonObject().get("clazz").getAsString();
        Class<? extends Component> c;
        try {
            c = (Class<? extends Component>) Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            clazz = clazz.replace("xyz.destiall.", "");
            try {
                c = (Class<? extends Component>) Class.forName(clazz);
            } catch (ClassNotFoundException ee) {
                String[] split = clazz.split("\\.");
                String scriptName = split[split.length - 1];
                InternalScript script = ApplicationImpl.getApp().getScriptManager().getScript(scriptName);
                if (script == null) {
                    DebugImpl.logError("Unable to load class: " + clazz);
                    e.printStackTrace();
                    return null;
                }

                c = script.getCompiledClass();
                if (c == null) {
                    DebugImpl.logError("Unable to load class: " + clazz);
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return defaultGson.fromJson(jsonElement, c);
    }

    public Component deserialize(JsonElement jsonElement, GameObject gameObject) throws JsonParseException {
        Component gsonComponent = deserialize(jsonElement, null, null);
        if (gsonComponent == null) return null;
        Constructor<Component> constructor = (Constructor<Component>) Reflect.getConstructor(gsonComponent.getClass(), GameObject.class);
        JsonObject object = jsonElement.getAsJsonObject();
        Component component;
        try {
            component = constructor.newInstance(gameObject);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            DebugImpl.logError(e.getLocalizedMessage());
            return gsonComponent;
        }
        for (Field field : gsonComponent.getClass().getFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            try {
                Object value = field.get(gsonComponent);
                if (field.getName().equalsIgnoreCase("links")) {
                    System.out.println(value);
                }
                if (Component.class.isAssignableFrom(field.getType())) {
                    JsonElement element = object.get(field.getName());
                    if (element != null) {
                        JsonObject componentField = element.getAsJsonObject();
                        Component fieldDeserialized = FIELD_COMPONENT_SERIALIZER.deserialize(componentField, null, null);
                        if (fieldDeserialized != null) {
                            value = fieldDeserialized;
                        } else {
                            UnknownFieldComponent ufc = new UnknownFieldComponent(field, component);
                            int id = componentField.get("id").getAsInt();
                            FIELD_MAP.putIfAbsent(id, new ArrayList<>());
                            FIELD_MAP.get(id).add(ufc);
                        }
                    }
                }
                field.set(component, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Debug.logError(e.getLocalizedMessage());
            }
            if (!accessible) field.setAccessible(false);
        }
        component.id = gsonComponent.id;
        return component;
    }

    @Override
    public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement element = defaultGson.toJsonTree(component);
        JsonObject object = element.getAsJsonObject();
        for (Field field : component.getClass().getFields()) {
            if (Component.class.isAssignableFrom(field.getType()) && !Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    object.remove(field.getName());
                    Component value = (Component) field.get(component);
                    if (value == null) continue;
                    JsonElement fieldObject = FIELD_COMPONENT_SERIALIZER.serialize(value, null, null);
                    object.add(field.getName(), fieldObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        object.addProperty("clazz", component.getClass().getName());
        return object;
    }
}
