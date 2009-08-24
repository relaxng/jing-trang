package com.thaiopensource.relaxng.pattern;

class TextOnlyFunction extends EndAttributesFunction {
  TextOnlyFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }
  public Pattern caseAttribute(AttributePattern p) {
    return p;
  }
  public Pattern caseElement(ElementPattern p) {
    return getPatternBuilder().makeNotAllowed();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.textOnly(this);
  }

}

