package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

class DataDerivFunction extends AbstractPatternFunction {
  private PatternBuilder builder;
  private ValidationContext vc;
  private String str;
  private boolean blank;
  private boolean stringDependent;

  DataDerivFunction(String str, ValidationContext vc, PatternBuilder builder) {
    this.str = str;
    this.vc = vc;
    this.builder = builder;
    this.blank = isBlank(str);
  }

  static private boolean isBlank(String str) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	break;
      default:
	return false;
      }
    }
    return true;
  }

  public Object caseText(TextPattern p) {
    return p;
  }

  public Object caseList(ListPattern p) {
    stringDependent = true;
    int len = str.length();
    int tokenStart = -1;
    Pattern r = p.getOperand();
    for (int i = 0; i < len; i++) {
      switch (str.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	if (tokenStart >= 0) {
	  r = tokenDeriv(r, tokenStart, i);
	  tokenStart = -1;
	}
	break;
      default:
	if (tokenStart < 0)
	  tokenStart = i;
	break;
      }
    }
    if (tokenStart >= 0)
      r = tokenDeriv(r, tokenStart, len);
    if (r.isNullable())
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  private Pattern tokenDeriv(Pattern p, int i, int j) {
    return p.applyForPattern(new DataDerivFunction(str.substring(i, j), vc, builder));
  }

  public Object caseValue(ValuePattern p) {
    stringDependent = true;
    Datatype dt = p.getDatatype();
    Object value = dt.createValue(str, vc);
    if (value != null && dt.sameValue(p.getValue(), value))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Object caseData(DataPattern p) {
    if (p.allowsAnyString())
      return builder.makeEmpty();
    stringDependent = true;
    if (p.getDatatype().isValid(str, vc))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Object caseDataExcept(DataExceptPattern p) {
    stringDependent = true;
    if (p.getDatatype().isValid(str, vc)
	&& !p.getExcept().applyForPattern(this).isNullable())
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Object caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    if (p1.applyForPattern(this).isNullable())
      return p.getOperand2();
    if (p1.isNullable()) {
      stringDependent = true;
      if (blank)
        return p.getOperand2();
    }
    return getPatternBuilder().makeNotAllowed();
  }

  public Object caseChoice(ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().applyForPattern(this),
			      p.getOperand2().applyForPattern(this),
                              true);
  }
  
  public Object caseGroup(GroupPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    Pattern tem = builder.makeGroup(p1.applyForPattern(this), p2);
    if (!p1.isNullable())
      return tem;
    return builder.makeChoice(tem, p2.applyForPattern(this), true);
  }

  public Object caseInterleave(InterleavePattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return builder.makeChoice(builder.makeInterleave(p1.applyForPattern(this), p2),
			      builder.makeInterleave(p1, p2.applyForPattern(this)), true);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return builder.makeGroup(p.getOperand().applyForPattern(this),
			     builder.makeOptional(p));
  }

  public Object caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  PatternBuilder getPatternBuilder() {
    return builder;
  }

  boolean isStringDependent() {
    return stringDependent;
  }
}
