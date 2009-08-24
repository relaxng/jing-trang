package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;

class ValuePattern extends StringPattern {
  private final Object obj;
  private final Datatype dt;
  private final Name dtName;
  private final String stringValue;

  ValuePattern(Datatype dt, Name dtName, Object obj, String stringValue) {
    super(combineHashCode(VALUE_HASH_CODE, dt.valueHashCode(obj)));
    this.dt = dt;
    this.dtName = dtName;
    this.obj = obj;
    this.stringValue = stringValue;
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    if (!(other instanceof ValuePattern))
      return false;
    return (dt.equals(((ValuePattern)other).dt)
	    && dt.sameValue(obj, ((ValuePattern)other).obj));
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseValue(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_value");
    }
  }

  Datatype getDatatype() {
    return dt;
  }

  Name getDatatypeName() {
    return dtName;
  }

  Object getValue() {
    return obj;
  }

  String getStringValue() {
    return stringValue;
  }
}
