package com.kremerk.Sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kremerk.Sqlite.Annotations.AutoIncrement;
import com.kremerk.Sqlite.Annotations.PrimaryKey;

public class SqlExecutor<T> {
    public SqlExecutor<T> select(Class<T> clazz) {
        reset();
        this.clazz = clazz;
        queryParts.put(StatementParts.SELECT, SELECT);
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.SELECT;
        return this;
    }

    public SqlExecutor<T> update(Object object) {
        reset();
        clazz = object.getClass();
        sqlObject = object;
        queryParts.put(StatementParts.UPDATE, String.format(UPDATE, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.UPDATE;
        return this;
    }

    public SqlExecutor<T> insert(Object object) throws DataConnectionException {
        reset();
        clazz = object.getClass();
        queryParts.put(StatementParts.INSERT, String.format(INSERT, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.INSERT;
        sqlObject = object;
        try {
            prepareInsert();
        }
        catch (Exception e) {
            throw new DataConnectionException("Error running insert");
        }
        return this;
    }

    public SqlExecutor<T> delete(Class<?> clazz) throws DataConnectionException {
        reset();
        this.clazz = clazz;
        queryParts.put(StatementParts.DELETE, DELETE);
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.DELETE;
        return this;
    }

    public SqlExecutor<T> delete(Object object) {
        reset();
        clazz = object.getClass();
        sqlObject = object;
        queryParts.put(StatementParts.DELETE, DELETE);
        queryParts.put(StatementParts.FROM, String.format(FROM, clazz.getSimpleName().toLowerCase()));
        statementType = StatementType.DELETE;
        return this;
    }

    public WhereExecutor<T> where(String field) throws DataConnectionException {
        if (statementType == StatementType.UPDATE) {
            try {
                prepareUpdate(field);
            }
            catch (DataConnectionException e) {
                throw e;
            }
            catch (Exception e) {
                throw new DataConnectionException("Error running update");
            }
        }
        whereDefined = true;
        queryParts.put(StatementParts.WHERE, String.format(WHERE, field));
        return whereExecutor;
    }

    public WhereExecutor<T> and(String field) {
        queryParts.put(StatementParts.WHERE, queryParts.get(StatementParts.WHERE).concat(String.format(AND, field)));
        return whereExecutor;
    }

    public SqlExecutor<T> orderBy(String field) {
        queryParts.put(StatementParts.ORDER_BY, String.format(ORDER_BY, field));
        return this;
    }

    public SqlExecutor<T> asc() {
        queryParts.put(StatementParts.ORDER_BY, queryParts.get(StatementParts.ORDER_BY).concat(ASC));
        return this;
    }

    public SqlExecutor<T> desc() {
        queryParts.put(StatementParts.ORDER_BY, queryParts.get(StatementParts.ORDER_BY).concat(DESC));
        return this;
    }

    public void execute() throws DataConnectionException {
        executeStatement();
    }

    public String getQuery() throws DataConnectionException {
        StringBuilder builder = new StringBuilder();
        for (StatementParts key : queryParts.keySet()) {
            builder.append(queryParts.get(key));
        }
        return builder.toString().trim().concat(";");
    }

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
    
    public List<Map<String, Object>> getMap(MapExpression mapExpression) throws DataConnectionException {
        /*
         * 1.) Take the map expression and replace the QUERY part of the sql statement with the mapExpressions's .getQuery()
         * 2.) Execute the query
         * 3.) Process the results, building an array of  maps of String to Object.
         *      * Each array element is a row returned
         *      * Each map entry is a map of columnName (alias in this case) to Value.
         */
    	this.queryParts.put(StatementParts.SELECT, mapExpression.getQuery());
    	executeStatement();
    	try {
    		return processMapResults();
        }
        catch (SQLException e) {
            throw new DataConnectionException("Could not process the map results of the query.", e);
        }
    }

    private List<T> processResults() throws SQLException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
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
            @SuppressWarnings("unchecked")
            T object = (T) clazz.newInstance();
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
            	System.out.println(types.get(i));
            	if(types.get(i).equals("text")) {
            		map.put(columns.get(i), resultSet.getString(columns.get(i)));
            	}
            	else if(types.get(i).equals("float")) {
            		map.put(columns.get(i), resultSet.getDouble(columns.get(i)));
            	}
            	else if(types.get(i).equals("integer")) {
            		map.put(columns.get(i), resultSet.getInt(columns.get(i)));
            	}
            	else if(types.get(i).equals("blob")) {
            		map.put(columns.get(i), resultSet.getBlob(columns.get(i)));
            	}
            	else if(types.get(i).equals("null")) {
            		map.put(columns.get(i), null);
            	}
            }
            objects.add(map);
        }
        resultSet.close();
        
        return objects;
    }

    private void prepareUpdate(String field) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, DataConnectionException {
        boolean first = true;
        queryParts.put(StatementParts.SET, "");
        for (Field classField : clazz.getDeclaredFields()) {
            String methodName = classField.getType() == Boolean.class ? "is" : "get" + capitalize(classField.getName().toLowerCase());
            String fieldName = classField.getName().toLowerCase();

            if (!fieldName.equals(field)) {
                if (classField.getAnnotation(AutoIncrement.class) != null) {
                    throw new DataConnectionException("The field " + classField.getName() + " is an auto incremented field and should not be updated.");
                }
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
                    // this means the field doesn't have a getter, so we're
                    // moving on.
                }
            }
        }
    }

