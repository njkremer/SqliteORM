package com.njkremer.Sqlite;

import com.njkremer.Sqlite.Annotations.PrimaryKey;

/**
 * The main way to interact with the database. This is designed to work with POJOs (Plain Old Java Objects) that
 * "map" to tables in a SQLite Database. This class is really only used to start an statement to work with the
 * database. A {@linkplain SqlExecutor} is used to write out the rest of the statement.
 * 
 * <p> Typically a {@linkplain SqlExecutor} isn't created on it's own to interact with the database since this
 * class creates a new instance of a {@linkplain SqlExecutor} that is returned by all of the methods of this class.
 * 
 */
public class SqlStatement {
    
    /**
     * Returns a new instance of {@linkplain SqlExecutor} to be used for retrieving an Object out of the database.
     * 
     * @param clazz A reference to the Object.class that you are retrieving from the database.
     * @return new {@linkplain SqlExecutor} used to retrieve an object from the database.
     * @throws DataConnectionException 
     */
    public static <T> SqlExecutor<T> select(Class<T> clazz) throws DataConnectionException {
        return new SqlExecutor<T>().select(clazz);
    }

    /**
     * Returns a new instance of {@linkplain SqlExecutor} to be used for updating an Object in the database.
     * 
     * <p> If you specify a {@linkplain PrimaryKey} on your POJO then a "where clause" is not needed when updating
     * an object instead it will use the {@linkplain PrimaryKey} to auto generate a where clause.
     * 
     * @param databaseObject An object that was queried from the database that has been updated.
     * @return new {@linkplain SqlExecutor} used to update an object in the database.
     * @throws DataConnectionException
     */
    public static <T> SqlExecutor<T> update(T databaseObject) throws DataConnectionException {
        return new SqlExecutor<T>().update(databaseObject);
    }

    /**
     * Returns a new instance of {@linkplain SqlExecutor} to be used for inserting an Object into the database.
     * 
     * <p> Note that the values that are set on the passed in POJO will be used for creating the record in the
     * database. If a value isn't set on the POJO than the default Java value will be used (e.g. 0 for int, null
     * for an Object, etc).
     * 
     * @param databaseObject An object that is to be input into the database.
     * @return new {@linkplain SqlExecutor} used to insert an object into the database.
     * @throws DataConnectionException
     */
    public static <T> SqlExecutor<T> insert(T databaseObject) throws DataConnectionException {
        return new SqlExecutor<T>().insert(databaseObject);
    }

    /**
     * Returns a new instance of {@linkplain SqlExecutor} to be used for deleting an record in the database.
     * 
     * @param clazz A reference to the Object.class that you are deleting from the database.
     * @return new {@linkplain SqlExecutor} used to delete a record in the database.
     * @throws DataConnectionException
     */
    public static <T> SqlExecutor<T> delete(Class<T> clazz) throws DataConnectionException {
        return new SqlExecutor<T>().delete(clazz);
    }

    /**
     * Returns a new instance of {@linkplain SqlExecutor} to be used for deleting an Object in the database.
     * 
     * <p> If you specify a {@linkplain PrimaryKey} on your POJO then a "where clause" is not needed when deleting
     * an object instead it will use the {@linkplain PrimaryKey} to auto generate a where clause.
     * 
     * @param databaseObject An object that should be deleted from the database.
     * @return new {@linkplain SqlExecutor} used to delete an object in the database.
     * @throws DataConnectionException
     */
    public static <T> SqlExecutor<T> delete(T databaseObject) throws DataConnectionException {
        return new SqlExecutor<T>().delete(databaseObject);
    }
    
    /**
     * @deprecated As of 8/13/2012 the creation of a SqlStatement is deprecated in favor of using the static methods off of the 
     * {@link SqlStatement} class. The methods will continue to work but you will get compiler warnings to access the methods
     * in a static way.
     */
    @Deprecated
    public SqlStatement() {
        
    }

}
