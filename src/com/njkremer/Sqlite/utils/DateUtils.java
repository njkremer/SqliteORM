package com.njkremer.Sqlite.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.njkremer.Sqlite.DataConnectionException;

public class DateUtils {
    
    public static Date getDateFromDatabaseFormattedString(String iso8601DateString) throws DataConnectionException {
        try {
            return DATE_FORMAT.parse(iso8601DateString);
        }
        catch (ParseException e) {
            throw new DataConnectionException(String.format("Error could not parse a date from the supplied string '%s' make sure it's in the form 'yyyy-MM-dd HH:mm:ss'", iso8601DateString));
        }
    }
    
    public static String getDatabaseFormattedStringFromDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
}
