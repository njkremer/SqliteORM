package com.njkremer.Sqlite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.njkremer.Sqlite.ColumnExpression;

public class TU_ColumnExpression {

    @Test
    public void testQueryStringWithOneColumn() {
        ColumnExpression me = new ColumnExpression();
        me.column("strftime('%Y',date)").as("year");
        assertEquals("select strftime('%Y',date) as year ", me.getQuery());
    }

    @Test
    public void testQueryStringWithMultipleColumn() {
        ColumnExpression me = new ColumnExpression();
        me.column("strftime('%Y',date)").as("year").column("strftime('%M',date)").as("month");
        assertEquals("select strftime('%Y',date) as year, strftime('%M',date) as month ", me.getQuery());
    }
    
    @Test
    public void testQueryWithoutAliasWithOneColumn() {
        ColumnExpression me = new ColumnExpression();
        me.column("blah1");
        assertEquals("select blah1 ", me.getQuery());
    }
    
    @Test
    public void testQueryWithoutAliasWithMultipleColumns() {
        ColumnExpression me = new ColumnExpression();
        me.column("blah1").column("blah2");
        assertEquals("select blah1, blah2 ", me.getQuery());
    }

    @Test
    public void testQueryStringWithDistinct() {
        ColumnExpression me = new ColumnExpression();
        me.column("strftime('%Y',date)").as("year").distinct();
        assertEquals("select distinct strftime('%Y',date) as year ", me.getQuery());
    }
}
