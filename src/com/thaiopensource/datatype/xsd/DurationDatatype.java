package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

import java.math.BigInteger;
import java.math.BigDecimal;

class DurationDatatype extends RegexDatatype {
  static private final BigInteger ZERO = new BigInteger("0");
  static private final BigDecimal ZERO_DOT_ZERO = new BigDecimal("0");

  static private final String PATTERN =
    "-?P([0-9]+Y)?([0-9]+M)?([0-9]+D)?(T([0-9]+H)?([0-9]+M)?(([0-9]+(\\.[0-9]*)?|\\.[0-9]+)S)?)?";
  DurationDatatype() {
    super(PATTERN);
  }

  public boolean lexicallyAllows(String str) {
    if (!super.lexicallyAllows(str))
      return false;
    char last = str.charAt(str.length()-1);
    // This enforces that there must be at least one component
    // and that T is omitted if all time components are omitted
    return last != 'P' && last != 'T';
  }

  static private class Duration {
    private final BigInteger years;
    private final BigInteger months;
    private final BigInteger days;
    private final BigInteger hours;
    private final BigInteger minutes;
    private final BigDecimal seconds;

    Duration(BigInteger years, BigInteger months, BigInteger days,
             BigInteger hours, BigInteger minutes, BigDecimal seconds) {
      this.years = years;
      this.months = months;
      this.days = days;
      this.hours = hours;
      this.minutes = minutes;
      this.seconds = seconds;
    }

    BigInteger getYears() {
      return years;
    }

    BigInteger getMonths() {
      return months;
    }

    BigInteger getDays() {
      return days;
    }

    BigInteger getHours() {
      return hours;
    }

    BigInteger getMinutes() {
      return minutes;
    }

    BigDecimal getSeconds() {
      return seconds;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof Duration))
        return false;
      Duration other = (Duration)obj;
      return (this.years.equals(other.years)
              && this.months.equals(other.months)
              && this.days.equals(other.days)
              && this.hours.equals(other.hours)
              && this.minutes.equals(other.minutes)
              && this.seconds.compareTo(other.seconds) == 0);

    }
  }

  Object getValue(String str, ValidationContext vc) {
    int t = str.indexOf('T');
    if (t < 0)
      t = str.length();
    String date = str.substring(0, t);
    String time = str.substring(t);
    return new Duration(getIntegerField(date, 'Y'),
                        getIntegerField(date, 'M'),
                        getIntegerField(date, 'D'),
                        getIntegerField(time, 'H'),
                        getIntegerField(time, 'M'),
                        getDecimalField(time, 'S'));

  }

  static private BigInteger getIntegerField(String str, char code) {
    int end = str.indexOf(code);
    if (end < 0)
      return ZERO;
    int start = end;
    while (Character.isDigit(str.charAt(start - 1)))
      --start;
    return new BigInteger(str.substring(start, end));
  }

  static private BigDecimal getDecimalField(String str, char code) {
    int end = str.indexOf(code);
    if (end < 0)
      return ZERO_DOT_ZERO;
    int start = end;
    while (!Character.isLetter(str.charAt(start - 1)))
      --start;
    return new BigDecimal(str.substring(start, end));
  }

}
