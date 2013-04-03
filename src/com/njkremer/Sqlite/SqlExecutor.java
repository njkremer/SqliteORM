package com.njkremer.Sqlite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;

import com.njkremer.Sqlite.Annotations.AutoIncrement;
import com.njkremer.Sqlite.Annotations.OneToMany;
import com.njkremer.Sqlite.Annotations.PrimaryKey;
import com.njkremer.Sqlite.JoinExecutor.JoinType;
import com.njkremer.Sqlite.utils.DateUtils;
import com.njkremer.Sqlite.utils.SqliteUtils;

/**
 * Used to continue a {@linkplain SqlStatement} to interact with the database.
 * 
 * <p>Typically this class is not instantiated outright, instead use {@linkplain SqlStatement#select(Class)},
 * {@linkplain SqlStatement#update(Object)}, {@linkplain SqlStatement#insert(Object)},
 * {@linkplain SqlStatement#delete(Class)}, or {@linkplain SqlStatement#delete(Object)} to create an instance of
 * this class.
 * 
 * @param <T> A class that is a POJO that "maps" to a table in the database.
 */
public class SqlExecutor<T> {

    /**
     * Used for retrieving an Object out of the database.
     * 
     * @param clazz A reference to the Object.class that you are retrieving from the database.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     * @throws DataConnectionException 
     */
    public SqlExecutor<T> select(Class<T> clazz) throws DataConnectionException {
        reset();
        this.clazz = clazz;
        queryParts.put(StatementParts.SELECT, String.format(SELECT, clazz.getSimpleName().toLowerCase()));
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.SELECT;
        return this;
    }

