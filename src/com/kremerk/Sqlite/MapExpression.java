package com.kremerk.Sqlite;

import java.util.LinkedHashMap;

public class MapExpression {
    public MapExpression() {
        queryParts.put(StatementParts.SELECT, SELECT);
    }
    
    public Alias column(String columnString) {
        queryParts.put(StatementParts.COLUMN, queryParts.get(StatementParts.COLUMN).concat(columnString + " "));
        return new Alias(this);
    }
    
    public MapExpression distinct() {
        queryParts.put(StatementParts.SELECT, SELECT + DISTINCT);
        return this;
    }
    
    public String getQuery() {
        StringBuilder builder = new StringBuilder();
        for (StatementParts key : queryParts.keySet()) {
            builder.append(queryParts.get(key));
        }
        return builder.toString();
    }
    
    class Alias {
        public Alias(MapExpression me) {
            mapExpression = me;
        }
        
        public MapExpression as(String alias) {
            queryParts.put(StatementParts.COLUMN, queryParts.get(StatementParts.COLUMN).concat(String.format(AS, alias)));
            return mapExpression;
        }
        
        private MapExpression mapExpression;
        
    }
    
    private static final String SELECT = "select ";
    private static final String AS = "as %s ";
    private static final String DISTINCT = "distinct ";
    private LinkedHashMap<StatementParts, String> queryParts = new LinkedHashMap<StatementParts, String>();
    
}
