package com.kremerkstudios.Sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataConnectionManager {
	public static DataConnectionManager getInstance() {
		if(manager == null) {
			manager = new DataConnectionManager();
		}
		return manager;
	}
	
	public void init(String databaseName) {
		init(databaseName, System.getProperty("user.dir"));
	}
	
	public void init(String databaseName, String pathToDatabase) {
		try {
			Class.forName("org.sqlite.JDBC");
			//TODO this should probably be done with env variables and File.Seperator
			String separator = File.separator;
			connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s%s%s", pathToDatabase, 
					separator,
					databaseName));
		    connection.setAutoCommit(true);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DataConnectionManager select(Class<?> clazz) {
		this.clazz = clazz;
		this.query.append(String.format(SELECT, clazz.getName().toLowerCase()));
		this.statementType = StatementType.SELECT;
		return this;
	}
	
	public DataConnectionManager where(String field) {
		this.query.append(WHERE + field);
		return this;
	}
	
	public DataConnectionManager eq(Object value) {
		this.query.append(EQUALS);
		this.values.add(value);
		return this;
	}
	
	public DataConnectionManager like(String value) {
		this.query.append(LIKE);
		this.values.add(value);
		return this;
	}
	
	public DataConnectionManager and(String field) {
		this.query.append(AND + field);
		return this;
	}
	
	public DataConnectionManager orderBy(String field) {
		this.query.append(ORDER_BY + field);
		return this;
	}
	
	public DataConnectionManager asc() {
		this.query.append(ASC);
		return this;
	}
	public DataConnectionManager desc() {
		this.query.append(DESC);
		return this;
	}
	
	public void getList() {
		try {
			statement = connection.prepareStatement(query.toString());
			replaceValues();
			executeStatement();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private DataConnectionManager() {
		this.query = new StringBuilder();
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
	
	private static Connection connection;
	private static DataConnectionManager manager;
	private StringBuilder query;
	private PreparedStatement statement;
	private ResultSet resultSet;
	private List<Object> values = new ArrayList<Object>();;
	private Class<?> clazz;
	private StatementType statementType;
	
	private static final String SELECT = " select * from %s ";
	private static final String WHERE = " where ";
	private static final String EQUALS = " = ? ";
	private static final String LIKE = " like %?%";
	private static final String AND = " and ";
	private static final String ORDER_BY = " order by ";
	private static final String ASC = " asc ";
	private static final String DESC = " desc ";
	
}
