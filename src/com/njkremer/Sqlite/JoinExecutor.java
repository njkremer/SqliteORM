package com.njkremer.Sqlite;

public class JoinExecutor {
    
    /**
     * User to represent various join types for a database.
     */
    public enum JoinType {
        INNER("inner "), 
        LEFT_OUTER("left outer "), 
        RIGHT_OUTER("right outer ");
        
        JoinType(String sql) {
            this.sql = sql;
        }
        
        public String getSql() {
            return sql;
        }

        private String sql;
    }

    public JoinExecutor join(Class<?> leftClazz, String leftField, Class<?> rightClazz, String rightField) {
        String rightClassName = rightClazz.getSimpleName().toLowerCase();
        String leftClassName = leftClazz.getSimpleName().toLowerCase();
        String joinTypeSql = "";
        if (joinType != null) {
            joinTypeSql = this.joinType.getSql();
        }
        query = String.format(JOIN, joinTypeSql, leftClassName, leftClassName, leftField, rightClassName, rightField);
        return this;
    }
    
    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    String getQuery() {
        return query;
    }

    private final static String JOIN = "%sjoin %s on %s.%s = %s.%s ";
    private JoinType joinType = null;
    private String query;
}
