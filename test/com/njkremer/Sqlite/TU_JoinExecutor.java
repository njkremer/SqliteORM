package com.njkremer.Sqlite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.njkremer.Sqlite.JoinExecutor;
import com.njkremer.Sqlite.JoinExecutor.JoinType;
import com.njkremer.Sqlite.TestClass.User;
import com.njkremer.Sqlite.TestClass.UserAccessGroup;

public class TU_JoinExecutor {

    @Test
    public void testJoin() {
        JoinExecutor joinExecutor = new JoinExecutor();
        joinExecutor.join(UserAccessGroup.class, "userId", User.class, "id");
        
        assertEquals("join useraccessgroup on useraccessgroup.userId = user.id ", joinExecutor.getQuery());
    }
    
    @Test
    public void testInnerJoin() {
        JoinExecutor joinExecutor = new JoinExecutor();
        joinExecutor.setJoinType(JoinType.INNER);
        joinExecutor.join(UserAccessGroup.class, "userId", User.class, "id");
        
        assertEquals("inner join useraccessgroup on useraccessgroup.userId = user.id ", joinExecutor.getQuery());
    }
    
    @Test
    public void testLeftOuterJoin() {
        JoinExecutor joinExecutor = new JoinExecutor();
        joinExecutor.setJoinType(JoinType.LEFT_OUTER);
        joinExecutor.join(UserAccessGroup.class, "userId", User.class, "id");
        
        assertEquals("left outer join useraccessgroup on useraccessgroup.userId = user.id ", joinExecutor.getQuery());
    }

    
    @Test
    public void testRightOuterJoin() {
        JoinExecutor joinExecutor = new JoinExecutor();
        joinExecutor.setJoinType(JoinType.RIGHT_OUTER);
        joinExecutor.join(UserAccessGroup.class, "userId", User.class, "id");
        
        assertEquals("right outer join useraccessgroup on useraccessgroup.userId = user.id ", joinExecutor.getQuery());
    }
    
    @Test
    public void testEnum() {
        @SuppressWarnings("unused")
        JoinType j = JoinType.valueOf("INNER");
    }

}
