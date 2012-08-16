package com.kremerk.Sqlite.utils;

import org.springframework.aop.support.AopUtils;

public class SqliteUtils {
    public static Class<?> getClass(Object object) {
        return AopUtils.getTargetClass(object);
    }
}
