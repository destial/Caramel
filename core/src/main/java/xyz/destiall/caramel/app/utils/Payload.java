package xyz.destiall.caramel.app.utils;

import caramel.api.Component;
import caramel.api.objects.GameObject;

import java.util.HashSet;
import java.util.Set;

public final class Payload {
    public static final String DRAG_DROP_GAMEOBJECT_HIERARCHY = "DRAG_DROP_GAMEOBJECT_HIERARCHY";
    public static final String DRAG_DROP_GAMEOBJECT_INSPECTOR = "DRAG_DROP_GAMEOBJECT_INSPECTOR";
    public static final String DRAG_DROP_PREFAB_SCENE = "DRAG_DROP_PREFAB_SCENE";

    public static final Set<GameObject> COPIED_GAMEOBJECTS = new HashSet<>();
    public static final Set<Class<? extends Component>> COMPONENTS = new HashSet<>();
}
