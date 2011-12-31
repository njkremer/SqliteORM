package com.kremerkstudios.Sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.kremerkstudios.Sqlite.Annotations.AutoIncrement;

public class SqlExecutor {
	public SqlExecutor select(Class<?> clazz) {
		this.clazz = clazz;
		query = new StringBuilder();
		query.append(String.format(SELECT, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.SELECT;
		values.clear();
		return this;
	}
	
	public SqlExecutor update(Object object) {
		clazz = object.getClass();
		query = new StringBuilder();
		query.append(String.format(UPDATE, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.UPDATE;
		sqlObject = object;
		values.clear();
		return this;
	}
	
	public SqlExecutor insert(Object object) throws DataConnectionException {
		clazz = object.getClass();
		query = new StringBuilder();
		query.append(String.format(INSERT, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.INSERT;
		sqlObject = object;
		values.clear();
		try {
			prepareInsert();			
		}
		catch(Exception e) {
			throw new DataConnectionException("Error running insert");
		}
		return this;
	}
	
	public SqlExecutor delete(Class<?> clazz) throws DataConnectionException {
		this.clazz = clazz;
		query = new StringBuilder();
		query.append(String.format(DELETE, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.DELETE;
		values.clear();
		return this;
	}
	
	public SqlExecutor where(String field) throws DataConnectionException {
		if(statementType == StatementType.UPDATE) {
			try {
				prepareUpdate(field);
			}
			catch(Exception e) {
				throw new DataConnectionException("Error running update");
			}
		}
		query.append(String.format(WHERE, field));
		return this;
	}
	


	public SqlExecutor eq(Object value) {
		query.append(EQUALS);
		values.add(value);
		return this;
	}
	
	public SqlExecutor like(String value) {
		query.append(LIKE);
		values.add(value);
		return this;
	}
	
	public SqlExecutor and(String field) {
		query.append(String.format(AND, field));
		return this;
	}
	
	public SqlExecutor orderBy(String field) {
		query.append(String.format(ORDER_BY, field));
		return this;
	}
	
	public SqlExecutor asc() {
		query.append(ASC);
		return this;
	}
	public SqlExecutor desc() {
		query.append(DESC);
		return this;
	}
	
	public SqlExecutor execute() throws DataConnectionException {
		try {
			Connection connection = DataConnectionManager.getConnection();
			if(connection == null) {
				throw new DataConnectionException("Connection is not initialized");
			}
			statement = connection.prepareStatement(getQuery());
			replaceValues();
			executeStatement();
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
		return this;
	}
	
	
	
	public String getQuery() {
		return query.toString().trim().concat(";");
	}
	
	public <T> List<T> getList() {
		try {
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
	
	private <T> List<T> processResults() throws SQLException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
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
	
	
	private void prepareUpdate(String field) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		boolean first = true;
		for(Method method : clazz.getDeclaredMethods()) {
			String methodName = method.getName();
			String fieldName = methodName.replaceFirst("(get|is)", "").toLowerCase();
			if((methodName.startsWith("get") || methodName.startsWith("is"))
				&& !fieldName.equals(field)) {
				Object value = method.invoke(sqlObject, (Object[]) null);
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
		}
	}
	
	private void prepareInsert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		StringBuilder fieldsString = new StringBuilder("(");
		StringBuilder valuesString = new StringBuilder("values(");
		
		boolean first = true;
		for(Field field : clazz.getDeclaredFields()) {
			String methodName = field.getType() == Boolean.class ? "is" : "get" + capitalize(field.getName().toLowerCase());
			String fieldName = field.getName().toLowerCase();
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
	
	private void replaceValues() throws SQLException {
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
			else {
				throw new RuntimeException(object.getClass() + " " + object.toString() + " is not a supported object");
			}
		}	
	}
	
	private void executeStatement() throws SQLException {
		if(statementType == StatementType.SELECT) {
			resultSet = statement.executeQuery();
		}
		else {
			statement.execute();
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
	
	private StringBuilder query;
	private PreparedStatement statement;
	private ResultSet resultSet;
	private List<Object> values = new ArrayList<Object>();;
	private Class<?> clazz;
	private StatementType statementType;
	private Object sqlObject;
	
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
