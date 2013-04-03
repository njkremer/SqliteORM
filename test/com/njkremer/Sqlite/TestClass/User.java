package com.njkremer.Sqlite.TestClass;

import java.util.List;

import com.njkremer.Sqlite.Annotations.AutoIncrement;
import com.njkremer.Sqlite.Annotations.OneToMany;
import com.njkremer.Sqlite.Annotations.PrimaryKey;

public class User {
    @AutoIncrement
    @PrimaryKey
    private long id;
    private String name;
    private String password;
    
    @OneToMany("userId")
    private List<Thing> things;

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

    public List<Thing> getThings() {
        return things;
    }

    public void setThings(List<Thing> things) {
        this.things = things;
    }
    

}