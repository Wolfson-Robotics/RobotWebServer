package org.wolfsonrobotics.RobotWebServer.communication;

public class MethodArg {

    private final Class<?> type;
    private final Object arg;

    public MethodArg(Class<?> type, Object arg) {
        this.type = type;
        this.arg = arg;
    }


    public static MethodArg of(String type, Object arg) {
        Class<?> argType;
        Number numArg = arg instanceof Number ? ((Number) arg) : null;
        Object castedArg = arg;

        switch (type) {
            case "int":
                argType = int.class;
                castedArg = numArg.intValue();
                break;
            case "double":
                argType = double.class;
                castedArg = numArg.doubleValue();
                break;
            case "float":
                argType = float.class;
                castedArg = numArg.floatValue();
                break;
            case "long":
                argType = long.class;
                castedArg = numArg.longValue();
                break;
            case "short":
                argType = short.class;
                castedArg = numArg.shortValue();
                break;
            case "byte":
                argType = byte.class;
                castedArg = numArg.byteValue();
                break;
            case "char":
                argType = char.class;
                castedArg = (char) numArg.intValue();
                break;
            case "String":
                argType = String.class;
                break;
            default:
                argType = Object.class;
                break;
        }
        return new MethodArg(argType, castedArg);
    }


    public Class<?> type() {
        return this.type;
    }
    public Object arg() {
        return this.arg;
    }
}
