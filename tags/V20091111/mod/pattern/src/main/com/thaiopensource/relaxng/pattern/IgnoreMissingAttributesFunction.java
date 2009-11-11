package com.thaiopensource.relaxng.pattern;

class IgnoreMissingAttributesFunction extends EndAttributesFunction {
  IgnoreMissingAttributesFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }

  public Pattern caseAttribute(AttributePattern p) {
    return getPatternBuilder().makeEmpty();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.ignoreMissingAttributes(this);
  }
}
