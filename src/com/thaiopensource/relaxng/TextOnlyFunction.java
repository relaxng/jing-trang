package com.thaiopensource.relaxng;

class TextOnlyFunction extends EndAttributesFunction {
  TextOnlyFunction(PatternBuilder builder) {
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

