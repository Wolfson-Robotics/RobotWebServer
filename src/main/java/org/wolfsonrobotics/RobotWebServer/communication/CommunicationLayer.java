package org.wolfsonrobotics.RobotWebServer.communication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

import org.json.JSONObject;

public class CommunicationLayer {
    
    private final Object instance;
    private TreeMap<String, Method> inputs = new TreeMap<>();

    public CommunicationLayer(Object instance) {
        this.instance = instance;
    }

    private void call(String inputName, Object... args) throws IllegalAccessException, InvocationTargetException {
        if (!inputs.containsKey(inputName)) { return; }

        inputs.get(inputName).invoke(instance, args);
    }
/*
    public void createInput(String inputName, Method method) {
        inputs.put(inputName, method);
    }

    public Collection<Method> getInstanceMethods() {
        return inputs.values();
    }*/
    
    public Method[] getCallableMethods() {
        Method[] methods = instance.getClass().getDeclaredMethods();
        return Arrays.stream(methods).filter(m -> {
            return Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers());
        }).toArray(Method[]::new);
    }

    public Method getInstanceMethod(String methodName) throws InvalidParameterException {
        Method[] methods = instance.getClass().getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.toString().contains(methodName)) { return method; }
        }

        throw new InvalidParameterException();
    }

    public JSONObject getJSONInputs() {
        JSONObject json = new JSONObject();
        
        for(String name : inputs.keySet()) {
            json.put(name, inputs.get(name));
        }

        return json;
    }
    


}
