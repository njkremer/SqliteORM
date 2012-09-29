package com.kremerk.Sqlite.utils;

import org.springframework.aop.support.AopUtils;

public class SqliteUtils {
    public static Class<?> getClass(Object object) {
        return AopUtils.getTargetClass(object);
    }
    
    public static String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
    
    public static String lowercase(String word) {
        return word.substring(0, 1).toLowerCase() + word.substring(1);
    }
}
