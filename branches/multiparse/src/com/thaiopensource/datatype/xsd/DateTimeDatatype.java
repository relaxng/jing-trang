package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

class DateTimeDatatype extends RegexDatatype implements OrderRelation {
  static private final String YEAR_PATTERN = "-?([1-9][0-9]*)?[0-9]{4}";
  static private final String MONTH_PATTERN = "[0-9]{2}";
  static private final String DAY_OF_MONTH_PATTERN = "[0-9]{2}";
  static private final String TIME_PATTERN = "[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]*)?";
  static private final String TZ_PATTERN = "(Z|[+\\-]([01][0-9]|2[0-3]):[0-5][0-9])?";

  private final String template;

  /**
   * The argument specifies the lexical representation accepted:
   * Y specifies a year with optional preceding minus
   * M specifies a two digit month
   * D specifies a two digit day of month
   * t specifies a time (hh:mm:ss.sss)
   * any other character stands for itself.
   * All lexical representations are implicitly followed by an optional time zone.
   */
  DateTimeDatatype(String template) {
    super(makePattern(template));
    this.template = template;
  }

  static private String makePattern(String template) {
    StringBuffer pattern = new StringBuffer();
    for (int i = 0, len = template.length(); i < len; i++) {
      char c = template.charAt(i);
      switch (c) {
      case 'Y':
        pattern.append(YEAR_PATTERN);
        break;
      case 'M':
        pattern.append(MONTH_PATTERN);
        break;
      case 'D':
        pattern.append(DAY_OF_MONTH_PATTERN);
        break;
      case 't':
        pattern.append(TIME_PATTERN);
        break;
      default:
        pattern.append(c);
        break;
      }
    }
    pattern.append(TZ_PATTERN);
    return pattern.toString();
  }

  boolean allowsValue(String str, ValidationContext vc) {
    return getValue(str, vc) != null;
  }

  static private class DateTime {
    private final Date date;
    private final int leapMilliseconds;
    private final boolean hasTimeZone;

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

    Date getDate() {
      return date;
    }

    int getLeapMilliseconds() {
      return leapMilliseconds;
    }

    boolean getHasTimeZone() {
      return hasTimeZone;
    }
  }

  // XXX Check leap second validity?
  // XXX Allow 24:00:00?
  Object getValue(String str, ValidationContext vc) {
    boolean negative = false;
    int year = 2000; // any leap year will do
    int month = 1;
    int day = 1;
    int hours = 0;
    int minutes = 0;
    int seconds = 0;
    int milliseconds = 0;
    int pos = 0;
    int len = str.length();
    for (int templateIndex = 0, templateLength = template.length();
         templateIndex < templateLength;
         templateIndex++) {
      char templateChar = template.charAt(templateIndex);
      switch (templateChar) {
      case 'Y':
        negative = str.charAt(pos) == '-';
        int yearStartIndex = negative ? pos + 1 : pos;
        pos = skipDigits(str, yearStartIndex);
        try {
          year = Integer.parseInt(str.substring(yearStartIndex, pos));
        }
        catch (NumberFormatException e) {
          return null;
        }
        break;
      case 'M':
        month = parse2Digits(str, pos);
        pos += 2;
        break;
      case 'D':
        day = parse2Digits(str, pos);
        pos += 2;
        break;
      case 't':
        hours = parse2Digits(str, pos);
        pos += 3;
        minutes = parse2Digits(str, pos);
        pos += 3;
        seconds = parse2Digits(str, pos);
        pos += 2;
        if (pos < len && str.charAt(pos) == '.') {
          int end = skipDigits(str, ++pos);
          for (int j = 0; j < 3; j++) {
            milliseconds *= 10;
            if (pos < end)
              milliseconds += str.charAt(pos++) - '0';
          }
          pos = end;
        }
        break;
      default:
        pos++;
        break;
      }
    }
    boolean hasTimeZone = pos < len;
    String gmtOffset;
    if (hasTimeZone && str.charAt(pos) != 'Z')
      gmtOffset = str.substring(pos);
    else
      gmtOffset = "+0";
    int leapMilliseconds;
    if (seconds == 60) {
      leapMilliseconds = milliseconds + 1;
      milliseconds = 999;
      seconds = 59;
    }
    else
      leapMilliseconds = 0;
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT" + gmtOffset));
    cal.setLenient(false);
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    cal.clear();
    cal.set(Calendar.ERA, negative ? GregorianCalendar.BC : GregorianCalendar.AD);
    // months in ISO8601 start with 1; months in Java start with 0
    cal.set(year, month - 1, day, hours, minutes, seconds);
    cal.set(Calendar.MILLISECOND, milliseconds);
    try {
      return new DateTime(cal.getTime(), leapMilliseconds, hasTimeZone);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  static private int parse2Digits(String str, int i) {
    return (str.charAt(i) - '0')*10 + (str.charAt(i + 1) - '0');
  }

  static private int skipDigits(String str, int i) {
    for (int len = str.length(); i < len; i++) {
      if ("0123456789".indexOf(str.charAt(i)) < 0)
        break;
    }
    return i;
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  static private final int TIME_ZONE_MAX = 14*60*60*1000;

  public int compareValue(Object obj1, Object obj2) {
    DateTime dt1 = (DateTime)obj1;
    DateTime dt2 = (DateTime)obj2;
    long t1 = dt1.getDate().getTime();
    long t2 = dt2.getDate().getTime();
    if (dt1.getHasTimeZone() == dt2.getHasTimeZone())
      return compare(t1,
                     dt1.getLeapMilliseconds(),
                     t2,
                     dt2.getLeapMilliseconds());
    int res1, res2;
    if (!dt2.getHasTimeZone()) {
      res1 = compare(t1, dt1.getLeapMilliseconds(), t2 - TIME_ZONE_MAX, dt2.getLeapMilliseconds());
      res2 = compare(t1, dt1.getLeapMilliseconds(), t2 + TIME_ZONE_MAX, dt2.getLeapMilliseconds());
    }
    else {
      res1 = compare(t1 - TIME_ZONE_MAX, dt1.getLeapMilliseconds(), t2, dt2.getLeapMilliseconds());
      res2 = compare(t1 + TIME_ZONE_MAX, dt1.getLeapMilliseconds(), t2, dt2.getLeapMilliseconds());
    }
    if (res1 == res2)
      return res1;
    return COMPARE_INCOMPARABLE;
  }

  static private int compare(long t1, int leapMillis1, long t2, int leapMillis2) {
    if (t1 < t2)
      return COMPARE_LESS_THAN;
    if (t1 > t2)
      return COMPARE_GREATER_THAN;
    if (leapMillis1 < leapMillis2)
      return COMPARE_LESS_THAN;
    if (leapMillis1 > leapMillis2)
      return COMPARE_GREATER_THAN;
    return COMPARE_EQUAL;
  }
}
