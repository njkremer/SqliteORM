# SqliteORM - A Lightweight ORM For SQLite in Java

This project is designed to be a very lightweight ORM for interacting with a SQLite Database. The concept behind it is more convention over configuration. Meaning that, to map an Java Object to a database table, there are very few steps needed:

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


# Slightly More Advanced Usage

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

