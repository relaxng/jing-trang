package com.thaiopensource.relaxng;

class IgnoreMissingAttributesFunction extends EndAttributesFunction {
  IgnoreMissingAttributesFunction(PatternBuilder builder) {
    super(builder);
  }

  public Object caseAttribute(AttributePattern p) {
    return getPatternBuilder().makeEmpty();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.ignoreMissingAttributes(this);
  }
}
