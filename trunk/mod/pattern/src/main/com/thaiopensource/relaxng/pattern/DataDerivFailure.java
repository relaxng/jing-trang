package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;

import java.util.List;

/**
 * Provides information about why a DataDerivFunction returned notAllowed.
 */
final class DataDerivFailure {
  private final Datatype datatype;
  private final Name datatypeName;
  private final List<String> datatypeParams;
  private final String message;
  private final String stringValue;
  private final Object value;
  // except non-null means it matched the except
  private Pattern except;
  // index where error occurred if known
  private int index;
  private int tokenIndex = -1;
  private int tokenStart = -1;
  private int tokenEnd = -1;

  // not a valid instance of the datatype
  DataDerivFailure(DataPattern p, DatatypeException e) {
    this(p.getDatatype(), p.getDatatypeName(), p.getParams(), e.getMessage(), e.getIndex());
  }

  // not a valid instance of the datatype
  DataDerivFailure(Datatype dt, Name dtName, DatatypeException e) {
    this(dt, dtName, null, e.getMessage(), e.getIndex());
  }
  // failed because it matched the except in a dataExcept
  DataDerivFailure(DataExceptPattern p) {
    this(p.getDatatype(), p.getDatatypeName(), p.getParams(), p.getExcept());
  }

  // not equal to the value in a value pattern
  DataDerivFailure(ValuePattern p) {
    this(p.getDatatype(), p.getDatatypeName(), p.getValue(), p.getStringValue());
  }

  private DataDerivFailure(Datatype datatype, Name datatypeName, List<String> datatypeParams, String message, int index) {
    this.datatype = datatype;
    this.datatypeName = datatypeName;
    this.datatypeParams = datatypeParams;
    this.message = message;
    this.except = null;
    this.index = index == DatatypeException.UNKNOWN ? -1 : index;
    this.stringValue = null;
    this.value = null;
  }

  private DataDerivFailure(Datatype datatype, Name datatypeName, List<String> datatypeParams, Pattern except) {
    this.datatype = datatype;
    this.datatypeName = datatypeName;
    this.datatypeParams = datatypeParams;
    this.message = null;
    this.except = except;
    this.index = -1;
    this.stringValue = null;
    this.value = null;
  }

  private DataDerivFailure(Datatype datatype, Name datatypeName, Object value, String stringValue) {
    this.datatype = datatype;
    this.datatypeName = datatypeName;
    this.datatypeParams = null;
    this.message = null;
    this.except = null;
    this.index = -1;
    this.stringValue = stringValue;
    this.value = value;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof DataDerivFailure))
      return false;
    DataDerivFailure other = (DataDerivFailure)obj;
    return (datatype == other.datatype
            && equal(message, other.message)
            && equal(stringValue, other.stringValue)
            && except == other.except
            && tokenIndex == other.tokenIndex
            && index == other.index);
  }

  public int hashCode() {
    int hc = datatype.hashCode();
    if (message != null)
      hc ^= message.hashCode();
    if (stringValue != null)
      hc ^= stringValue.hashCode();
    if (except != null)
      hc ^= except.hashCode();
    return hc;
  }

  private static boolean equal(Object o1, Object o2) {
    if (o1 == null)
      return o2 == null;
    return o1.equals(o2);
  }

  Datatype getDatatype() {
    return datatype;
  }

  Name getDatatypeName() {
    return datatypeName;
  }

  List<String> getDatatypeParams() {
    return datatypeParams;
  }

  String getMessage() {
    return message;
  }

  String getStringValue() {
    return stringValue;
  }

  Object getValue() {
    return value;
  }

  Pattern getExcept() {
    return except;
  }

  int getIndex() {
    return index;
  }

  int getTokenIndex() {
    return tokenIndex;
  }

  int getTokenStart() {
    return tokenStart;
  }

  int getTokenEnd() {
    return tokenEnd;
  }

  void setToken(int tokenIndex, int tokenStart, int tokenEnd) {
    this.tokenIndex = tokenIndex;
    this.tokenStart = tokenStart;
    this.tokenEnd = tokenEnd;
    if (index < 0)
      index += tokenStart;
  }

}
