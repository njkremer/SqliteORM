package com.kremerkstudios.Sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

public class TU_SqlExecutor {

	@Test
	public void testBasicSelectStatement() throws DataConnectionException {
		SqlExecutor<User> executor =  new SqlExecutor<User>();
		String sql = executor.select(User.class).where("name").eq("nick").getQuery();
		assertEquals("select * from user where name = ?;", sql);
	}
	
	@Test
	public void testSelectWithAnds() throws DataConnectionException {
		SqlExecutor<User> executor = new SqlExecutor<User>();
		String sql = executor.select(User.class)
		.where("name").eq("nick")
		.and("password").eq("123456")
		.getQuery();
		assertEquals("select * from user where name = ? and password = ?;", sql);
	}
	
	@Test
	public void testSelectWithLike() throws DataConnectionException {
		SqlExecutor<User> executor = new SqlExecutor<User>();
		String sql = executor.select(User.class)
		.where("name").like("%nick")
		.and("password").eq("123456")
		.getQuery();
		assertEquals("select * from user where name like ? and password = ?;", sql);
	}
	
	@Test
	public void testSelectOrderBy() throws DataConnectionException {
		SqlExecutor<User> executor = new SqlExecutor<User>();
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
		SqlExecutor<User> executor = new SqlExecutor<User>();
		String sql = executor.update(user)
		.where("id").eq(1).getQuery();
		assertEquals("update user set name = ?, password = ? where id = ?;", sql);
	}
	
	@Test
	public void testInsert() throws DataConnectionException {
		User user = new User();
		user.setName("nick");
		user.setPassword("123456");
		SqlExecutor<User> executor = new SqlExecutor<User>();
		String sql = executor.insert(user).getQuery();
		assertEquals("insert into user(name, password) values(?, ?);", sql);
	}
	
	@Test
	public void testDelete() throws DataConnectionException {
		SqlExecutor<User> executor = new SqlExecutor<User>();
		String sql = executor.delete(User.class).where("name").eq("nick").getQuery();
		assertEquals("delete from user where name = ?;", sql);
	}
	
	@Test
	public void testSelectFromDb() throws DataConnectionException {
		createUser("Nick");
		
		User newUser = e.select(User.class).where("name").eq("Nick").getList().get(0);
		assertEquals("Nick", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test
	public void testSelectWithLikeFromDb() throws DataConnectionException {
		createUser("Nick");
		
		User newUser = e.select(User.class).where("name").like("N%").getList().get(0);
		assertEquals("Nick", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test
	public void testSortingFromDb() throws DataConnectionException {
		createUser("Nick");
		createUser("John");
		
		List<User> users = e.select(User.class).orderBy("name").desc().getList();
		
		assertEquals(2, users.size());
		assertEquals("Nick", users.get(0).getName());
		
		deleteUser(users.get(0));
		deleteUser(users.get(1));
	}
	
	@Test
	public void testUpdatingInDb() throws DataConnectionException {
		createUser("Nick");
		
		User user =  e.select(User.class).where("name").like("N%").getList().get(0);
		user.setName("John");
		e.update(user).where("id").eq(user.getId()).execute();
		
		User newUser =  e.select(User.class).where("name").eq("John").getList().get(0);
		assertEquals("John", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test 
	public void testInferredDeleteInDb() throws DataConnectionException {
		createUser("Nick");
		
		User user =  e.select(User.class).where("name").like("N%").getList().get(0);
		e.delete(user).execute();
		List<User> users = e.select(User.class).getList();
		assertEquals(0, users.size());
				
	}
	
	@Test
	public void testInferredUpdateInDb() throws DataConnectionException {
		createUser("Nick");
		
		User user =  e.select(User.class).where("name").like("N%").getList().get(0);
		user.setName("John");
		e.update(user).execute();
		
		User newUser =  e.select(User.class).where("name").eq("John").getList().get(0);
		assertEquals("John", newUser.getName());
		assertEquals("123456", newUser.getPassword());
		
		deleteUser(newUser);
	}
	
	@Test
	public void testDeletingAUserNotInTheDb() throws DataConnectionException {
		DataConnectionManager.init("test/test.db");
		e.delete(User.class).where("name").eq("Nick").execute();
	}
	
	@Test
	public void testUpdatingAUserNotInTheDb() throws DataConnectionException {
		DataConnectionManager.init("test/test.db");
		User u = new User();
		u.setName("Nick");
		u.setPassword("123456");
		u.setId(new Long(45));
		e.update(u).execute();
	}
	
	@Test
	public void testSettingNullDataType() throws DataConnectionException {
		createUser("Nick");
		
		User user =  e.select(User.class).where("name").like("N%").getList().get(0);
		user.setPassword(null);
		e.update(user).execute();
		user =  e.select(User.class).where("name").like("N%").getList().get(0);
		assertNull(user.getPassword());
		
		deleteUser(user);
	}
	
	@Test
	public void testUpdatingAnAutoIncrementedField() throws DataConnectionException {
		createUser("Nick");
		
		User user =  e.select(User.class).where("name").like("N%").getList().get(0);
		user.setId(new Long(55));
		try {
			e.update(user).where("name").eq("name").execute();
			fail("This method should have thrown an error by now");
		}
		catch(DataConnectionException e) {
			// pass
		}
		user.setId(new Long(1));
		deleteUser(user);
	}
	
	@Test
	public void testSelectCountStatementInDb() throws DataConnectionException {
		createUser("Nick");
		
		assertEquals(1, e.select(User.class).where("name").eq("Nick").getCount());
		
		deleteUser(e.getList().get(0));
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
	
	private SqlExecutor<User> e = new SqlExecutor<User>();
	
	
	
}
