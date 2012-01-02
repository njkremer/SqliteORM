package com.kremerkstudios.Sqlite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TU_SqlStatement {

	@Test 
	public void testSelect() throws DataConnectionException {
		createUser("Nick");
		User u = stmt.select(User.class).getList().get(0);
		
		assertEquals("Nick", u.getName());
		
		deleteUser(u);
	}
	
	@Test
	public void testSelectCount() throws DataConnectionException {
		createUser("Nick");
		assertEquals(1, stmt.select(User.class).getCount());
		deleteUser(stmt.select(User.class).getList().get(0));
	}
	
	@Test 
	public void testUpdate() throws DataConnectionException {
		createUser("Nick");
		User u = stmt.select(User.class).getList().get(0);
		
		u.setName("John");
		stmt.update(u).execute();
		
		User u2 = stmt.select(User.class).where("name").eq("John").getList().get(0);
		assertEquals("John", u2.getName());
		
		deleteUser(u);
	}
	
	public void createUser(String name) throws DataConnectionException {
		DataConnectionManager.init("test/test.db");
		User user = new User();
		user.setName(name);
		user.setPassword("123456");
		stmt.insert(user).execute();
	}
	
	public void deleteUser(User user) throws DataConnectionException {
		stmt.delete(user).execute();
	}
	private SqlStatement stmt = new SqlStatement();
}
