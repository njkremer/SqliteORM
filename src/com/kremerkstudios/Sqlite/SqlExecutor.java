package com.kremerkstudios.Sqlite;

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
import java.util.List;

import com.kremerkstudios.Sqlite.Annotations.AutoIncrement;
import com.kremerkstudios.Sqlite.Annotations.PrimaryKey;

public class SqlExecutor<T> {
	public SqlExecutor<T> select(Class<T> clazz) {
		reset();
		this.clazz = clazz;
		query.append(String.format(SELECT, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.SELECT;
		return this;
	}
	
	public SqlExecutor<T> update(Object object) {
		reset();
		clazz = object.getClass();
		sqlObject = object;
		query.append(String.format(UPDATE, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.UPDATE;
		return this;
	}
	
	public SqlExecutor<T> insert(Object object) throws DataConnectionException {
		reset();
		clazz = object.getClass();
		query.append(String.format(INSERT, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.INSERT;
		sqlObject = object;
		try {
			prepareInsert();			
		}
		catch(Exception e) {
			throw new DataConnectionException("Error running insert");
		}
		return this;
	}
	
	public SqlExecutor<T> delete(Class<?> clazz) throws DataConnectionException {
		reset();
		this.clazz = clazz;
		query.append(String.format(DELETE, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.DELETE;
		return this;
	}
	
	public SqlExecutor<T> delete(Object object) {
		reset();
		clazz = object.getClass();
		sqlObject = object;
		query.append(String.format(DELETE, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.DELETE;
		return this;
	}
	
	public SqlExecutor<T> where(String field) throws DataConnectionException {
		if(statementType == StatementType.UPDATE) {
			try {
				prepareUpdate(field);
			}
			catch(DataConnectionException e) {
				throw e;
			}
			catch(Exception e) {
				throw new DataConnectionException("Error running update");
			}
		}
		whereDefined = true;
		query.append(String.format(WHERE, field));
		return this;
	}
	


	public SqlExecutor<T> eq(Object value) {
		query.append(EQUALS);
		values.add(value);
		return this;
	}
	
	public SqlExecutor<T> like(String value) {
		query.append(LIKE);
		values.add(value);
		return this;
	}
	
	public SqlExecutor<T> and(String field) {
		query.append(String.format(AND, field));
		return this;
	}
	
	public SqlExecutor<T> orderBy(String field) {
		query.append(String.format(ORDER_BY, field));
		return this;
	}
	
	public SqlExecutor<T> asc() {
		query.append(ASC);
		return this;
	}
	public SqlExecutor<T> desc() {
		query.append(DESC);
		return this;
	}
	
	public void execute() throws DataConnectionException {
		executeStatement();
	}

	public String getQuery() throws DataConnectionException {
		return query.toString().trim().concat(";");
	}
	
	public int getCount() {
		//TODO Implement getCount, change the select portion to select count(*) instead of select *
		// In addition replace the string builer with a linkedHashMap to easily replace parts of the query.
		return 0;
	}
	
	public List<T> getList() throws DataConnectionException {
		try {
			executeStatement();
			return processResults();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<T>();
	}
	
	private List<T> processResults() throws SQLException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		List<T> objects = new ArrayList<T>();
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		
		// Get column data
		for(int i = 1; i <= columnCount; i++) {
			columns.add(resultSet.getMetaData().getColumnName(i));
			types.add(resultSet.getMetaData().getColumnTypeName(i));
		}
		
		while(resultSet.next()) {
			@SuppressWarnings("unchecked")
			T object = (T) clazz.newInstance();
			for(int i = 0; i < columnCount; i++) {
				processColumn(object, columns.get(i), types.get(i), resultSet);
			}
			objects.add(object);
		}
		return objects;
	}
	
	
	private void prepareUpdate(String field) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, DataConnectionException {
		boolean first = true;
		for(Field classField : clazz.getDeclaredFields()) {
			String methodName = classField.getType() == Boolean.class ? "is" : "get" + capitalize(classField.getName().toLowerCase());
			String fieldName = classField.getName().toLowerCase();
			
			if(!fieldName.equals(field)) {
				if(classField.getAnnotation(AutoIncrement.class) != null) {
					throw new DataConnectionException("The field " + classField.getName() + " is an auto incremented field and should not be updated.");
				}
				try {
					Object value = clazz.getDeclaredMethod(methodName, (Class<?>[]) null).invoke(sqlObject, (Object[]) null);					
					if(first) {
						query.append(String.format(SET, fieldName));
						values.add(value);
						first = false;
					}
					else {
						query.append(String.format(SET_AND, fieldName));
						values.add(value);
					}
				}
				catch(NoSuchMethodException e) {
					// this means the field doesn't have a getter, so we're moving on.
				}
			}
		}
	}
	
	private void prepareInsert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		StringBuilder fieldsString = new StringBuilder("(");
		StringBuilder valuesString = new StringBuilder("values(");
		
		boolean first = true;
		for(Field field : clazz.getDeclaredFields()) {
			String methodName = field.getType() == Boolean.class ? "is" : "get" + capitalize(field.getName().toLowerCase());
			String fieldName = field.getName().toLowerCase();
			// We don't want to include the auto increment in the create statement.
			if(field.getAnnotation(AutoIncrement.class) == null) {
				try {
					Object value = clazz.getDeclaredMethod(methodName, (Class<?>[]) null).invoke(sqlObject, (Object[]) null);					
					if(!first) {
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
				catch(NoSuchMethodException nsme) {
					//this means the field doesn't have a getter, so we're moving on.
				}
			}
		}
		fieldsString.append(") ");
		valuesString.append(") ");
		query.append(fieldsString.toString()).append(valuesString.toString());
	}
	
	private void replaceValues() throws SQLException, DataConnectionException {
		System.out.println(String.format(getQuery().replaceAll("\\?", "%s"), values.toArray()));
		for(int i = 0; i < values.size(); i++) {
			Object object = values.get(i);
			if(object instanceof String) {
				statement.setString(i + 1, (String)object);
			}else if(object instanceof Float) {
				statement.setFloat(i + 1, (Float)object);
			}
			else if(object instanceof Integer) {
				statement.setInt(i + 1, (Integer) object);
			}
			else if(object instanceof Long) {
				statement.setLong(i + 1, (Long) object);
			}
			else if(object instanceof Double) {
				statement.setDouble(i + 1, (Double) object);
			}
			else if(object instanceof Date) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = df.format((Date) object);
				statement.setObject(i + 1, date); 
			}
			else if(object == null) {
				statement.setNull(i + 1, Types.NULL);
			}
			else {
				throw new RuntimeException(object.getClass() + " " + object.toString() + " is not a supported object");
			}
		}	
	}
	
	private String getPkField() {
		String pkField = null;
		for(Field field : this.clazz.getDeclaredFields()) {
			if(field.getAnnotation(PrimaryKey.class) != null) {
				pkField = field.getName();
			}
		}
		return pkField;
	}
	
	private Object getPkValue(String pkField) throws DataConnectionException {
		try {
			return this.clazz.getMethod("get" + capitalize(pkField), (Class<?>[]) null).invoke(this.sqlObject, (Object[]) null);
		}
		catch(Exception e){
			throw new DataConnectionException("Could not get pkValue");
		}
	}

	
	private void executeStatement() throws DataConnectionException {
		try {
			Connection connection = DataConnectionManager.getConnection();
			if(connection == null) {
				throw new DataConnectionException("Connection is not initialized");
			}
			// Try to defined the where based on if there is an auto incremented id on the table (acting as pk).
			if(!whereDefined && (statementType == StatementType.UPDATE || statementType == StatementType.DELETE)) {
				String pkField = getPkField();
				Object pkValue = getPkValue(pkField);
				if(pkField == null) {
					throw new DataConnectionException("pkField couldn't be found... it's probably not declared on the object.");
				}
				if(sqlObject == null) {
					throw new DataConnectionException("An instance of the object " + clazz.getSimpleName() + " must be supplied to auto infer the upate/delete");		
				}
				where(pkField);
				eq(pkValue);
			}
			statement = connection.prepareStatement(getQuery());
			replaceValues();
			if(statementType == StatementType.SELECT) {
				resultSet = statement.executeQuery();
			}
			else {
				statement.execute();
			}
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void processColumn(Object object, String columnName, String columnType, ResultSet resultSet) throws SQLException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Object value = null;
		Class<?> type = clazz.getDeclaredField(columnName).getType();
		if(type == String.class) {
			value = resultSet.getString(columnName);
		}else if(type == Float.class) {
			value = resultSet.getFloat(columnName);
		}
		else if(type == Integer.class) {
			value = resultSet.getInt(columnName);
		}
		else if(type == Double.class) {
			value = resultSet.getDouble(columnName);
		}
		else if(type == Date.class) {
			value = resultSet.getDate(columnName);
		}
		else if(type == Long.class) {
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
		query = new StringBuilder();
		statement = null;
		resultSet = null;
		values.clear();
		clazz = null;
		statementType = null;
		sqlObject = null;
		whereDefined = false;
	}
	
	private StringBuilder query;
	private PreparedStatement statement;
	private ResultSet resultSet;
	private List<Object> values = new ArrayList<Object>();;
	private Class<?> clazz;
	private StatementType statementType;
	private Object sqlObject;
	private boolean whereDefined = false;
	
	private static final String SELECT = "select * from %s ";
	private static final String UPDATE = "update %s ";
	private static final String INSERT = "insert into %s";
	private static final String DELETE = "delete from %s ";
	private static final String WHERE = "where %s ";
	private static final String EQUALS = "= ? ";
	private static final String LIKE = "like ? ";
	private static final String AND = "and %s ";
	private static final String ORDER_BY = "order by %s ";
	private static final String ASC = "asc ";
	private static final String DESC = "desc ";
	private static final String SET = "set %s = ?";
	private static final String SET_AND = ", %s = ? ";
}
