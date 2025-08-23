package com.energy.commoncomponent.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppUtils {
    
    public static void runMethodByReflectClass(String className, String methodName) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            method.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
