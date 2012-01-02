package com.kremerkstudios.Sqlite;

public class DataConnectionException extends Exception {

	public DataConnectionException(String string) {
		super(string);
	}
	
	public DataConnectionException(String string, Throwable e) {
		super(string, e);
	}

	private static final long serialVersionUID = -8783760761696466541L;
	
}
