package com.kremerk.Sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.kremerk.Sqlite.TestClass.AccessGroup;
import com.kremerk.Sqlite.TestClass.BadOneToMany;
import com.kremerk.Sqlite.TestClass.BadUser;
import com.kremerk.Sqlite.TestClass.TestObject;
import com.kremerk.Sqlite.TestClass.Thing;
import com.kremerk.Sqlite.TestClass.User;
import com.kremerk.Sqlite.TestClass.UserAccessGroup;

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
    public void testOneJoin() throws DataConnectionException {
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.select(User.class)
            .join(UserAccessGroup.class, "userId", User.class, "id")
            .where(UserAccessGroup.class, "userId").eq(5).getQuery();
        assertEquals("select user.* from user join useraccessgroup on useraccessgroup.userId = user.id where useraccessgroup.userId = ?;", sql);
        
    }
    
    @Test
    public void testMultipeJoins() throws DataConnectionException {
        SqlExecutor<User> executor = new SqlExecutor<User>();
        String sql = executor.select(User.class)
            .join(UserAccessGroup.class, "userId", User.class, "id")
            .join(AccessGroup.class, "id", UserAccessGroup.class, "groupId")
            .where(AccessGroup.class, "id").eq(5).getQuery();
        assertEquals("select user.* from user join useraccessgroup on useraccessgroup.userId = user.id join accessgroup on accessgroup.id = useraccessgroup.groupId where accessgroup.id = ?;", sql);
        
    }
    
    @Test
    public void testAOneToManyRelationship() throws DataConnectionException {
        SqlExecutor<Thing> thingExectuor = new SqlExecutor<Thing>();
        User u = new User();
        u.setId(1);
        u.setName("Nick");
        u.setPassword("123456");
        
        String sql = thingExectuor.select(Thing.class).from(u).getQuery();
        assertEquals("select thing.* from thing join user on user.id = thing.userId;", sql);
    }
    
    @Test
    public void testAOneToManyRelationshipWithAWhereClause() throws DataConnectionException {
        SqlExecutor<Thing> thingExectuor = new SqlExecutor<Thing>();
        User u = new User();
        u.setId(1);
        u.setName("Nick");
        u.setPassword("123456");
        
        String sql = thingExectuor.select(Thing.class).from(u).where("name").eq("blah").getQuery();
        assertEquals("select thing.* from thing join user on user.id = thing.userId where thing.name = ?;", sql);
    }
    
    @Test
    public void testAOneToManyRelationshipWithWrongContainerType() {
        SqlExecutor<Thing> thingExectuor = new SqlExecutor<Thing>();
        BadOneToMany obj = new BadOneToMany();
        
        try {
            thingExectuor.select(Thing.class).from(obj).getQuery();
            fail("An exception should have been thrown");
        }
        catch(DataConnectionException e) {
            assertEquals("The return type of a OneToMany relationship must be a List Type for field things", e.getMessage());
        }
    }
    
    
    @Test
    public void testSelectingFromAnObjectThatHasNoPK() {
        SqlExecutor<AccessGroup> accessGroup = new SqlExecutor<AccessGroup>();
        BadUser uag = new BadUser();
        try {
            accessGroup.select(AccessGroup.class).from(uag).getQuery();
            fail("An exception should have been thrown.");
        }
        catch(DataConnectionException e) {
            assertEquals("pkField on the target object couldn't be found... it's probably not declared on the object. To use this method the target object must have a PrimaryKey defined.", e.getMessage());
        }
    }
    
    @Test
    public void testSelectingTheWrongTypeFromAClassThatHasAOneToManyRelationship() {
        SqlExecutor<AccessGroup> accessGroup = new SqlExecutor<AccessGroup>();
        User user = new User();
        try {
            accessGroup.select(AccessGroup.class).from(user).getQuery();
            fail("An exception should have been thrown.");
        }
        catch(DataConnectionException e) {
            assertEquals("A OneToMany was found, however it wasn't a list of type com.kremerk.Sqlite.TestClass.AccessGroup", e.getMessage());
        }
    }
    
    @Test
    public void testSelectingFromAnObjectThatHasNoOneToManyRelationship() {
        SqlExecutor<AccessGroup> accessGroup = new SqlExecutor<AccessGroup>();
        Thing thing  = new Thing();
        try {
            accessGroup.select(AccessGroup.class).from(thing).getQuery();
            fail("An exception should have been thrown.");
        }
        catch(DataConnectionException e) {
            assertEquals("No OneToMany reationship could be found on the class com.kremerk.Sqlite.TestClass.Thing", e.getMessage());
        }
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
    public void testGettingARelationshipObjectDireclty() throws DataConnectionException {
        createUser("Nick");
        createThing("Thing1");
        createThing("Thing2");
        createThing("Thing3");
        
        User nick = e.select(User.class).getFirst();
        List<Thing> nicksThings = nick.getThings();
        assertEquals(3, nicksThings.size());
        assertEquals("Thing1", nicksThings.get(0).getName());
        assertEquals("Thing2", nicksThings.get(1).getName());
        assertEquals("Thing3", nicksThings.get(2).getName());
        
        deleteUser(nick);
        deleteThing("Thing1");
        deleteThing("Thing2");
        deleteThing("Thing3");
    }

    @Test
    public void testToMakeSureNonRelationshipObjectArentProxies() throws DataConnectionException {
        createUser("Nick");
        createThing("Thing1");
        
        User nick = e.select(User.class).getFirst();
        Thing nicksThing = nick.getThings().get(0);
        
        assertTrue( "The User object has a relationship and should therefore have faulting, which means it shuld not be of time User.class.", nick.getClass() != User.class);
        assertEquals("Thing doesn't have a relationship and should be the an instance of Thing.class", nicksThing.getClass() , Thing.class);
        
        deleteUser(nick);
        deleteThing("Thing1");

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
    public void testJoinInDb() throws DataConnectionException {
        createUser("Nick");
        createGroup("Admin");
        createGroup("PowerUser");
        createGroup("User");
        
        User nick = e.select(User.class).getList().get(0);
        List<AccessGroup> groups = groupExecutor.select(AccessGroup.class).getList();
        
        createUserGroupLink(nick.getId(), groups.get(0).getId());
        createUserGroupLink(nick.getId(), groups.get(1).getId());
        
        List<AccessGroup> nicksGroups = groupExecutor.select(AccessGroup.class)
        .join(UserAccessGroup.class, "groupId", AccessGroup.class, "id")
        .join(User.class, "id", UserAccessGroup.class, "userId")
        .where(User.class, "name").eq("Nick").getList();
        
        assertEquals(2, nicksGroups.size());
        assertEquals("Admin", nicksGroups.get(0).getName());
        assertEquals("PowerUser", nicksGroups.get(1).getName());
        
        deleteUser(nick);
        deleteGroup("Admin");
        deleteGroup("PowerUser");
        deleteGroup("User");
        deleteUserGroupLinks(nick.getId());
        
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
    public void testSelectCountStatementInDb() throws DataConnectionException {
        createUser("Nick");

        assertEquals(1, e.select(User.class).where("name").eq("Nick").getCount());

        deleteUser(e.getList().get(0));
    }

    @Test
    public void testGettingMapInDb() throws DataConnectionException {
        createUser("Nick");

        List<Map<String, Object>> map = e.select(User.class).where("name").eq("Nick").getColumns(new ColumnExpression().column("name").as("name1").column("id").as("userid"));

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
        
        calendar.add(Calendar.HOUR, 1);
        test2.setDateType(calendar.getTime());
        test2.setDoubleType(25.6);
        test2.setFloatType(6.28318f);
        test2.setIntType(100);
        test2.setLongType(12345l);
        test2.setStringType("Goodbye World!");
        test2.setBooleanType(false);
        
        te.update(test2).where("intType").eq(42).execute();
        
        test = te.select(TestObject.class).getList().get(0);
        
        assertEquals(25.6, test.getDoubleType(), 0.0);
        assertEquals(new Float(6.28318f), test.getFloatType());
        assertEquals(new Integer(100), test.getIntType());
        assertEquals(new Long(12345l), test.getLongType());
        assertEquals("Goodbye World!", test.getStringType());
        assertEquals(calendar.getTime().getTime(), test.getDateType().getTime());
        assertEquals(false, test.isBooleanType());

        te.delete(TestObject.class).where("intType").eq(100).execute();

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
    
    public void createGroup(String name) throws DataConnectionException {
        AccessGroup group = new AccessGroup();
        group.setName(name);
        groupExecutor.insert(group).execute();
    }
    
    public void deleteGroup(String name) throws DataConnectionException {
        groupExecutor.delete(AccessGroup.class).where("name").eq(name).execute();
    }
    
    public void createUserGroupLink(long userId, long groupId) throws DataConnectionException {
        UserAccessGroup uag = new UserAccessGroup();
        uag.setGroupId(groupId);
        uag.setUserId(userId);
        uagExecutor.insert(uag).execute();
    }
    
    public void deleteUserGroupLinks(long userId) throws DataConnectionException {
        uagExecutor.delete(UserAccessGroup.class).where("userId").eq(userId).execute();
    }
    
    public void createThing(String thingName) throws DataConnectionException {
        Thing thing = new Thing();
        thing.setName(thingName);
        thing.setUserId(1);
        thingExecutor.insert(thing).execute();
    }
    
    public void deleteThing(String thingName) throws DataConnectionException {
        thingExecutor.delete(Thing.class).where("name").eq(thingName).execute();
    }

    private SqlExecutor<User> e = new SqlExecutor<User>();
    private SqlExecutor<AccessGroup> groupExecutor = new SqlExecutor<AccessGroup>();
    private SqlExecutor<UserAccessGroup> uagExecutor = new SqlExecutor<UserAccessGroup>();
    private SqlExecutor<Thing> thingExecutor = new SqlExecutor<Thing>();
    private SqlExecutor<com.kremerk.Sqlite.TestClass.TestObject> te = new SqlExecutor<com.kremerk.Sqlite.TestClass.TestObject>();

}
