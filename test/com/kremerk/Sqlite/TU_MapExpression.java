package com.kremerk.Sqlite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TU_MapExpression {

    @Test
    public void testQueryStringWithOneColumn() throws DataConnectionException {
    	MapExpression me = new MapExpression();
    	me.column("strftime('%Y',date)").as("year");
    	assertEquals("select strftime('%Y',date) as year ", me.getQuery());
    }
    
    @Test
    public void testQueryStringWithMultipleColumn() {
    	MapExpression me = new MapExpression();
    	me.column("strftime('%Y',date)").as("year")
    	.column("strftime('%M',date)").as("month");
    	assertEquals("select strftime('%Y',date) as year, strftime('%M',date) as month ", me.getQuery());        
    }
    
    @Test
    public void testQueryStringWithDistinct() {
    	MapExpression me = new MapExpression();
    	me.column("strftime('%Y',date)").as("year").distinct();
    	assertEquals("select distinct strftime('%Y',date) as year ", me.getQuery());
    }
}
