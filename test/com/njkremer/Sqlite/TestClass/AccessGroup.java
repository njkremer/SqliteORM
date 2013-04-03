package com.njkremer.Sqlite.TestClass;

import com.njkremer.Sqlite.Annotations.AutoIncrement;
import com.njkremer.Sqlite.Annotations.PrimaryKey;

public class AccessGroup {
    
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
    
    @PrimaryKey
    @AutoIncrement
    private long id;
    private String name;
}
