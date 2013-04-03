package com.njkremer.Sqlite.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows for specifying a field as Auto Incremented on a POJO that "maps" to a database table.
 * 
 * <p>Note that this does not create the auto increment scheme, that needs to be handled by the database when
 * creating the database table.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoIncrement {

}
