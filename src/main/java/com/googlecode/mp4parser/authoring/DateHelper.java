package com.googlecode.mp4parser.authoring;

import java.util.Date;

/**
 * Converts ISO Dates (seconds since 1/1/1904) to Date and vice versa.
 */
public class DateHelper {
    /**
     * Converts a long value with seconds since 1/1/1904 to Date.
     *
     * @param secondsSince seconds since 1/1/1904
     * @return date the corresponding <code>Date</code>
     */
    static public Date convert(long secondsSince) {
        return new Date((secondsSince - 2082844800L) * 1000L);
    }


    /**
     * Converts a date as long to a mac date as long
     *
     * @param date date to convert
     * @return date in mac format
     */
    static public long convert(Date date) {
        return (date.getTime() / 1000L) + 2082844800L;
    }
}