    private void prepareInsert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        StringBuilder fieldsString = new StringBuilder("(");
        StringBuilder valuesString = new StringBuilder("values(");

        boolean first = true;
        for (Field field : clazz.getDeclaredFields()) {
            String methodName = field.getType() == Boolean.class ? "is" : "get" + capitalize(field.getName().toLowerCase());
            String fieldName = field.getName().toLowerCase();
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
        System.out.println(String.format(getQuery().replaceAll("\\?", "%s"), values.toArray()));
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
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = df.format((Date) object);
                statement.setObject(i + 1, date);
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
        catch (Exception e) {
            throw new DataConnectionException("Error executing sql statement", e);
        }
    }

    private void processColumn(Object object, String columnName, ResultSet resultSet) throws SQLException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
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
        else if (type == Double.class  || type == Double.TYPE) {
            value = resultSet.getDouble(columnName);
        }
        else if (type == Date.class) {
            value = resultSet.getDate(columnName);
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
    private Class<?> clazz;
    private StatementType statementType;
    private Object sqlObject;
    private boolean whereDefined = false;
    private WhereExecutor<T> whereExecutor = new WhereExecutor<T>(this);

    private static final String SELECT = "select * ";
    private static final String FROM = "from %s ";
    private static final String SELECT_COUNT = "select count(*) as count ";
    private static final String UPDATE = "update %s ";
    private static final String INSERT = "insert into %s";
    private static final String DELETE = "delete ";
    private static final String WHERE = "where %s ";
    private static final String AND = "and %s ";
    private static final String ORDER_BY = "order by %s ";
    private static final String ASC = "asc ";
    private static final String DESC = "desc ";
    private static final String SET = "set %s = ?";
    private static final String SET_AND = ", %s = ? ";
    
    class WhereExecutor<U> {
        
        public WhereExecutor(SqlExecutor<U> sqlE) {
            sqlExecutor = sqlE;
        }
        
        public SqlExecutor<U> eq(Object value) {
            return _appendToWhere(EQUALS, value);
        }
        
        public SqlExecutor<U> greaterThan(Object value) {
            return _appendToWhere(GREATER_THAN, value);
        }
        
        public SqlExecutor<U> greaterThanOrEq(Object value) {
            return _appendToWhere(GREATER_THAN_EQUAL, value);
        }
        
        public SqlExecutor<U> lessThan(Object value) {
            return _appendToWhere(LESS_THAN, value);
        }
        
        public SqlExecutor<U> lessThanOrEq(Object value) {
            return _appendToWhere(LESS_THAN_EQUAL, value);
        }

        public SqlExecutor<U> like(String value) {
            return _appendToWhere(LIKE, value);
        }
        
        private SqlExecutor<U> _appendToWhere(String appendString, Object value) {
            queryParts.put(StatementParts.WHERE, queryParts.get(StatementParts.WHERE).concat(appendString));
            values.add(value);
            return sqlExecutor;
        }
        
        private SqlExecutor<U> sqlExecutor;
        private static final String EQUALS = "= ? ";
        private static final String GREATER_THAN = "> ? ";
        private static final String LESS_THAN = "< ? ";
        private static final String GREATER_THAN_EQUAL = ">= ? ";
        private static final String LESS_THAN_EQUAL = "<= ? ";
        private static final String LIKE = "like ? ";
        
    }
}
