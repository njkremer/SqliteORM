package com.njkremer.Sqlite.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows for specifying a field as a One To Many relationship. This would typically be used on a list property of your
 * SQL POJO class. The parameter of the annotation is the foreign key to the current class object in the target list's type.
 * So if a Thing class was tied to a user it would have a foreign key to the user table in the database that could be named
 * userId, so then you'd use <code>@OneToMany(userId)</code> in your annotation.
 * 
 * <p>For Example:
 * <pre>
 * public class User {
 * 
 *   &#064;PrimaryKey
 *   private int id;
 *
 *   &#064;OneToMany("userId")
 *   private List&lt;Thing&gt; things;
 * }
 * </pre>
 * 
 * Also note that to use this you must have a {@link PrimaryKey} defined in class object that is also defining the OneToMany
 * relationship, in the above example, the <b>User</b> class would need a primary key defined.
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    String value();
}
