package org.wolfsonrobotics.RobotWebServer.communication;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class CommunicationLayer {

    private final Object instance;
    private Method[] providedMethods;
    private Set<Method> excludedMethods;

    public CommunicationLayer(Object instance) {
        this.instance = instance;
    }

    public CommunicationLayer(Object instance, Method[] providedMethods, Method[] excludedMethods) {
        this(instance);
        this.providedMethods = providedMethods;
        this.excludedMethods = new HashSet<>(Arrays.asList(excludedMethods));
    }
    public CommunicationLayer(Object instance, String[] providedMethods, String[] excludedMethods) {
        this(instance, stringToMethod(instance, providedMethods), stringToMethod(instance, excludedMethods));
    }


    private static Method stringToMethod(Object instance, String methodName) {
        if (methodName == null) return null;
        try {
            return instance.getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    private static Method[] stringToMethod(Object instance, String[] methodNames) {
        if (methodNames == null) return null;
        return Arrays.stream(methodNames)
                .map(m -> CommunicationLayer.stringToMethod(instance, m))
                .filter(Objects::nonNull)
                .toArray(Method[]::new);
    }

    public String getName() {
        return instance.getClass().getSimpleName();
    }

    public boolean instanceOf(Class<?> clazz) {
        return instance.getClass().equals(clazz);
    }

    private Method[] getInstanceMethods() {
        return Arrays.stream(instance.getClass().getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()))
                .toArray(Method[]::new);
    }

    public Method[] getCallableMethods() {
        Method[] methods = Optional.ofNullable(this.providedMethods).orElseGet(this::getInstanceMethods);
        if (excludedMethods == null) {
            return methods;
        }
        // exclude methods
        return Arrays.stream(methods)
                .filter(method -> !excludedMethods.contains(method))
                .filter(Objects::nonNull)
                .toArray(Method[]::new);

    }

    public String[] getFields() {
        return Arrays.stream(instance.getClass().getDeclaredFields()).map(Field::getName).toArray(String[]::new);
    }
    public Object getField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }
    public CommunicationLayer getFieldLayer(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return new CommunicationLayer(getField(fieldName));
    }


    public Object call(String inputName, MethodArg... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = instance.getClass().getMethod(inputName, Arrays.stream(args).map(MethodArg::type).toArray(Class[]::new));
        return method.invoke(instance, Arrays.stream(args).map(MethodArg::arg).toArray());
    }
    public Object call(String inputName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return call(inputName, new MethodArg[] {});
    }
    public Object callNoThrows(String inputName) {
        try { return call(inputName); } catch (Exception e) { return null; }
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


}
