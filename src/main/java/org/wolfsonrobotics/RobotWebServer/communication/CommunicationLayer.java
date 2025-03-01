package org.wolfsonrobotics.RobotWebServer.communication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class CommunicationLayer {
    
    private final Object instance;
    private final String teamName;
    private final int teamNumber;

    public CommunicationLayer(Object instance,  String teamName, int teamNumber) {
        this.instance = instance;
        this.teamName = teamName;
        this.teamNumber = teamNumber;
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
        Method[] methods = instance.getClass().getDeclaredMethods();
        return Arrays.stream(methods).filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())).toArray(Method[]::new);
    }

    public Method getInstanceMethod(String methodName) throws InvalidParameterException {
        Method[] methods = instance.getClass().getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.toString().contains(methodName)) { return method; }
        }

        throw new InvalidParameterException();
    }
    
    public String getTeamName() {
        return teamName;
    }

    public int getTeamNumber() {
        return teamNumber;
    }


}
