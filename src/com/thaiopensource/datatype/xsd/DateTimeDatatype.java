package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

class DateTimeDatatype extends RegexDatatype {
  static private final String DATE_PATTERN = "([1-9][0-9]*)?[0-9]{4}-[0-9]{2}-[0-9]{2}";
  static private final String TIME_PATTERN = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
  static private final String TZ_PATTERN = "(Z|[+\\-]([01][0-9]|2[0-3]):[0-5][0-9])?";
  static private final String PATTERN = "-?" + DATE_PATTERN + "T" + TIME_PATTERN + TZ_PATTERN;

  DateTimeDatatype() {
    super(PATTERN);
  }

  boolean allowsValue(String str, ValidationContext vc) {
    return getValue(str, vc) != null;
  }

  static private class DateTime {
    private Date date;
    private int leapMilliseconds;
    private boolean hasTimeZone;

    DateTime(Date date, int leapMilliseconds, boolean hasTimeZone) {
      this.date = date;
      this.leapMilliseconds = leapMilliseconds;
      this.hasTimeZone = hasTimeZone;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof DateTime))
        return false;
      DateTime other = (DateTime)obj;
      return (this.date.equals(other.date)
              && this.leapMilliseconds == other.leapMilliseconds
              && this.hasTimeZone == other.hasTimeZone);
    }

    public int hashCode() {
      return date.hashCode();
    }
  }

  // XXX What to do about leap seconds?
  // XXX Allow 24:00:00?
  Object getValue(String str, ValidationContext vc) {
    int yearStartIndex = str.charAt(0) == '-' ? 1 : 0;
    int yearEndIndex = str.indexOf('-', yearStartIndex);
    int secondFractionStartIndex = yearEndIndex + 15;
    int len = str.length();
    int timeZoneStartIndex = secondFractionStartIndex;
    for (; timeZoneStartIndex < len; timeZoneStartIndex++) {
      char c = str.charAt(timeZoneStartIndex);
      if (c == '+' || c == '-' || c == 'Z')
        break;
    }
    boolean hasTimeZone = timeZoneStartIndex < len;
    String gmtOffset;
    if (hasTimeZone && str.charAt(timeZoneStartIndex) != 'Z')
      gmtOffset = str.substring(timeZoneStartIndex);
    else
      gmtOffset = "+0";
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT" + gmtOffset));
    cal.setLenient(false);
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    cal.clear();
    cal.set(Calendar.ERA, yearStartIndex == 0 ? GregorianCalendar.AD : GregorianCalendar.BC);

    int milliseconds = 0;
    if (secondFractionStartIndex < len && str.charAt(secondFractionStartIndex) == '.') {
      for (int i = 0; i < 3; i++) {
        milliseconds *= 10;
        if (secondFractionStartIndex + i + 1 < timeZoneStartIndex)
          milliseconds += str.charAt(secondFractionStartIndex) - '0';
      }
      if (secondFractionStartIndex + 4 < timeZoneStartIndex
              && str.charAt(secondFractionStartIndex) >= '6')
        milliseconds++;
    }
    int seconds = parse2Digits(str, yearEndIndex + 13);
    int leapMilliseconds;
    if (seconds == 60) {
      leapMilliseconds = milliseconds + 1;
      milliseconds = 999;
      seconds = 59;
    }
    else
      leapMilliseconds = 0;
    try {
      cal.set(Integer.parseInt(str.substring(yearStartIndex, yearEndIndex)),  // year
              // months in Java start from 0, in ISO 8601 from 1
              parse2Digits(str, yearEndIndex + 1) - 1, // month
              parse2Digits(str, yearEndIndex + 4), // day of month
              parse2Digits(str, yearEndIndex + 7), // hour
              parse2Digits(str, yearEndIndex + 10), // minute
              seconds); // second
      cal.set(Calendar.MILLISECOND, milliseconds);
      return new DateTime(cal.getTime(), leapMilliseconds, hasTimeZone);
    }
    catch (NumberFormatException e) {
      return null;
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  static int parse2Digits(String str, int i) {
    return (str.charAt(i) - '0')*10 + (str.charAt(i + 1) - '0');
  }
}
