package com.kremerkstudios.Sqlite;

import org.junit.Test;

public class TU_SqlStatement {

	@Test 
	void testSelect() throws DataConnectionException {
		SqlStatement stmt = new SqlStatement();
		stmt.select(User.class).getList();
	}
}
