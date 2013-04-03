package com.njkremer.Sqlite;



/**
 * Used to specify an equality in a where statement. This is used in conjunction with
 * {@linkplain SqlExecutor#where(String)} and {@linkplain SqlExecutor#and(String)}.
 */
public class WhereExecutor<T> {

    /**
     * Used to specify a equal comparison with the supplied value and the preceding field as specified with a
     * {@linkplain SqlExecutor#where(String) where} or {@linkplain SqlExecutor#and(String) and}.
     * 
     * @param value The value for the right hand side of the equality statement.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> eq(Object value) {
        return _appendToWhere(EQUALS, value);
    }

    /**
     * Used to specify a greater than comparison with the supplied value and the preceding field as specified
     * with a {@linkplain SqlExecutor#where(String) where} or {@linkplain SqlExecutor#and(String) and}.
     * 
     * @param value The value for the right hand side of the greater than statement.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> greaterThan(Object value) {
        return _appendToWhere(GREATER_THAN, value);
    }

    /**
     * Used to specify a greater than or equal to comparison with the supplied value and the preceding field as
     * specified with a {@linkplain SqlExecutor#where(String) where} or {@linkplain SqlExecutor#and(String)
     * and}.
     * 
     * @param value The value for the right hand side of the greater than or equal to statement.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> greaterThanOrEq(Object value) {
        return _appendToWhere(GREATER_THAN_EQUAL, value);
    }

    /**
     * Used to specify a less than comparison with the supplied value and the preceding field as specified with
     * a {@linkplain SqlExecutor#where(String) where} or {@linkplain SqlExecutor#and(String) and}.
     * 
     * @param value The value for the right hand side of the less than statement.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> lessThan(Object value) {
        return _appendToWhere(LESS_THAN, value);
    }

    /**
     * Used to specify a less than or equal to comparison with the supplied value and the preceding field as
     * specified with a {@linkplain SqlExecutor#where(String) where} or {@linkplain SqlExecutor#and(String)
     * and}.
     * 
     * @param value The value for the right hand side of the less than or equal to statement.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> lessThanOrEq(Object value) {
        return _appendToWhere(LESS_THAN_EQUAL, value);
    }

    /**
     * Used to specify a like than comparison with the supplied value and the preceding field as specified with
     * a {@linkplain SqlExecutor#where(String) where} or {@linkplain SqlExecutor#and(String) and}.
     * 
     * <p>A like statement uses a % as the wild card and needs to be specified in the value string passed in.
     * 
     * @param value The value for the right hand side of the like statement.
     * @return A {@linkplain SqlExecutor} used for function chaining.
     */
    public SqlExecutor<T> like(String value) {
        return _appendToWhere(LIKE, value);
    }   
    
    private SqlExecutor<T> _appendToWhere(String appendString, Object value) {
        sqlExecutor.getQueryParts().put(StatementParts.WHERE, sqlExecutor.getQueryParts().get(StatementParts.WHERE).concat(appendString));
        sqlExecutor.getValues().add(value);
        return sqlExecutor;
    }

    WhereExecutor(SqlExecutor<T> sqlE) {
        sqlExecutor = sqlE;
    }

    private SqlExecutor<T> sqlExecutor;
    private static final String EQUALS = "= ? ";
    private static final String GREATER_THAN = "> ? ";
    private static final String LESS_THAN = "< ? ";
    private static final String GREATER_THAN_EQUAL = ">= ? ";
    private static final String LESS_THAN_EQUAL = "<= ? ";
    private static final String LIKE = "like ? ";

}