package com.njkremer.Sqlite.utils;

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
    
    public static boolean isEmpty(Object object) {
        if(object == null) {
            return true;
        }
        if (object instanceof String) {
            return ((String) object).trim().length() == 0;
        }
        else if (object instanceof Float) {
            return ((Float) object) == 0.0f;
        }
        else if (object instanceof Integer) {
            return ((Integer) object) == 0;
        }
        else if (object instanceof Long) {
            return ((Long) object) == 0l;
        }
        else if (object instanceof Double) {
            return ((Double) object) == 0.0d;
        }
        else if (object instanceof Boolean) {
            return ((Boolean) object) == false;
        }
        return false;
    }
}
