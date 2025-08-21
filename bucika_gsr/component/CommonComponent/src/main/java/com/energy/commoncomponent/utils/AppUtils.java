package com.energy.commoncomponent.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by fengjibo on 2024/4/24.
 */
public class AppUtils {
    /**
     * 通过反射类执行静态方法
     * @param className
     * @param methodName
     */
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
