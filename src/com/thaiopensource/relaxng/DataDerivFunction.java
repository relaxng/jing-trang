package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

class DataDerivFunction extends AbstractPatternFunction {
  private PatternBuilder builder;
  private ValidationContext vc;
  private String str;
  private boolean blank;

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

  public Pattern caseText(TextPattern p) {
    return p;
  }

  public Pattern caseList(ListPattern p) {
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
    return p.apply(new DataDerivFunction(str.substring(i, j), vc, builder));
  }

  public Pattern caseValue(ValuePattern p) {
    Datatype dt = p.getDatatype();
    Object value = dt.createValue(str, vc);
    if (value != null && dt.sameValue(p.getValue(), value))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Pattern caseData(DataPattern p) {
    if (p.getDatatype().isValid(str, vc))
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Pattern caseDataExcept(DataExceptPattern p) {
    if (p.getDatatype().isValid(str, vc)
	&& !p.getExcept().apply(this).isNullable())
      return builder.makeEmpty();
    else
      return builder.makeNotAllowed();
  }

  public Pattern caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    if (p1.apply(this).isNullable() || (blank && p1.isNullable()))
      return p.getOperand2();
    else
      return getPatternBuilder().makeNotAllowed();
  }

  public Pattern caseChoice(ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().apply(this),
			      p.getOperand2().apply(this));
  }
  
  public Pattern caseGroup(GroupPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    Pattern tem = builder.makeGroup(p1.apply(this), p2);
    if (!p1.isNullable())
      return tem;
    return builder.makeChoice(tem, p2.apply(this));
  }

  public Pattern caseInterleave(InterleavePattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return builder.makeChoice(builder.makeInterleave(p1.apply(this), p2),
			      builder.makeInterleave(p1, p2.apply(this)));
  }

  public Pattern caseOneOrMore(OneOrMorePattern p) {
    return builder.makeGroup(p.getOperand().apply(this),
			     builder.makeOptional(p));
  }

  public Pattern caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  PatternBuilder getPatternBuilder() {
    return builder;
  }
}
