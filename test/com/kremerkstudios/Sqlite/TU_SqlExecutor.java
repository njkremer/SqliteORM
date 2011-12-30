package com.kremerkstudios.Sqlite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.kremerkstudios.Sqlite.DataConnectionException;
import com.kremerkstudios.Sqlite.SqlExecutor;

public class TU_SqlExecutor {

	@Test
	public void testBasicSelectStatement() throws DataConnectionException {
		SqlExecutor executor =  new SqlExecutor();
		String sql = executor.select(User.class).where("name").eq("nick").getQuery();
		assertEquals("select * from user where name = ? ", sql);
	}
	
	@Test
	public void testSelectWithAnds() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.select(User.class)
		.where("name").eq("nick")
		.and("password").eq("123456")
		.getQuery();
		assertEquals("select * from user where name = ? and password = ? ", sql);
	}
	
	@Test
	public void testSelectWithLike() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.getQuery();
		assertEquals("select * from user where name like ? and password = ? ", sql);
	}
	
	@Test
	public void testSelectOrderBy() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.orderBy("name").asc()
		.getQuery();
		assertEquals("select * from user where name like ? and password = ? order by name asc ", sql);
		
		sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.orderBy("name").desc()
		.getQuery();
		assertEquals("select * from user where name like ? and password = ? order by name desc ", sql);
	}
	
	public class User {
		private int id;
		private String name;
		private String password;
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		
	}
}
