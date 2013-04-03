package com.njkremer.Sqlite.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.njkremer.Sqlite.SqlStatement;

/**
 * Allows for specifying a field as a Primary key on a POJO that "maps" to a database table. This allows for not
 * needing a "where clause" when using {@linkplain SqlStatement#update(Object)} and
 * {@linkplain SqlStatement#delete(Object)}
 * 
 * <p>Note that this does not create the primary key in the database, that needs to be handled by the database when
 * creating the database table.
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {

}
