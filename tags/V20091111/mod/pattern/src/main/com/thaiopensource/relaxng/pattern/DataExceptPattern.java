package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;
import org.xml.sax.Locator;

import java.util.List;

class DataExceptPattern extends DataPattern {
  private final Pattern except;
  private final Locator loc;

  DataExceptPattern(Datatype dt, Name dtName, List<String> params, Pattern except, Locator loc) {
    super(dt, dtName, params);
    this.except = except;
    this.loc = loc;
  }

  boolean samePattern(Pattern other) {
    if (!super.samePattern(other))
      return false;
    return except.samePattern(((DataExceptPattern)other).except);
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseDataExcept(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    super.checkRestrictions(context, dad, alpha);
    try {
      except.checkRestrictions(DATA_EXCEPT_CONTEXT, null, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(loc);
      throw e;
    }
  }

  Pattern getExcept() {
    return except;
  }
}
