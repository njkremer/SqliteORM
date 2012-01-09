package com.kremerk.Sqlite;

import java.util.LinkedHashMap;

public class MapExpression {
    public MapExpression() {
        queryParts.put(StatementParts.SELECT, SELECT);
        queryParts.put(StatementParts.COLUMN, "");
    }
    
    public MapExpression column(String columnString) {
        queryParts.put(StatementParts.COLUMN, queryParts.get(StatementParts.COLUMN).concat(columnString + ", "));
        return this;
    }
    
    public MapExpression as(String alias) {
        String columns = queryParts.get(StatementParts.COLUMN);
        columns = columns.substring(0, columns.lastIndexOf(",")).concat(" ");
        queryParts.put(StatementParts.COLUMN, columns.concat(String.format(AS, alias)));
        return this;
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
        String query = builder.toString();
        int commaIndex = query.lastIndexOf(",");
        int trimIndex = commaIndex == -1 ? query.length() : commaIndex;
        return query.substring(0, trimIndex).concat(" ");
    }
    
    private static final String SELECT = "select ";
    private static final String AS = "as %s, ";
    private static final String DISTINCT = "distinct ";
    private LinkedHashMap<StatementParts, String> queryParts = new LinkedHashMap<StatementParts, String>();
    
}