    /**
     * Used for updating an Object in the database.
     * 
     * @param databaseObject An object that was queried from the database that has been updated.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     * @throws DataConnectionException 
     */
    @SuppressWarnings("unchecked")
    public SqlExecutor<T> update(Object databaseObject) throws DataConnectionException {
        reset();
        clazz = (Class<T>) SqliteUtils.getClass(databaseObject);
        sqlObject = databaseObject;
        queryParts.put(StatementParts.UPDATE, String.format(UPDATE, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.UPDATE;
        return this;
    }

    /**
     * Used for inserting an Object into the database.
     * 
     * @param databaseObject An object that is to be input into the database.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    @SuppressWarnings("unchecked")
    public SqlExecutor<T> insert(T databaseObject) throws DataConnectionException {
        reset();
        clazz = (Class<T>) SqliteUtils.getClass(databaseObject);
        queryParts.put(StatementParts.INSERT, String.format(INSERT, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.INSERT;
        sqlObject = databaseObject;
        try {
            prepareInsert();
        }
        catch (Exception e) {
            throw new DataConnectionException("Error running insert", e);
        }
        return this;
    }

    /**
     * Used for deleting an record in the database.
     * 
     * @param clazz A reference to the Object.class that you are deleting from the database.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     * @throws DataConnectionException
     */
    public SqlExecutor<T> delete(Class<T> clazz) throws DataConnectionException {
        reset();
        this.clazz = clazz;
        queryParts.put(StatementParts.DELETE, DELETE);
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.DELETE;
        return this;
    }

    /**
     * Used for deleting an Object in the database.
     * 
     * @param databaseObject An object that should be deleted from the database.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     * @throws DataConnectionException
     */
    @SuppressWarnings("unchecked")
    public SqlExecutor<T> delete(T databaseObject) throws DataConnectionException {
        reset();
        clazz = (Class<T>) SqliteUtils.getClass(databaseObject);
        sqlObject = databaseObject;
        queryParts.put(StatementParts.DELETE, DELETE);
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.DELETE;
        return this;
    }
    

    /**
     * Used to do a SQL Join with another POJO/Table.
     * 
     * <p>A simple example of using joining to get all of a user's things, given the user's name:
     * 
     * <p>
     * <pre>
     * new SqlStatement().select(Thing.class).join(User.class, "id", Thing.class, "userId")
     *                                       .where(User.class, "name").eq("Bob").getList()
     *</pre> 
     * 
     * @param leftClazz The (left) class/table you're joining to.
     * @param leftField The field in the left class/table you're joining on.
     * @param rightClazz The (right) class/table you're joining from.
     * @param rightField The field in the right class/table you're joining on.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> join(Class<?> leftClazz, String leftField, Class<?> rightClazz, String rightField) {
        if (firstJoin) {
            queryParts.put(StatementParts.JOIN, "");
            firstJoin = false;
        }
        this.joinExecutor.join(leftClazz, leftField, rightClazz, rightField);
        String join = joinExecutor.getQuery();
        queryParts.put(StatementParts.JOIN, queryParts.get(StatementParts.JOIN).concat(join));
        return this;
    }
    
    /**
     * Used to do a SQL Join with another POJO/Table, with specifying the {@linkplain JoinType}
     *  
     * <p>A simple example of using joining to get all of a user's things, given the user's name:
     * 
     * <p>
     * <pre>
     * new SqlStatement().select(Thing.class).join(User.class, "id", Thing.class, "userId")
     *                                       .where(User.class, "name").eq("Bob").getList()
     *</pre> 
     *
     * @param leftClazz The (left) class/table you're joining to.
     * @param leftField The field in the left class/table you're joining on.
     * @param rightClazz The (right) class/table you're joining from.
     * @param rightField The field in the right class/table you're joining on.
     * @param joinType The {@linkplain JoinType} that the join should use.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> join(Class<?> leftClazz, String leftField, Class<?> rightClazz, String rightField, JoinType joinType) {
        joinExecutor.setJoinType(joinType);
        return join(leftClazz, leftField, rightClazz, rightField);
    }
    
    
//    User {
//        @OneToMany(userId) // the parameter is the foreignKey in the AccessGroup table
//        private List<AccessGroup> accessGroup;
//        }
//
//        Or
//
//        User {
//        @ManyToMany(userId, groupId) // maybe the go in order of this object's foreign key, then the return objects foreign key... depends on how much "auto magical" stuff we want to do...
//        private List<AccessGroup> accessGroup;
//        // another option would be to spell it all out... @ManyToMany(id, userId, id, groupId) first two are the user's id and it's foreign key in the "junction table" then the accessGroups id, and it's foreign key in the junction table)
//        }
//    assertEquals("select thing.* from thing join user on user.id = thing.userId", sql);
    /**
     * Allows to select a {@linkplain List} of Objects of type T (as specified by class type passed into
     * {@linkplain SqlExecutor#select(Class)}) from the POJO passed into this method. The POJO that is passed in must have
     * a {@linkplain OneToMany} relationship setup in it to property have this method work.
     * 
     * <p>You can use the returned {@linkplain SqlExecutor} object to limit the resulting list by chaining a {@linkplain SqlExecutor#where(String) where} clause after this method.
     * 
     * <p> Example usage would be:
     * 
     * <pre>
     * User nick = new SqlStatement().select(User.class).where("name").eq("Nick");
     * List&lt;Thing&gt; nicksThings = new SqlStatement().select(Thing.class).from(nick).getList();
     * </pre>
     * 
     * <p>If you only wanted the things where it's value was 5 you could do:
     * 
     * <pre>
     * User nick = new SqlStatement().select(User.class).where("name").eq("Nick");
     * List&lt;Thing&gt; nicksThings = new SqlStatement().select(Thing.class).from(nick).where("value").eq(5).getList();
     * </pre>
     * 
     * <p>See {@link OneToMany} for more information on setting up this relationship.
     */
    public SqlExecutor<T> from(Object object) throws DataConnectionException {
        String objectPk = this.getPkField(object.getClass());
        String thisFk = this.getFkField(object.getClass());
        
        if (objectPk == null) {
            throw new DataConnectionException("pkField on the target object couldn't be found... it's probably not declared on the object. To use this method the target object must have a PrimaryKey defined.");
        }
        if (thisFk == null) {
            throw new DataConnectionException(String.format("A OneToMany was found, however it wasn't a list of type %s", this.clazz.getName()));
        }

        return join(object.getClass(), objectPk, this.clazz, thisFk);
    }

    /**
     * Used to start a "where clause" when querying/updating/deleting for an Object from the database. This will
     * return a {@linkplain WhereExecutor} that is used to in conjunction with the where to limit your database
     * call. Additional fields can be added to your "where clause" by using the
     * {@linkplain SqlExecutor#and(String)} method.
     * 
     * @param field The field of the object/database table you want to limit by.
     * @return a {@linkplain WhereExecutor} to be used in conjunction with the <b>where</b> method.
     * @throws DataConnectionException
     */
    public WhereExecutor<T> where(String field) throws DataConnectionException {
        return where(this.clazz, field);
    }
    
    /**
     * Used to start a "where clause" when querying/updating/deleting for an Object from the database. This will
     * return a {@linkplain WhereExecutor} that is used to in conjunction with the where to limit your database
     * call. Additional fields can be added to your "where clause" by using the
     * {@linkplain SqlExecutor#and(String)} method.
     * 
     * <P>This version is to explicitly state what Entity the field belongs to. This is typically needed when doing
     * a {@linkplain SqlExecutor#join(Class, String, Class, String) join}.
     * 
     * @param clazz The class who the field belongs to.
     * @param field The field of the object/database table you want to limit by.
     * @return a {@linkplain WhereExecutor} to be used in conjunction with the <b>where</b> method.
     * @throws DataConnectionException
     */
    public WhereExecutor<T> where(Class<?> clazz, String field) throws DataConnectionException {
        if (statementType == StatementType.UPDATE) {
            try {
                prepareUpdate(field);
            }
            catch (DataConnectionException e) {
                throw e;
            }
            catch (Exception e) {
                throw new DataConnectionException("Error running update", e);
            }
        }
        whereDefined = true;
        queryParts.put(StatementParts.WHERE, String.format(WHERE, clazz.getSimpleName().toLowerCase(), field));
        return whereExecutor;
    }

    /**
     * Used to continue a "where clause" when querying/updating/deleting for an Object from the database. This will
     * return a {@linkplain WhereExecutor} that is used to in conjunction with the where to limit your database
     * call. Additional fields can be added to your "where clause" by using the
     * {@linkplain SqlExecutor#and(String)} method.
     * 
     * @param field Additional fields of the object/database table you want to limit by.
     * @return A {@linkplain WhereExecutor} to be used in conjunction with the <b>and</b> method.
     */
    public WhereExecutor<T> and(String field) {
        return and(this.clazz, field);
    }
    
    /**
     * Used to continue a "where clause" when querying/updating/deleting for an Object from the database. This will
     * return a {@linkplain WhereExecutor} that is used to in conjunction with the where to limit your database
     * call. Additional fields can be added to your "where clause" by using the
     * {@linkplain SqlExecutor#and(String)} method.
     * 
     * <P>This version is to explicitly state what Entity the field belongs to. This is typically needed when doing
     * a {@linkplain SqlExecutor#join(Class, String, Class, String) join}.
     * 
     * @param clazz The class who the field belongs to.
     * @param field Additional fields of the object/database table you want to limit by.
     * @return A {@linkplain WhereExecutor} to be used in conjunction with the <b>and</b> method.
     */
    public WhereExecutor<T> and(Class<?> clazz, String field) {
        queryParts.put(StatementParts.WHERE, queryParts.get(StatementParts.WHERE).concat(String.format(AND, clazz.getSimpleName().toLowerCase(), field)));
        return whereExecutor;
    }
    
    /**
     * Used to continue a "where clause" when querying/updating/deleting for an Object from the database. This will
     * return a {@linkplain WhereExecutor} that is used to in conjunction with the where to limit your database
     * call. Additional fields can be added to your "where clause" by using the
     * {@linkplain SqlExecutor#and(String)} method.
     * 
     * @param field Additional fields of the object/database table you want to limit by.
     * @return A {@linkplain WhereExecutor} to be used in conjunction with the <b>and</b> method.
     */
    public WhereExecutor<T> or(String field) {
        return or(this.clazz, field);
    }
    
    /**
     * Used to continue a "where clause" when querying/updating/deleting for an Object from the database. This will
     * return a {@linkplain WhereExecutor} that is used to in conjunction with the where to limit your database
     * call. Additional fields can be added to your "where clause" by using the
     * {@linkplain SqlExecutor#and(String)} method.
     * 
     * @param field Additional fields of the object/database table you want to limit by.
     * @return A {@linkplain WhereExecutor} to be used in conjunction with the <b>and</b> method.
     */
    public WhereExecutor<T> or(Class<?> clazz, String field) {
        queryParts.put(StatementParts.WHERE, queryParts.get(StatementParts.WHERE).concat(String.format(OR, clazz.getSimpleName().toLowerCase(), field)));
        return whereExecutor;
    }

    /**
     * Used to sort the resulting list of Objects from the query. By default, the order will be ordered in
     * ascending order by the field passed in. If you want to sort in descending order, use
     * {@linkplain SqlExecutor#desc()} after calling this method.
     * 
     * @param field The field of the object/database table you want to sort the resulting list by.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> orderBy(String field) {
        queryParts.put(StatementParts.ORDER_BY, String.format(ORDER_BY, field));
        return this;
    }

    /**
     * Used to explicitly say you want to sort in ascending order. Works in conjunction with
     * {@linkplain SqlExecutor#orderBy(String)}.
     * 
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> asc() {
        queryParts.put(StatementParts.ORDER_BY, queryParts.get(StatementParts.ORDER_BY).concat(ASC));
        return this;
    }

    /**
     * Used to sort in descending order. Works in conjunction with {@linkplain SqlExecutor#orderBy(String)}.
     * 
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> desc() {
        queryParts.put(StatementParts.ORDER_BY, queryParts.get(StatementParts.ORDER_BY).concat(DESC));
        return this;
    }

    /**
     * Used to end a {@linkplain SqlStatement#update(Object) update}/{@linkplain SqlStatement#insert(Object)
     * insert}/{@linkplain SqlStatement#delete(Class) delete} {@linkplain SqlStatement}.
     * 
     * @throws DataConnectionException
     */
    public void execute() throws DataConnectionException {
        executeStatement();
    }

    /**
     * Used to end a {@linkplain SqlExecutor#select(Class)} where you just want the count of the resulting query.
     * 
     * @return The count of the number of objects the query would return.
     * @throws DataConnectionException
     */
    public int getCount() throws DataConnectionException {
        queryParts.put(StatementParts.SELECT, SELECT_COUNT);
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        executeStatement();
        try {
            return processCountResults();
        }
        catch (SQLException e) {
            throw new DataConnectionException("Could not process the count results of the query.", e);
        }
    }

    /**
     * Returns a {@linkplain List} of Objects of type T (as specified by class type passed into
     * {@linkplain SqlExecutor#select(Class)}) that result from the query built up with the
     * {@linkplain SqlStatement}/{@linkplain SqlExecutor}.
     * 
     * @return A {@linkplain List} of Objects of type T that is the result from querying the database.
     * @throws DataConnectionException
     */
    public List<T> getList() throws DataConnectionException {
        try {
            queryParts.put(StatementParts.SELECT, String.format(SELECT, clazz.getSimpleName().toLowerCase()));
            executeStatement();
            return processResults();
        }
        catch (Exception e) {
            throw new DataConnectionException("An error occured when trying to get the list of " + clazz.getSimpleName() + " objects", e);
        }
    }
    
    /**
     * A convenience method to return the first object of a given query which would normally be returned by {@link SqlExecutor#getList}.
     * @return An object of type T that is the result of querying the database.
     * @throws DataConnectionException
     */
    public T getFirst() throws DataConnectionException {
        List<T> items = getList();
        
        return items.size() > 0 ? items.get(0) : null;
    }

    /**
     * Returns a {@linkplain List} of {@linkplain Map Maps} which map from a field/database table column for the
     * resulting query along with the passed in {@linkplain ColumnExpression}.
     * 
     * <p> The value of the map is of type {@linkplain Object} and will need to be cast to the type of value you're
     * expecting from the fields/columns you specified in your {@linkplain ColumnExpression}.
     * 
     * @param columnExpression A map expression to specify what columns you want back from the database.
     * @return A {@linkplain List} of {@linkplain Map Maps} which are the result of the query with the passed in
     * {@linkplain ColumnExpression}.
     * @throws DataConnectionException
     */
    public List<Map<String, Object>> getColumns(ColumnExpression columnExpression) throws DataConnectionException {
        this.queryParts.put(StatementParts.SELECT, columnExpression.getQuery());
        executeStatement();
        try {
            return processMapResults();
        }
        catch (SQLException e) {
            throw new DataConnectionException("Could not process the map results of the query.", e);
        }
    }
    
    
    String getQuery() throws DataConnectionException {
        StringBuilder builder = new StringBuilder();
        for (StatementParts key : queryParts.keySet()) {
            builder.append(queryParts.get(key));
        }
        return builder.toString().trim().concat(";");
    }
    
    List<Object> getValues() {
        return this.values;
    }
    
    LinkedHashMap<StatementParts, String> getQueryParts() {
        return this.queryParts;
    }

    private List<T> processResults() throws SQLException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException, DataConnectionException {
        List<T> objects = new ArrayList<T>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<String> types = new ArrayList<String>();

        // Get column data
        for (int i = 1; i <= columnCount; i++) {
            columns.add(resultSet.getMetaData().getColumnName(i));
            types.add(resultSet.getMetaData().getColumnTypeName(i));
        }

        while (resultSet.next()) {
            T object = clazz.newInstance();
            for (int i = 0; i < columnCount; i++) {
                processColumn(object, columns.get(i), resultSet);
            }
            objects.add(createProxyObject(object));
        }
        resultSet.close();
        return objects;
    }

    private int processCountResults() throws SQLException {
        int count = 0;
        while (resultSet.next()) {
            count = resultSet.getInt(1);
        }
        resultSet.close();
        return count;
    }

    private List<Map<String, Object>> processMapResults() throws SQLException {
        List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<String> types = new ArrayList<String>();

        // Get column data
        for (int i = 1; i <= columnCount; i++) {
            columns.add(resultSet.getMetaData().getColumnName(i));
            types.add(resultSet.getMetaData().getColumnTypeName(i));
        }

        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < columnCount; i++) {
                if (types.get(i).equals("text")) {
                    map.put(columns.get(i), resultSet.getString(columns.get(i)));
                }
                else if (types.get(i).equals("float")) {
                    map.put(columns.get(i), resultSet.getDouble(columns.get(i)));
                }
                else if (types.get(i).equals("integer")) {
                    map.put(columns.get(i), resultSet.getInt(columns.get(i)));
                }
                else if (types.get(i).equals("blob")) {
                    map.put(columns.get(i), resultSet.getBlob(columns.get(i)));
                }
                else if (types.get(i).equals("null")) {
                    map.put(columns.get(i), null);
                }
            }
            objects.add(map);
        }
        resultSet.close();

        return objects;
    }

