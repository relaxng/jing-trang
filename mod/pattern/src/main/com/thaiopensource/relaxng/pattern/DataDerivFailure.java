package com.thaiopensource.relaxng.pattern;

/**
 * Provides information about why a DataDerivFunction returned notAllowed.
 */
final class DataDerivFailure {
  private int tokenStart;
  private int tokenEnd;
  private PatternMemo expected;

  DataDerivFailure() {
    reset();
  }

  void reset() {
    tokenStart = -1;
    tokenEnd = -1;
    expected = null;
  }
  
  String substring(String str) {
    return str.substring(tokenStart, tokenEnd);
  }

  int getTokenStart() {
    return tokenStart;
  }

  int getTokenEnd() {
    return tokenEnd;
  }

  PatternMemo getExpected() {
    return expected;
  }

  boolean isSet() {
    return tokenStart >= 0;
  }

  void set(int tokenStart, int tokenEnd, PatternMemo expected) {
    this.tokenStart = tokenStart;
    this.tokenEnd = tokenEnd;
    this.expected = expected;
  }

}
