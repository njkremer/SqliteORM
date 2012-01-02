package com.kremerkstudios.Sqlite;

public class SqlStatement {
	
	public <T> SqlExecutor<T> select(Class<T> clazz) {
		return new SqlExecutor<T>().select(clazz);
	}
	
	public <T> SqlExecutor<T> update(T object) {
		return new SqlExecutor<T>().update(object);
	}
	
	public <T> SqlExecutor<T> insert(T object) throws DataConnectionException {
		return new SqlExecutor<T>().insert(object);
	}
	
	public <T> SqlExecutor<T> delete(Class<T> clazz) throws DataConnectionException {
		return new SqlExecutor<T>().delete(clazz);
	}
	
	public <T> SqlExecutor<T> delete(T object) {
		return new SqlExecutor<T>().delete(object);
	}

}