    private void prepareUpdate(String field) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, DataConnectionException, SecurityException, NoSuchMethodException {
        boolean first = true;
        queryParts.put(StatementParts.SET, "");
        for (Field classField : clazz.getDeclaredFields()) {
            String methodName = (classField.getType() == Boolean.class ? "is" : "get") + SqliteUtils.capitalize(classField.getName());
            String fieldName = classField.getName();

            boolean fieldShouldBeInUpdateStatement = !classField.isAnnotationPresent(AutoIncrement.class) && !classField.isAnnotationPresent(OneToMany.class);
            if (fieldShouldBeInUpdateStatement) {
                try {
                    Object value = clazz.getDeclaredMethod(methodName, (Class<?>[]) null).invoke(sqlObject, (Object[]) null);
                    if (first) {
                        queryParts.put(StatementParts.SET, queryParts.get(StatementParts.SET).concat(String.format(SET, fieldName)));
                        values.add(value);
                        first = false;
                    }
                    else {
                        queryParts.put(StatementParts.SET, queryParts.get(StatementParts.SET).concat(String.format(SET_AND, fieldName)));
                        values.add(value);
                    }
                }
                catch (NoSuchMethodException e) {
                    if (classField.getType() == Boolean.class && clazz.getDeclaredMethod(methodName.replaceFirst("is", "get"), (Class<?>[]) null) != null) {
                        throw new DataConnectionException("boolean fields must name their fields isValue, not getValue");
                    }
                    // this means the field doesn't have a getter, so we're
                    // moving on.
                }
            }
        }
    }

