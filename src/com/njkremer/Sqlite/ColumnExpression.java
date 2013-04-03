package com.njkremer.Sqlite;

import java.util.LinkedHashMap;

/**
 * A expression to allow for custom columns and functions to be called as part of a "select" portion of a sql
 * statment.
 * 
 * <p> The idea here is to be able to return arbitrary columns and to operate on columns (such as SQLite's <a
 * href="http://www.sqlite.org/lang_datefunc.html">Date/Time Functions</a>). This class is used in conjunction with
 * {@linkplain SqlExecutor#getColumns(ColumnExpression)} to return a list of maps. Each entry in the list is a map of the
 * specified column/field name to it's value.
 * 
 * <p> For example if my sql statement was returning something like:
 * 
 * <p>
 * <table border=1>
 *  <tr><th>name</th><th>age</th></tr>
 *  <tr><td>John</td><td>21</td></tr>
 *  <tr><td>Mary</td><td>31</td></tr>
 * </table>
 * 
 * <p>A call to the resulting <code>List&lt;Map&lt;String, Object&gt;&gt;</code> that looked like
 * <code>list.get(1).get("name")</code> would return "Mary".
 * 
 * <p>Since this is used in conjunction with {@link SqlExecutor}/{@link SqlStatement} there still needs to be a
 * POJO that "maps" to a database table. This just lets you get a non-Object from the database and perform
 * functions on a column.
 */
public class ColumnExpression {
    public ColumnExpression() {
        queryParts.put(StatementParts.SELECT, SELECT);
        queryParts.put(StatementParts.COLUMN, "");
    }

    /**
     * Specify the column to limit you're resulting query by. A function (such as SQLite's <a
     * href="http://www.sqlite.org/lang_datefunc.html">Date/Time Functions</a>) can be used in the passed in string
     * too.
     * 
     * @param columnString The column to query.
     * @return A {@link ColumnExpression} for function chaining.
     */
    public ColumnExpression column(String columnString) {
        queryParts.put(StatementParts.COLUMN, queryParts.get(StatementParts.COLUMN).concat(columnString + ", "));
        return this;
    }

    /**
     * Lets you alias a column if you want. This can be useful when using a function in the
     * {@linkplain #column(String)}.
     * 
     * @param alias The name of the alias for the column.
     * @return A {@link ColumnExpression} for function chaining.
     */
    public ColumnExpression as(String alias) {
        String columns = queryParts.get(StatementParts.COLUMN);
        columns = columns.substring(0, columns.lastIndexOf(",")).concat(" ");
        queryParts.put(StatementParts.COLUMN, columns.concat(String.format(AS, alias)));
        return this;
    }

    /**
     * Allows you to specify making a query distinc, which will eliminate duplicate rows in the result set.
     * 
     * @return A {@link ColumnExpression} for function chaining.
     */
    public ColumnExpression distinct() {
        queryParts.put(StatementParts.SELECT, SELECT + DISTINCT);
        return this;
    }

    /**
     * @return Returns the Sql Select String so far for the {@linkplain ColumnExpression}.
     */
    String getQuery() {
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
