package com.kremerk.Sqlite.TestClass;

import java.util.Set;

import com.kremerk.Sqlite.Annotations.AutoIncrement;
import com.kremerk.Sqlite.Annotations.OneToMany;
import com.kremerk.Sqlite.Annotations.PrimaryKey;

public class BadOneToMany {
    @AutoIncrement
    @PrimaryKey
    private long id;
    
    @OneToMany("userId")
    private Set<Thing> things;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<Thing> getThings() {
        return things;
    }

    public void setThings(Set<Thing> things) {
        this.things = things;
    }
    
    
    
}
