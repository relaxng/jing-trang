package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.xsd.regex.Regex;
import org.relaxng.datatype.DatatypeException;

class PatternRestrictDatatype extends RestrictDatatype {
  private final Regex pattern;
  private final String patternString;

  PatternRestrictDatatype(DatatypeBase base, Regex pattern, String patternString) {
    super(base);
    this.pattern = pattern;
    this.patternString = patternString;
  }

  boolean lexicallyAllows(String str) {
    return pattern.matches(str) && super.lexicallyAllows(str);
  }

  void checkLexicallyAllows(String str) throws DatatypeException {
    super.checkLexicallyAllows(str);
    if (!pattern.matches(str))
      throw new DatatypeException(localizer().message("pattern_violation",
                                                      getDescriptionForRestriction(),
                                                      patternString));
  }
}
