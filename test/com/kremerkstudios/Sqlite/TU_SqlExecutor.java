package com.kremerkstudios.Sqlite;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

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
		assertEquals("update user set name = ?, password = ? where id = ?;", sql);
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
	
	@Test
	public void testSelectFromDb() throws DataConnectionException {
		createUser("Nick");
		
		User newUser = (User) e.select(User.class).where("name").eq("Nick").execute().getList().get(0);
		assertEquals("Nick", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test
	public void testSelectWithLikeFromDb() throws DataConnectionException {
		createUser("Nick");
		
		User newUser = (User) e.select(User.class).where("name").like("N%").execute().getList().get(0);
		assertEquals("Nick", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test
	public void testSortingFromDb() throws DataConnectionException {
		createUser("Nick");
		createUser("John");
		
		List<User> users = e.select(User.class).orderBy("name").desc().execute().getList();
		
		assertEquals(2, users.size());
		assertEquals("Nick", users.get(0).getName());
		
		deleteUser(users.get(0));
		deleteUser(users.get(1));
	}
	
	@Test
	public void testUpdatingInDb() throws DataConnectionException {
		createUser("Nick");
		
		User user = (User) e.select(User.class).where("name").like("N%").execute().getList().get(0);
		user.setName("John");
		e.update(user).where("id").eq(user.getId()).execute();
		
		User newUser = (User) e.select(User.class).where("name").eq("John").execute().getList().get(0);
		assertEquals("John", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test 
	public void testInferredDeleteInDb() throws DataConnectionException {
		createUser("Nick");
		
		User user = (User) e.select(User.class).where("name").like("N%").execute().getList().get(0);
		e.delete(user).execute();
		List<User> users = e.select(User.class).execute().getList();
		assertEquals(0, users.size());
				
	}
	
	@Test
	public void testInferredUpdateInDb() throws DataConnectionException {
		createUser("Nick");
		
		User user = (User) e.select(User.class).where("name").like("N%").execute().getList().get(0);
		user.setName("John");
		e.update(user).execute();
		
		User newUser = (User) e.select(User.class).where("name").eq("John").execute().getList().get(0);
		assertEquals("John", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	public void createUser(String name) throws DataConnectionException {
		DataConnectionManager.init("test/test.db");
		User user = new User();
		user.setName(name);
		user.setPassword("123456");
		e.insert(user).execute();
	}
	
	public void deleteUser(User user) throws DataConnectionException {
		e.delete(user).execute();
	}
	
	private SqlExecutor e = new SqlExecutor();
	
	
	
}
