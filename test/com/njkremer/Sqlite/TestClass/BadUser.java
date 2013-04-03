package com.njkremer.Sqlite.TestClass;

import java.util.List;

import com.njkremer.Sqlite.Annotations.AutoIncrement;
import com.njkremer.Sqlite.Annotations.OneToMany;

public class BadUser {
    @AutoIncrement
    private long id;
    
    @OneToMany("userId")
    private List<Thing> things;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Thing> getThings() {
        return things;
    }

    public void setThings(List<Thing> things) {
        this.things = things;
    }
    
    
    

}