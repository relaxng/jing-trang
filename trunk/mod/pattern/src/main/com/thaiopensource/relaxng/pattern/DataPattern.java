package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.datatype.Datatype2;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;

class DataPattern extends StringPattern {
  private final Datatype dt;
  private final Name dtName;

  DataPattern(Datatype dt, Name dtName) {
    super(combineHashCode(DATA_HASH_CODE, dt.hashCode()));
    this.dt = dt;
    this.dtName = dtName;
  }

  boolean samePattern(Pattern other) {
    if (other.getClass() != this.getClass())
      return false;
    return dt.equals(((DataPattern)other).dt);
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseData(this);
  }

  Datatype getDatatype() {
    return dt;
  }

  Name getDatatypeName() {
    return dtName;
  }

  boolean allowsAnyString() {
    return dt instanceof Datatype2 && ((Datatype2)dt).alwaysValid();
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_data");
    }
  }
}
