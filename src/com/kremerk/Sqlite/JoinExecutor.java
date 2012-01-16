package com.kremerk.Sqlite;

import java.util.LinkedHashMap;

public class JoinExecutor {
    public JoinExecutor join(Class<?> clazz) {
        queryParts.put(StatementParts.JOIN, String.format(JOIN, clazz.getSimpleName().toLowerCase()));
        return this;
    }
    
    public JoinExecutor on(Class<?> clazz1, String fieldName) {
        String join = queryParts.get(StatementParts.JOIN);
        queryParts.put(StatementParts.JOIN, join.concat(String.format(ON, clazz1.getSimpleName().toLowerCase(), fieldName)));
        return this;
    }
    
    public JoinExecutor eq(Class<?> clazz2, String fieldName) {
        String join = queryParts.get(StatementParts.JOIN);
        queryParts.put(StatementParts.JOIN, join.concat(String.format(EQ, clazz2.getSimpleName().toLowerCase(), fieldName)));
        return this;
    }
    
     String getQuery() {
        StringBuilder builder = new StringBuilder();
        for (StatementParts key : queryParts.keySet()) {
            builder.append(queryParts.get(key));
        }
        queryParts.clear();
        String query = builder.toString();
        int commaIndex = query.lastIndexOf(",");
        int trimIndex = commaIndex == -1 ? query.length() : commaIndex;
        return query.substring(0, trimIndex).concat(" ");
    }
    
    private final static String JOIN = "join %s ";
    private final static String ON = "on %s.%s ";
    private final static String EQ = "= %s.%s ";
    
    private LinkedHashMap<StatementParts, String> queryParts = new LinkedHashMap<StatementParts, String>();
}