    private void prepareInsert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, DataConnectionException {
        StringBuilder fieldsString = new StringBuilder("(");
        StringBuilder valuesString = new StringBuilder("values(");

        boolean first = true;
        for (Field field : clazz.getDeclaredFields()) {
            String methodName = field.getType() == Boolean.class ? "is" + SqliteUtils.capitalize(field.getName()) : "get" + SqliteUtils.capitalize(field.getName());
            String fieldName = field.getName();
            // We don't want to include the auto increment in the create statement.
            boolean fieldShouldBeInCreateStatement = !field.isAnnotationPresent(AutoIncrement.class) && !field.isAnnotationPresent(OneToMany.class);
            if (fieldShouldBeInCreateStatement) {
                try {
                    Object value = clazz.getDeclaredMethod(methodName, (Class<?>[]) null).invoke(sqlObject, (Object[]) null);
                    if (!first) {
                        fieldsString.append(", ");
                        valuesString.append(", ");
                    }
                    else {
                        first = false;
                    }
                    fieldsString.append(fieldName);
                    valuesString.append("?");
                    values.add(value);
                }
                catch (NoSuchMethodException nsme) {
                    if (field.getType() == Boolean.class && clazz.getDeclaredMethod(methodName.replaceFirst("is", "get"), (Class<?>[]) null) != null) {
                        throw new DataConnectionException("boolean fields must name their fields isValue, not getValue");
                    }
                    // this means the field doesn't have a getter, so we're
                    // moving on.
                }
            }
        }
        fieldsString.append(") ");
        valuesString.append(") ");
        queryParts.put(StatementParts.INSERT, queryParts.get(StatementParts.INSERT).concat(fieldsString.toString()).concat(valuesString.toString()));
    }

