package com.kremerkstudios.Sqlite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.kremerkstudios.Sqlite.Annotations.AutoIncrement;

public class TU_SqlExecutor {

	@Test
	public void testBasicSelectStatement() throws DataConnectionException {
		SqlExecutor executor =  new SqlExecutor();
		String sql = executor.select(User.class).where("name").eq("nick").getQuery();
		assertEquals("select * from user where name = ?;", sql);
	}
	
	@Test
	public void testSelectWithAnds() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.select(User.class)
		.where("name").eq("nick")
		.and("password").eq("123456")
		.getQuery();
		assertEquals("select * from user where name = ? and password = ?;", sql);
	}
	
	@Test
	public void testSelectWithLike() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.getQuery();
		assertEquals("select * from user where name like ? and password = ?;", sql);
	}
	
	@Test
	public void testSelectOrderBy() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.orderBy("name").asc()
		.getQuery();
		assertEquals("select * from user where name like ? and password = ? order by name asc;", sql);
		
		sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.orderBy("name").desc()
		.getQuery();
		assertEquals("select * from user where name like ? and password = ? order by name desc;", sql);
	}
	
	@Test
	public void testUpdate() throws DataConnectionException {
		User user = new User();
		user.setName("nick");
		user.setPassword("123456");
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.update(user)
		.where("id").eq(1).getQuery();
		assertEquals("update user set name = ? and password = ? where id = ?;", sql);
	}
	
	@Test
	public void testInsert() throws DataConnectionException {
		User user = new User();
		user.setName("nick");
		user.setPassword("123456");
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.insert(user).getQuery();
		assertEquals("insert into user(name, password) values(?, ?);", sql);
	}
	
	@Test
	public void testDelete() throws DataConnectionException {
		SqlExecutor executor = new SqlExecutor();
		String sql = executor.delete(User.class).where("name").eq("nick").getQuery();
		assertEquals("delete from user where name = ?;", sql);
	}
	
	public class User {
		@AutoIncrement
		private Long id;
		private String name;
		private String password;
		
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
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
