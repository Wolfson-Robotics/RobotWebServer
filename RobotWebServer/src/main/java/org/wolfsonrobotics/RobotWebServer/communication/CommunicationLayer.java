package org.wolfsonrobotics.RobotWebServer.communication;

import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this(instance, stringToMethod(instance, providedMethods), stringToAllMethods(instance, excludedMethods));
    }


    private static List<Method> allMethodsByName(Class<?> clazz, String methodName) {
        if (clazz == null) return new ArrayList<>();
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .collect(Collectors.toList());
    }
    private static Method[] stringToAllMethods(Class<?> clazz, List<Method> methods, String methodName) {
        if (clazz == null) {
            return methods.toArray(new Method[0]);
        }
        if (methodName == null) return null;
        methods.addAll(allMethodsByName(clazz, methodName));
        return stringToAllMethods(clazz.getSuperclass(), methods, methodName);
    }
    private static Method[] stringToAllMethods(Object instance, String methodName) {
        return stringToAllMethods(instance.getClass(), new ArrayList<>(), methodName);
    }
    private static Method[] stringToAllMethods(Object instance, String[] methodNames) {
        if (methodNames == null) return null;
        return Arrays.stream(methodNames)
                .map(m -> CommunicationLayer.stringToAllMethods(instance, m))
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .toArray(Method[]::new);
    }

    private static Method stringToMethod(Class<?> clazz, String methodName) {
        if (clazz == null) return null;
        if (methodName == null) return null;
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            return stringToMethod(clazz.getSuperclass(), methodName);
        }
    }
    private static Method stringToMethod(Object instance, String methodName) {
        return stringToMethod(instance.getClass(), methodName);
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
        return instance != null && !instance.getClass().equals(Object.class) && instance.getClass().isAssignableFrom(clazz);
    }

    private Method[] getInstanceMethods(Class<?> clazz, Stream<Method> stream) {
        if (clazz == null) return stream.toArray(Method[]::new);
        return getInstanceMethods(clazz.getSuperclass(),
                Stream.concat(
                        Optional.ofNullable(stream).orElse(Stream.empty()),
                        Arrays.stream(clazz.getDeclaredMethods())
                            .filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()))
                ));
    }
    private Method[] getInstanceMethods() {
        return getInstanceMethods(instance.getClass(), null);
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

    private Field[] getRawFields(Class<?> clazz, Stream<Field> stream, Set<Class<?>> typeConstraints) {
        if (clazz == null) return stream.toArray(Field[]::new);
        return getRawFields(
                clazz.getSuperclass(),
                Stream.concat(
                    stream,
                    Arrays.stream(clazz.getDeclaredFields())
                            .filter(f -> typeConstraints.isEmpty() || typeConstraints.contains(f.getType()))
                            .peek(f -> f.setAccessible(true))
                ),
                typeConstraints);
    }
    private Field[] getRawFields(Class<?> clazz, Class<?>[] typeConstraints) {
        return getRawFields(clazz, Stream.empty(), Arrays.stream(Optional.ofNullable(typeConstraints).orElse(new Class<?>[0])).collect(Collectors.toSet()));
    }
    public Field[] getRawFields(Class<?>[] typeConstraints) {
        return getRawFields(instance.getClass(), typeConstraints);
    }
    public Field[] getRawFields() {
        return getRawFields(null);
    }

    public String[] getFields(Class<?>[] typeConstraints) {
        return Arrays.stream(getRawFields(typeConstraints))
                .map(Field::getName)
                .toArray(String[]::new);
    }
    public String[] getFields() {
        return getFields(null);
    }

    // Recursive function to check for fields in parent classes that are in the instance
    private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() == null) throw e;
            return getField(clazz.getSuperclass(), fieldName);
        }
    }
    public Object getField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(instance.getClass(), fieldName);
        field.setAccessible(true);
        return getField(field);
    }
    public Object getField(Field field) throws IllegalAccessException {
        return field.get(instance);
    }

    public CommunicationLayer getFieldLayer(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return new CommunicationLayer(getField(fieldName));
    }


    private Object call(Class<?> clazz, String inputName, MethodArg... args) throws InvocationTargetException, IllegalAccessException, BadInputException {
        Method method;
        try {
            method = instance.getClass().getMethod(inputName, Arrays.stream(args).map(MethodArg::type).toArray(Class[]::new));
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() == null) return e;
            return call(clazz.getSuperclass(), inputName, args);
        }

        List<Object> argObjs = new ArrayList<>();
        // For loop instead of Array streams to catch errors
        for (MethodArg arg : args) {
            Object argObj = arg.arg();
            // If the value is a String but the type is not, this indicates that
            // it is referring to an externally defined variable, i.e. a class member
            if (!arg.type().equals(String.class) && argObj instanceof String) {
                try {
                    argObjs.add(getField((String) argObj));
                    continue;
                } catch (NoSuchFieldException e) {
                    // In the event that a name is passed representing an externally defined variable
                    throw new BadInputException(argObj + " is not defined");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            argObjs.add(argObj);
        }

        return method.invoke(instance, argObjs);
    }
    public Object call(String inputName, MethodArg... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, BadInputException {
        return call(instance.getClass(), inputName, args);
    }
    public Object call(String inputName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, BadInputException {
        return instance.getClass().getMethod(inputName).invoke(instance);
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