    private void replaceValues() throws SQLException, DataConnectionException {
        logger.trace(String.format(getQuery().replaceAll("%", "%%").replaceAll("\\?", "%s"), values.toArray()));

        for (int i = 0; i < values.size(); i++) {
            Object object = values.get(i);
            if (object instanceof String) {
                statement.setString(i + 1, (String) object);
            }
            else if (object instanceof Float) {
                statement.setFloat(i + 1, (Float) object);
            }
            else if (object instanceof Integer) {
                statement.setInt(i + 1, (Integer) object);
            }
            else if (object instanceof Long) {
                statement.setLong(i + 1, (Long) object);
            }
            else if (object instanceof Double) {
                statement.setDouble(i + 1, (Double) object);
            }
            else if (object instanceof Date) {
                String date = DateUtils.getDatabaseFormattedStringFromDate((Date) object);
                statement.setObject(i + 1, date);
            }
            else if (object instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) object);
            }
            else if (object == null) {
                statement.setNull(i + 1, Types.NULL);
            }
            else {
                throw new RuntimeException(object.getClass() + " " + object.toString() + " is not a supported object");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private T createProxyObject(T object) throws DataConnectionException {
        relationships = findRelationships();

        if (relationships.size() > 0) {
            String objectPk = this.getPkField(this.clazz);
            if (objectPk == null) {
                throw new DataConnectionException("Error when mapping relationships. PkField on the target object couldn't be found... it's probably not declared on the object. To use this method the target object must have a PrimaryKey defined.");
            }
            final Object pkValue = getPkValue(objectPk, object);

            ProxyFactory proxyFactory = new ProxyFactory(object);
            proxyFactory.addAdvice(new MethodInterceptor() {
                public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                    String fieldName = SqliteUtils.lowercase(methodInvocation.getMethod().getName().replaceFirst("get", ""));
                    if (relationships.keySet().contains(fieldName)) {
                        // TODO Check to see if the internal variable for the collection is null, load if not. If it is
                        // just return that already loaded instance, don't do more DB calls than needed.
                        Relationship relationship = relationships.get(fieldName);
                        return SqlStatement.select(relationship.getRelatedClassType()).where(relationship.getFk()).eq(pkValue).getList();
                    }
                    else if (methodInvocation.getMethod().getReturnType() == List.class) {
                        throw new DataConnectionException(String.format("No OneToMany relationship could be found on a member variable that corresponds to the method %s", methodInvocation.getMethod().getName()));
                    }
                    return methodInvocation.getMethod().invoke(methodInvocation.getThis(), methodInvocation.getArguments());
                }
            });
            return (T) proxyFactory.getProxy();
        }
        return object;
    }

