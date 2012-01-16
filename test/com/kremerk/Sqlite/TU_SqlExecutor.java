package com.kremerk.Sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TU_SqlExecutor {

    @Test
    public void testBasicSelectStatement() throws DataConnectionException {
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.select(User.class).where("name").eq("nick").getQuery();
        assertEquals("select user.* from user where user.name = ?;", sql);
    }

    @Test
    public void testSelectWithAnds() throws DataConnectionException {
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.select(User.class).where("name").eq("nick").and("password").eq("123456").getQuery();
        assertEquals("select user.* from user where user.name = ? and user.password = ?;", sql);
    }

    @Test
    public void testSelectWithLike() throws DataConnectionException {
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.select(User.class).where("name").like("%nick").and("password").eq("123456").getQuery();
        assertEquals("select user.* from user where user.name like ? and user.password = ?;", sql);
    }

    @Test
    public void testSelectOrderBy() throws DataConnectionException {
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.select(User.class).where("name").like("%nick").and("password").eq("123456").orderBy("name").asc().getQuery();
        assertEquals("select user.* from user where user.name like ? and user.password = ? order by name asc;", sql);

        sql = executor.select(User.class).where("name").like("%nick").and("password").eq("123456").orderBy("name").desc().getQuery();
        assertEquals("select user.* from user where user.name like ? and user.password = ? order by name desc;", sql);
    }

    @Test
    public void testUpdate() throws DataConnectionException {
        User user = new User();
        user.setName("nick");
        user.setPassword("123456");
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.update(user).where("id").eq(1).getQuery();
        assertEquals("update user set name = ?, password = ? where user.id = ?;", sql);
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
        assertEquals("delete from user where user.name = ?;", sql);
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

        User user = e.select(User.class).where("name").like("N%").getList().get(0);
        user.setName("John");
        e.update(user).where("id").eq(user.getId()).execute();

        User newUser = e.select(User.class).where("name").eq("John").getList().get(0);
        assertEquals("John", newUser.getName());
        assertEquals("123456", newUser.getPassword());

        deleteUser(newUser);
    }

    @Test
    public void testInferredDeleteInDb() throws DataConnectionException {
        createUser("Nick");

        User user = e.select(User.class).where("name").like("N%").getList().get(0);
        e.delete(user).execute();
        List<User> users = e.select(User.class).getList();
        assertEquals(0, users.size());

    }

    @Test
    public void testInferredUpdateInDb() throws DataConnectionException {
        createUser("Nick");

        User user = e.select(User.class).where("name").like("N%").getList().get(0);
        user.setName("John");
        e.update(user).execute();

        User newUser = e.select(User.class).where("name").eq("John").getList().get(0);
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

        User user = e.select(User.class).where("name").like("N%").getList().get(0);
        user.setPassword(null);
        e.update(user).execute();
        user = e.select(User.class).where("name").like("N%").getList().get(0);
        assertNull(user.getPassword());

        deleteUser(user);
    }

    @Test
    public void testUpdatingAnAutoIncrementedField() throws DataConnectionException {
        createUser("Nick");

        User user = e.select(User.class).where("name").like("N%").getList().get(0);
        user.setId(new Long(55));
        try {
            e.update(user).where("name").eq("name").execute();
            fail("This method should have thrown an error by now");
        }
        catch (DataConnectionException e) {
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

    @Test
    public void testGettingMapInDb() throws DataConnectionException {
        createUser("Nick");

        List<Map<String, Object>> map = e.select(User.class).where("name").eq("Nick").getMap(new MapExpression().column("name").as("name1").column("id").as("userid"));

        assertEquals("Nick", map.get(0).get("name1"));
        assertEquals(1, map.get(0).get("userid"));
        deleteUser(e.select(User.class).getList().get(0));
    }

    @Test
    public void testGettingVariousDataTypes() throws DataConnectionException {
        DataConnectionManager.init("test/test.db");
        TestObject test = new TestObject();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 0, 1, 12, 00, 00);
        calendar.set(Calendar.MILLISECOND, 0);

        test.setDateType(calendar.getTime());
        test.setDoubleType(20.5);
        test.setFloatType(3.14159f);
        test.setIntType(42);
        test.setLongType(1234567890l);
        test.setStringType("Hello World!");
        test.setBooleanType(true);

        te.insert(test).execute();

        TestObject test2 = te.select(TestObject.class).getList().get(0);

        assertEquals(20.5, test2.getDoubleType(), 0.0);
        assertEquals(new Float(3.14159f), test2.getFloatType());
        assertEquals(new Integer(42), test2.getIntType());
        assertEquals(new Long(1234567890l), test2.getLongType());
        assertEquals("Hello World!", test2.getStringType());
        assertEquals(calendar.getTime().getTime(), test2.getDateType().getTime());
        assertEquals(true, test2.isBooleanType());

        te.delete(test).where("intType").eq(42).execute();

    }

    @Test
    public void testMultiTreading() throws DataConnectionException, InterruptedException {
        createUser("Nick");
        User user = e.select(User.class).where("name").like("N%").getList().get(0);

        ExecutorService pool = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 1000; i++) {
            pool.execute(new ThreadTest(i));
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        deleteUser(user);
    }
    
    public class ThreadTest implements Runnable {
        public ThreadTest(int i) {
            this.i = i;
            this.e = new SqlExecutor<User>().select(User.class);
        }

        public void run() {
            try {
                System.out.print(i + " ");
                e.getList();
            }
            catch (DataConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        
        private int i;
        private SqlExecutor<?> e;

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
    private SqlExecutor<com.kremerk.Sqlite.TestObject> te = new SqlExecutor<com.kremerk.Sqlite.TestObject>();

}
