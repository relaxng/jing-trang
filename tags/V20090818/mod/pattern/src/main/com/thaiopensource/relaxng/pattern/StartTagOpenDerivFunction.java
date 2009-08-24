package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

class StartTagOpenDerivFunction extends AbstractPatternFunction<Pattern> {
  private final Name name;
  private final ValidatorPatternBuilder builder;

  StartTagOpenDerivFunction(Name name, ValidatorPatternBuilder builder) {
    this.name = name;
    this.builder = builder;
  }

  public Pattern caseChoice(ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }

  public Pattern caseGroup(GroupPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    Pattern tem = memoApply(p1).apply(new ApplyAfterFunction(builder) {
      Pattern apply(Pattern x) {
        return builder.makeGroup(x, p2);
      }
    });
    return p1.isNullable() ? builder.makeChoice(tem, memoApply(p2)) : tem;
  }

  public Pattern caseInterleave(InterleavePattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return builder.makeChoice(
            memoApply(p1).apply(new ApplyAfterFunction(builder) {
              Pattern apply(Pattern x) {
                return builder.makeInterleave(x, p2);
              }
            }),
            memoApply(p2).apply(new ApplyAfterFunction(builder) {
              Pattern apply(Pattern x) {
                return builder.makeInterleave(p1, x);
              }
            }));
  }

  public Pattern caseAfter(AfterPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    return memoApply(p1).apply(new ApplyAfterFunction(builder) {
				   Pattern apply(Pattern x) {
				     return builder.makeAfter(x, p2);
				   }
				 });
  }

  public Pattern caseOneOrMore(final OneOrMorePattern p) {
    final Pattern p1 = p.getOperand();
    return memoApply(p1).apply(new ApplyAfterFunction(builder) {
				   Pattern apply(Pattern x) {
				     return builder.makeGroup(x,
							      builder.makeOptional(p));
				   }
				 });
  }


  public Pattern caseElement(ElementPattern p) {
    if (!p.getNameClass().contains(name))
      return builder.makeNotAllowed();
    return builder.makeAfter(p.getContent(), builder.makeEmpty());
  }

  public Pattern caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  final Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.startTagOpenDeriv(this);
  }

  Name getName() {
    return name;
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }
}