    private Map<String, Relationship> findRelationships() throws DataConnectionException {
        Map<String, Relationship> relationshipMap = new HashMap<String, Relationship>();

        Field[] fields = this.clazz.getDeclaredFields();
        for (Field field : fields) {
            boolean hasAnnotation = field.isAnnotationPresent(OneToMany.class);
            if (hasAnnotation) {
                Class<?> targetClazz = field.getType();
                if (targetClazz != List.class) {
                    throw new DataConnectionException(String.format("The return type of a OneToMany relationship must be a List Type for field %s", field.getName()));
                }
                relationshipMap.put(field.getName(), new Relationship(field));
            }
        }

        return relationshipMap;
    }

    private String getPkField(Class<?> clazz) {
        String pkField = null;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                pkField = field.getName();
            }
        }
        return pkField;
    }

    private Object getPkValue(String pkField, Object object) throws DataConnectionException {
        try {
            return object.getClass().getMethod("get" + SqliteUtils.capitalize(pkField), (Class<?>[]) null).invoke(object, (Object[]) null);
        }
        catch (Exception e) {
            throw new DataConnectionException("Could not get pkValue", e);
        }
    }

    private String getFkField(Class<?> clazz) throws DataConnectionException {
        String fkField = null;
        Field[] fields = clazz.getDeclaredFields();
        boolean annotationFound = false;
        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(OneToMany.class);
            if (annotation != null) {
                annotationFound = true;
                Class<?> targetClazz = field.getType();
                if (targetClazz != List.class) {
                    throw new DataConnectionException(String.format("The return type of a OneToMany relationship must be a List Type for field %s", field.getName()));
                }
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Type[] types = genericType.getActualTypeArguments();
                if ((Class<?>) types[0] == this.clazz) {
                    fkField = ((OneToMany) annotation).value();
                }
            }
        }
        if (!annotationFound) {
            throw new DataConnectionException(String.format("No OneToMany reationship could be found on the %s", clazz));
        }
        return fkField;
    }

    private void executeStatement() throws DataConnectionException {
        try {
            Connection connection = DataConnectionManager.getConnection();
            if (connection == null) {
                throw new DataConnectionException("Connection is not initialized");
            }
            // Try to define the where based on if there is a pk field defined.
            boolean needsAutoDefinedWhereStatement = !whereDefined && (statementType == StatementType.UPDATE || statementType == StatementType.DELETE);
            if (needsAutoDefinedWhereStatement) {
                String pkField = getPkField(this.clazz);
                Object pkValue = getPkValue(pkField, this.sqlObject);
                if (pkField == null) {
                    throw new DataConnectionException("pkField couldn't be found... it's probably not declared on the object.");
                }
                if (sqlObject == null) {
                    throw new DataConnectionException("An instance of the object " + clazz.getSimpleName() + " must be supplied to auto infer the upate/delete");
                }
                where(pkField);
                whereExecutor.eq(pkValue);
            }
            statement = connection.prepareStatement(getQuery(), Statement.RETURN_GENERATED_KEYS);
            replaceValues();
            if (statementType == StatementType.SELECT) {
                resultSet = statement.executeQuery();
            }
            else {
                statement.execute();

                /*
                 * TODO This needs to be looked at... before going down the road of inserting the related objects,
                 *      we might want to check if the list actually has any objects. The other problem is right at
                 *      creation time the pk of the object might not be there... so we may want to write a join to
                 *      do this update of the related objects...
                 */
                if (relationships == null) {
                    relationships = findRelationships();
                }
                boolean relationshipsNeedToBeAdded = relationships.size() > 0
                        && (statementType == StatementType.INSERT || statementType == StatementType.UPDATE);
                if (relationshipsNeedToBeAdded) {
                    if (statementType == StatementType.INSERT || statementType == StatementType.UPDATE) {
                        for (Relationship relationship : relationships.values()) {
                            addRelationshipForInsert(relationship);
                        }
                    }
                }
//                    Object thisObjectsPrimaryKey = this.getPkValue(this.getPkField(this.clazz), this.sqlObject);
//                    for(Relationship relationship : relationships.values()) {
//                        
//                        // Get the list of related objects.
//                        Method method = clazz.getDeclaredMethod(relationship.getterName(), (Class<?>[]) null);
//                        List<?> relatedObjects = (List<?>) method.invoke(this, (Object []) null);
//                        
//                        // Call the foreign key setter on each of the objects, with the current object's primary key.
//                        for(Object object : relatedObjects) {
//                            Method objectsSetterMethod = object.getClass().getDeclaredMethod(relationship.foreignSetterName(), relationship.getFkClassType());
//                            objectsSetterMethod.invoke(object, thisObjectsPrimaryKey);
//                            SqlStatement.update(object);
//                        }
//                    }
//                }
            }
        }
        catch (SQLException e) {
            logger.error(e.getErrorCode());
            if (e.getMessage().contains("PRIMARY KEY must be unique")) {
                throw new DataConnectionException("A @PrimaryKey needs to be defined on the class '" + this.clazz.getSimpleName() + "' , since there is a primary key in the database.", e);
            }
            throw new DataConnectionException("Error executing sql statement", e);
        }
        catch (Exception e) {
            throw new DataConnectionException("Error executing sql statement", e);
        }
    }

    private void processColumn(Object object, String columnName, ResultSet resultSet) throws SQLException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, DataConnectionException {
        Object value = null;
        Class<?> type = clazz.getDeclaredField(columnName).getType();
        if (type == String.class) {
            value = resultSet.getString(columnName);
        }
        else if (type == Float.class || type == Float.TYPE) {
            value = resultSet.getFloat(columnName);
        }
        else if (type == Integer.class || type == Integer.TYPE) {
            value = resultSet.getInt(columnName);
        }
        else if (type == Double.class || type == Double.TYPE) {
            value = resultSet.getDouble(columnName);
        }
        else if (type == Boolean.class || type == Boolean.TYPE) {
            value = resultSet.getBoolean(columnName);
        }
        else if (type == Date.class) {
            value = DateUtils.getDateFromDatabaseFormattedString(resultSet.getString(columnName));
        }
        else if (type == Long.class || type == Long.TYPE) {
            value = resultSet.getLong(columnName);
        }
        String methodName = "set" + SqliteUtils.capitalize(columnName);
        Method method = clazz.getDeclaredMethod(methodName, type);
        method.invoke(object, value);
    }

    private void addRelationshipForInsert(Relationship relationship) throws DataConnectionException {
        try {
            Method method = clazz.getDeclaredMethod(relationship.getterName(), (Class<?>[]) null);
            List<?> relatedObjects = (List<?>) method.invoke(this.sqlObject, (Object[]) null);
            if (relatedObjects != null) {
                for (Object object : relatedObjects) {
                    setupRelationshipForRelatedObject(relationship, object, relationship.getFk());
                }
            }
        }
        catch (Exception e) {
            throw new DataConnectionException("Error adding relationship on object insertion", e);
        }
    }

    public void setupRelationshipForRelatedObject(Relationship relationship, Object object, String foreignKey) throws DataConnectionException {
        String primaryKey = getPkField(object.getClass());
        Object objectsPrimaryKey = getPkValue(primaryKey, object);
        Object thisObjectsPrimaryKey = this.getPkValue(this.getPkField(this.clazz), this.sqlObject);
        boolean thisSqlObjectIsNotUpToDate = SqliteUtils.isEmpty(thisObjectsPrimaryKey);

        try {
            if (thisSqlObjectIsNotUpToDate) {
                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                thisObjectsPrimaryKey = rs.getLong(1);
            }
            Method objectsSetterMethod = object.getClass().getDeclaredMethod(relationship.foreignSetterName(), relationship.getFkClassType());
            objectsSetterMethod.invoke(object, thisObjectsPrimaryKey);
        }
        catch (Exception e) {
            throw new DataConnectionException(String.format("Could not set the foreign key %s on the %s object", foreignKey, object.getClass()), e);
        }

        boolean objectIsInDbAlready = SqlStatement.select(object.getClass()).where(primaryKey).eq(objectsPrimaryKey).getCount() > 0;
        if (objectIsInDbAlready) {
            SqlStatement.update(object).execute();
        }
        else {
            SqlStatement.insert(object).execute();
        }
    }

    private void reset() throws DataConnectionException {
        try {
            if (statement != null) {
                statement.close();
            }
        }
        catch (Exception e) {
            throw new DataConnectionException("Failed to close sql statement", e);
        }
        queryParts = new LinkedHashMap<StatementParts, String>();
        statement = null;
        resultSet = null;
        values.clear();
        clazz = null;
        statementType = null;
        sqlObject = null;
        whereDefined = false;
    }

    private LinkedHashMap<StatementParts, String> queryParts;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private List<Object> values = new ArrayList<Object>();;
    private Class<T> clazz;
    private StatementType statementType;
    private Object sqlObject;
    private boolean whereDefined = false;
    private boolean firstJoin = true;
    private WhereExecutor<T> whereExecutor = new WhereExecutor<T>(this);
    private JoinExecutor joinExecutor = new JoinExecutor();
    private Map<String, Relationship> relationships;

    private static final String SELECT = "select %s.* ";
    private static final String FROM = "from %s ";
    private static final String SELECT_COUNT = "select count(*) as count ";
    private static final String UPDATE = "update %s ";
    private static final String INSERT = "insert into %s";
    private static final String DELETE = "delete ";
    private static final String WHERE = "where %s.%s ";
    private static final String AND = "and %s.%s ";
    private static final String OR = "or %s.%s ";
    private static final String ORDER_BY = "order by %s ";
    private static final String ASC = "asc ";
    private static final String DESC = "desc ";
    private static final String SET = "set %s = ?";
    private static final String SET_AND = ", %s = ? ";

    private static final Logger logger = Logger.getLogger(SqlExecutor.class);
}
