package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;

class DataPattern extends StringPattern {
  private Datatype dt;

  DataPattern(Datatype dt) {
    super(combineHashCode(DATA_HASH_CODE, dt.hashCode()));
    this.dt = dt;
  }

  boolean samePattern(Pattern other) {
    if (other.getClass() != this.getClass())
      return false;
    return dt.equals(((DataPattern)other).dt);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitData(dt);
  }

  Pattern apply(PatternFunction f) {
    return f.caseData(this);
  }

  Datatype getDatatype() {
    return dt;
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_data");
    }
  }
}
