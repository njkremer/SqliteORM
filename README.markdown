# SqliteORM - A Lightweight ORM For SQLite in Java

## Quicklinks:
* [API JavaDoc](http://kremerk.github.com/SqliteORM/javadoc/)
* [GitHub Wiki](https://github.com/kremerk/SqliteORM/wiki)

This project is designed to be a very lightweight ORM for interacting with a SQLite Database. The concept behind it is more convention over configuration. Meaning that, to map an Java Object to a database table, there are very few steps needed:

Contents:

1. ["Mapping" an Object to a Table](#mapping)
1. [Slightly More Advanced "Mapping"](#advanced)
1. [Interacting with the Database](#interacting)
1. [Working With Non-Objects From The Database](#nonObjects)
1. [TODOs](#todo)

***

<a name="mapping"></a>
# "Mapping" an Object to a Table

1. Create a Java Object (POJO style) with fields, and getters/setters that follow the POJO convention:
    * getters and setters work like getXXXX and setXXXX where XXXX = the field name with a capital first letter
    * boolean getters follow the "is" convention, so a boolean would be setXXXX and isXXXX.
1. The Object's name matches a table in the database. The database's table name should be an all **lowercase** version of the Java Object

That's it!

Example:

    public class User {
        private String name;
        private String password;

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

Would map to a database table that looks like this, with the name of **user**:
<table>
    <tr>
        <th>column name</th><th>type</th>
    </tr>
    <tr><td>name</td><td>text</td><tr>
    <tr><td>password</td><td>text</td><tr>
</table>


***

<a name="advanced"></a>
# Slightly More Advanced "Mapping"

For slightly more "advanced" usage, you can specify two different types of Annotations on your POJO.

* @PrimaryKey - This annotation is to be used on a field of your POJO. It should be put on the field that would correlate to the primary key of the table in the database. It's useful for things like doing an update/delete without specifying a "where" clause (See JavaDoc API for more info)
* @AutoIncrement - This should be specified on any database columns that are being auto-incremented by the database, so that we know to not specify anything for that field when inserting and let the database handle it.

Example:

    public class User {
        @AutoIncrement
        @PrimaryKey
        private long id;
        private String name;
        private String password;
    
        public long getId() {
            return id;
        }
    
        public void setId(long id) {
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

Which would map to a table named **user** that looks like:

<table>
    <tr>
        <th>column name</th><th>type</th><th>PK?</th>
    </tr>
    <tr><td>id</td><td>auto incremented int</td><td>yes</td><tr>
    <tr><td>name</td><td>text</td><td>no</td><tr>
    <tr><td>password</td><td>text</td><td>no</td><tr>
</table>

***

<a name="interacting"></a>
# Interacting with the Database

First you'll need to initialized the connection to the database. You do this with the `DataConnectionManager.init(String)` or `DataConnectionManager.init(String, String)` method.

Once the connection is initialized you will created an instance of SqlStatement for a call to the database. Typically you'd want to create a new SqlStatement for each "set" of calls you'd be
making. Note that starting a call new call to select/update/insert/delete from a SqlStatement will reset an internal state that is used with the SqlStatement/SqlExecutor instance, so reusing one
for multiple queries should be fine.

Code Examples:

Retrieving a User from the database:

    User u = new SqlStatement().select(User.class).where("name").eq("Nick").getList().get(0);

Retreiving a list of Users whose name starts with the letter 'A':

    List<User> users = new SqlStatement().select(User.class).where("name").like("A%").getList();

Changing the User with the name = Nick's password:

    SqlStatement stmt = new SqlStatement();
    User nick = stmt.select(User.class).where("name").eq("nick");
    nick.setPassword("ABC123");
    stmt.update(nick).execute(); // Note this is if you define a @PrimaryKey annotation 
                                 //(see 'Slightly More Advanced "Mapping"' section for more info).

Inserting a new User into the database:

    User newUser = new User();
    newUser.setName("Bob");
    newUser.setPassword("123456");
    new SqlStatement().insert(newUser).execute();

Retreiving a list of all users in descending order:

    List<User> users = new SqlStatement().select(User.class).orderBy("name").desc().getList();

Getting just the count of all the users in the database:

    int numOfUsers = new SqlStatement().select(User.class).getCount();

The [JavaDocs](http://kremerk.github.com/SqliteORM/javadoc/) have a pretty good outline of what is possible with interactions. Note that after you start your SqlStatement a SqlExecutor is returned
for function chaining. So when looking at the JavaDocs you may want to look at the [SqlExecutor](http://kremerk.github.com/SqliteORM/javadoc/com/kremerk/Sqlite/SqlExecutor.html) class.

***

<a name="nonObjects"></a>
# Working With Non-Objects From The Database

***


<a name="todo"></a>
# TODOs

1. Add support for OR in where clause
1. Add support for more complex logic with where clause, e.g. a AND (b OR c)
1. Add support for more complex ording, e.g. Order by a desc THEN b asc