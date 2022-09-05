package xyz.destiall.caramel.app.editor.nodes;

import caramel.api.Component;
import caramel.api.components.Blueprint;

import java.lang.reflect.Method;

public class ComponentGraphNode extends GraphNode<Component> {
    public Method method;
    public Component component;
    public ComponentGraphNode(final int nodeId) {
        super(nodeId);
    }

    @Override
    public Component getValue() {
        return component;
    }

    @Override
    public void setValue(final Component component) {
        this.component = component;
    }

    public void setMethod(final String methodName) {
        try {
            method = component.getClass().getMethod(methodName);
            method.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(final Blueprint script, final Graph graph) {
        try {
            method.invoke(script);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
