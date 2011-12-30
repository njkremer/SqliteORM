package com.kremerkstudios.Sqlite;

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

public class SqlExecutor {
	public SqlExecutor select(Class<?> clazz) {
		this.clazz = clazz;
		query = new StringBuilder();
		query.append(String.format(SELECT, clazz.getSimpleName().toLowerCase()));
		statementType = StatementType.SELECT;
		return this;
	}
	
	public SqlExecutor where(String field) {
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
	
	public List<Object> getList() throws DataConnectionException {
		try {
			Connection connection = DataConnectionManager.getConnection();
			if(connection == null) {
				throw new DataConnectionException("Connection is not initialized");
			}
			statement = connection.prepareStatement(query.toString());
			replaceValues();
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
		}
		return new ArrayList<Object>();
	}
	
	public String getQuery() {
		return query.toString();
	}
	
	private void replaceValues() throws SQLException {
		System.out.println(String.format(query.toString().replaceAll("\\?", "%s"), values.toArray()));
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
	
	private List<Object> processResults() throws SQLException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		List<Object> objects = new ArrayList<Object>();
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		
		// Get column data
		for(int i = 1; i <= columnCount; i++) {
			columns.add(resultSet.getMetaData().getColumnName(i));
			types.add(resultSet.getMetaData().getColumnTypeName(i));
		}
		
		while(resultSet.next()) {
			Object object = clazz.newInstance();
			for(int i = 1; i <= columnCount; i++) {
				processColumn(object, i, columns.get(i), types.get(i), resultSet);
			}
			objects.add(object);
		}
		return objects;
	}
	
	private void processColumn(Object object, int index, String columnName, String columnType, ResultSet resultSet) throws SQLException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object value = null;
		if(columnType.equals("String")) {
			value = resultSet.getString(index);
		}else if(object.equals("Float")) {
			value = resultSet.getFloat(index);
		}
		else if(object.equals("Int")) {
			value = resultSet.getInt(index);
		}
		else if(object.equals("Double")) {
			value = resultSet.getDouble(index);
		}
		else if(object.equals("Date")) {
			value = resultSet.getDate(index);
		}
		String methodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
		Method method = clazz.getDeclaredMethod(methodName, (Class<?>[])null);
		method.invoke(object, value);
	}
	
	private StringBuilder query;
	private PreparedStatement statement;
	private ResultSet resultSet;
	private List<Object> values = new ArrayList<Object>();;
	private Class<?> clazz;
	private StatementType statementType;
	
	private static final String SELECT = "select * from %s ";
	private static final String WHERE = "where %s ";
	private static final String EQUALS = "= ? ";
	private static final String LIKE = "like ? ";
	private static final String AND = "and %s ";
	private static final String ORDER_BY = "order by %s ";
	private static final String ASC = "asc ";
	private static final String DESC = "desc ";
}
