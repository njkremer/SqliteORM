package com.kremerk.Sqlite.TestClass;

import com.kremerk.Sqlite.Annotations.AutoIncrement;
import com.kremerk.Sqlite.Annotations.PrimaryKey;

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