package xyz.destiall.caramel.app.serialize;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.app.ApplicationImpl;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public final class ComponentSerializer implements JsonSerializer<Component>, JsonDeserializer<Component> {
    private final Gson defaultGson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public Component deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String clazz = jsonElement.getAsJsonObject().get("clazz").getAsString();
        System.out.println("Deserializing " + clazz);
        Class<? extends Component> c;
        try {
            c = (Class<? extends Component>) Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            String[] split = clazz.split("\\.");
            String scriptName = split[split.length - 1];
            c = ApplicationImpl.getApp().getScriptManager().getScript(scriptName).getCompiledClass();
            if (c == null) return null;
        }
        return defaultGson.fromJson(jsonElement, c);
    }

    public Component deserialize(JsonElement jsonElement, GameObject gameObject) throws JsonParseException {
        Component gsonComponent = deserialize(jsonElement, null, null);
        if (gsonComponent == null) return null;
        Constructor<Component> constructor = (Constructor<Component>) Reflect.getConstructor(gsonComponent.getClass(), GameObject.class);
        Component component;
        try {
            component = constructor.newInstance(gameObject);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return gsonComponent;
        }
        for (Field field : gsonComponent.getClass().getFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            try {
                field.set(component, field.get(gsonComponent));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (!accessible) field.setAccessible(false);
        }
        component.id = gsonComponent.id;
        return component;
    }

    @Override
    public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
        System.out.println("Serializing " + component.getClass().getName());
        JsonElement element = defaultGson.toJsonTree(component);
        JsonObject object = element.getAsJsonObject();
        object.addProperty("clazz", component.getClass().getName());
        return object;
    }
}
