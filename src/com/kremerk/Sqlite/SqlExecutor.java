package com.kremerk.Sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kremerk.Sqlite.JoinExecutor.JoinType;
import com.kremerk.Sqlite.Annotations.AutoIncrement;
import com.kremerk.Sqlite.Annotations.PrimaryKey;

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
     */
    public SqlExecutor<T> select(Class<T> clazz) {
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
     */
    @SuppressWarnings("unchecked")
    public SqlExecutor<T> update(Object databaseObject) {
        reset();
        clazz = (Class<T>) databaseObject.getClass();
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
        clazz = (Class<T>) databaseObject.getClass();
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
    public SqlExecutor<T> delete(T databaseObject) {
        reset();
        clazz = (Class<T>) databaseObject.getClass();
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
        if(firstJoin) {
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
        queryParts.put(StatementParts.WHERE, queryParts.get(StatementParts.WHERE).concat(String.format(OR, field)));
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

    private List<T> processResults() throws SQLException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException, ParseException {
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
            objects.add(object);
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
            String methodName = (classField.getType() == Boolean.class ? "is" : "get") + capitalize(classField.getName());
            String fieldName = classField.getName();

            if (classField.getAnnotation(AutoIncrement.class) == null) {
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
            String methodName = field.getType() == Boolean.class ? "is" + capitalize(field.getName()) : "get" + capitalize(field.getName());
            String fieldName = field.getName();
            // We don't want to include the auto increment in the create
            // statement.
            if (field.getAnnotation(AutoIncrement.class) == null) {
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
        System.out.println(String.format(getQuery().replaceAll("%", "%%").replaceAll("\\?", "%s"), values.toArray()));
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
                String date = DATE_FORMAT.format((Date) object);
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

    private String getPkField() {
        String pkField = null;
        for (Field field : this.clazz.getDeclaredFields()) {
            if (field.getAnnotation(PrimaryKey.class) != null) {
                pkField = field.getName();
            }
        }
        return pkField;
    }

    private Object getPkValue(String pkField) throws DataConnectionException {
        try {
            return this.clazz.getMethod("get" + capitalize(pkField), (Class<?>[]) null).invoke(this.sqlObject, (Object[]) null);
        }
        catch (Exception e) {
            throw new DataConnectionException("Could not get pkValue");
        }
    }

    private void executeStatement() throws DataConnectionException {
        try {
            Connection connection = DataConnectionManager.getConnection();
            if (connection == null) {
                throw new DataConnectionException("Connection is not initialized");
            }
            // Try to defined the where based on if there is an auto incremented
            // id on the table (acting as pk).
            if (!whereDefined && (statementType == StatementType.UPDATE || statementType == StatementType.DELETE)) {
                String pkField = getPkField();
                Object pkValue = getPkValue(pkField);
                if (pkField == null) {
                    throw new DataConnectionException("pkField couldn't be found... it's probably not declared on the object.");
                }
                if (sqlObject == null) {
                    throw new DataConnectionException("An instance of the object " + clazz.getSimpleName() + " must be supplied to auto infer the upate/delete");
                }
                where(pkField);
                whereExecutor.eq(pkValue);
            }
            statement = connection.prepareStatement(getQuery());
            replaceValues();
            if (statementType == StatementType.SELECT) {
                resultSet = statement.executeQuery();
            }
            else {
                statement.execute();
            }
        }
        catch(SQLException e) {
            System.out.println(e.getErrorCode());
            if(e.getMessage().contains("PRIMARY KEY must be unique")) {
                throw new DataConnectionException("A @PrimaryKey needs to be defined on the class '"  + this.clazz.getSimpleName() + "' , since there is a primary key in the database.", e);
            }
            throw new DataConnectionException("Error executoing sql statement", e);
        }
        catch (Exception e) {
            throw new DataConnectionException("Error executing sql statement", e);
        }
    }

    private void processColumn(Object object, String columnName, ResultSet resultSet) throws SQLException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, ParseException {
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
            value = DATE_FORMAT.parse(resultSet.getString(columnName));
        }
        else if (type == Long.class || type == Long.TYPE) {
            value = resultSet.getLong(columnName);
        }
        String methodName = "set" + capitalize(columnName);
        Method method = clazz.getDeclaredMethod(methodName, type);
        method.invoke(object, value);
    }

    private String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private void reset() {
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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String SELECT = "select %s.* ";
    private static final String FROM = "from %s ";
    private static final String SELECT_COUNT = "select count(*) as count ";
    private static final String UPDATE = "update %s ";
    private static final String INSERT = "insert into %s";
    private static final String DELETE = "delete ";
    private static final String WHERE = "where %s.%s ";
    private static final String AND = "and %s.%s ";
    private static final String OR = "or %s ";
    private static final String ORDER_BY = "order by %s ";
    private static final String ASC = "asc ";
    private static final String DESC = "desc ";
    private static final String SET = "set %s = ?";
    private static final String SET_AND = ", %s = ? ";

    
}
