package org.wolfsonrobotics.RobotWebServer.communication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;

public class CommunicationLayer {
    
    private final FakeRobot instance;
    private Method[] methods;

    public CommunicationLayer(FakeRobot instance) {
        this.instance = instance;
    }

    public CommunicationLayer(FakeRobot instance, Method[] methods) {
        this(instance);
        this.methods = methods; //TODO: add the ability exclude methods as well
    }

    public void call(String inputName, Object... args) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method method = instance.getClass().getMethod(inputName,
                Arrays.stream(args).map(obj -> {
                    // Convert the wrapper classes to their primitive counterparts, as if
                    // a method has primitive parameters, then wrapper classes will not
                    // qualify for finding the method with those primitive parameters
                    Class<?> clazz = obj.getClass();
                    if (clazz.equals(Integer.class)) {
                        return int.class;
                    } else if (clazz.equals(Double.class)) {
                        return double.class;
                    } else if (clazz.equals(Long.class)) {
                        return long.class;
                    } else if (clazz.equals(Float.class)) {
                        return float.class;
                    } else if (clazz.equals(Short.class)) {
                        return short.class;
                    } else if (clazz.equals(Byte.class)) {
                        return byte.class;
                    } else if (clazz.equals(Boolean.class)) {
                        return boolean.class;
                    } else if (clazz.equals(Character.class)) {
                        return char.class;
                    } else {
                        return clazz;
                    }
                }).toArray(Class[]::new));
        method.invoke(instance, args);
    }

    public void call(String inputName, List<Object> args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        call(inputName, args.toArray());
    }

    public Method[] getCallableMethods() {
        if (methods != null) { //returns specified methods instead
            return methods;
        }
        Method[] methods = instance.getClass().getDeclaredMethods();
        return Arrays.stream(methods).filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())).toArray(Method[]::new);
    }


}
