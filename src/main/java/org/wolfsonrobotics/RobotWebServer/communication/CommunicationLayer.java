package org.wolfsonrobotics.RobotWebServer.communication;

import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class CommunicationLayer {
    
    private final FakeRobot instance;
    private Method[] methods;
    private Method[] excludedMethods;

    public CommunicationLayer(FakeRobot instance) {
        this.instance = instance;
    }

    public CommunicationLayer(FakeRobot instance, Method[] methods, Method[] excludedMethods) {
        this(instance);
        this.methods = methods; 
        this.excludedMethods = excludedMethods;
    }

    public void call(String inputName, Map<Class<?>, Object> args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = instance.getClass().getMethod(inputName, args.keySet().toArray(new Class[0]));
        method.invoke(instance, args.values().toArray());
    }
    public void callMethod(String inputName, Map<String, Object> args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // We cannot return a SimpleEntry and use Collectors.toMap to convert it to a
        // Map in the stream because of a type error
        Map<Class<?>, Object> castedArgs = new HashMap<>();
        args.forEach((k, v) -> {
            Class<?> argType;
            Number numV = v instanceof Number ? ((Number) v) : null;
            Object castedArg = v;

            switch (k) {
                case "int":
                    argType = int.class;
                    castedArg = numV.doubleValue();
                    break;
                case "double":
                    argType = double.class;
                    castedArg = numV.doubleValue();
                    break;
                case "float":
                    argType = float.class;
                    castedArg = numV.floatValue();
                    break;
                case "long":
                    argType = long.class;
                    castedArg = numV.longValue();
                    break;
                case "short":
                    argType = short.class;
                    castedArg = numV.shortValue();
                    break;
                case "byte":
                    argType = byte.class;
                    castedArg = numV.byteValue();
                    break;
                case "char":
                    argType = char.class;
                    castedArg = (char) numV.intValue();
                    break;
                case "String":
                    argType = String.class;
                    break;
                default:
                    argType = Object.class;
                    break;
            }
            castedArgs.put(argType, castedArg);

        });
        call(inputName, castedArgs);

    }

    @Deprecated
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
    @Deprecated
    public void call(String inputName, List<Object> args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        call(inputName, args.toArray());
    }


    public Method[] getCallableMethods() {
        Method[] methods = this.methods;

        if (methods == null) { //gets all declared methods
            methods = instance.getClass().getDeclaredMethods();
            methods = Arrays.stream(methods)
                    .filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()))
                    .toArray(Method[]::new);
        }
        
        if (excludedMethods == null) {
            return methods;
        }
        //exclude methods
        Set<Method> excludedMethodSet = new HashSet<>(Arrays.asList(excludedMethods));
        List<Method> finalMethods = Arrays.stream(methods)
                .filter(method -> !excludedMethodSet.contains(method))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return finalMethods.toArray(new Method[finalMethods.size()]);
    }


}
