package com.topdon.commons.poster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

public class MethodInfo {
    @NonNull
    private String name;
    @Nullable
    private Parameter[] parameters;
    @NonNull
    private String tag;

    public MethodInfo(@NonNull String name, @Nullable Parameter... parameters) {
        this(name, name, parameters);
    }

    public MethodInfo(@NonNull String name, @NonNull String tag, @Nullable Parameter... parameters) {
        this.name = name;
        this.tag = tag;
        this.parameters = parameters;
    }

    public MethodInfo(@NonNull String name, @Nullable Class<?>[] parameterTypes) {
        this(name, name, parameterTypes);
    }

    public MethodInfo(@NonNull String name, @NonNull String tag, @Nullable Class<?>[] parameterTypes) {
        this(name, tag, toParameters(parameterTypes));
    }
    
    public static MethodInfo valueOf(@NonNull Method method) {
        Tag annotation = method.getAnnotation(Tag.class);
        return new MethodInfo(method.getName(), annotation == null ? method.getName() : annotation.value(),
                method.getParameterTypes());
    }

    private static Parameter[] toParameters(Class<?>[] parameterTypes) {
        Parameter[] parameters = null;
        if (parameterTypes != null) {
            parameters = new Parameter[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = new Parameter(parameterTypes[i], null);
            }
        }
        return parameters;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    @Nullable
    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable Parameter[] parameters) {
        this.parameters = parameters;
    }

    @Nullable
    public Class<?>[] getParameterTypes() {
        if (parameters == null) {
            return null;
        } else {
            Class<?>[] types = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                types[i] = parameters[i].type;
            }
            return types;
        }
    }

    @Nullable
    public Object[] getParameterValues() {
        if (parameters == null) {
            return null;
        } else {
            Object[] values = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                values[i] = parameters[i].value;
            }
            return values;
        }
    }

    public static class Parameter {
        @Nullable
        private Object value;
        @NonNull
        private Class<?> type;

        public Parameter(@NonNull Class<?> type, @Nullable Object value) {
            this.type = type;
            this.value = value;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        @NonNull
        public Class<?> getType() {
            return type;
        }

        public void setType(@NonNull Class<?> type) {
            this.type = type;
        }
    }
}
