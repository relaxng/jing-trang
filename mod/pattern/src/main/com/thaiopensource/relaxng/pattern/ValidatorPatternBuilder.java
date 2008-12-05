package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidatorPatternBuilder extends PatternBuilder {
  private final Map<Pattern, PatternMemo> patternMemoMap = new HashMap<Pattern, PatternMemo>();
  private final PatternFunction<Pattern> endAttributesFunction;
  private final PatternFunction<Pattern> ignoreMissingAttributesFunction;
  private final PatternFunction<Pattern> endTagDerivFunction;
  private final PatternFunction<Pattern> mixedTextDerivFunction;
  private final PatternFunction<Pattern> textOnlyFunction;
  private final PatternFunction<Pattern> recoverAfterFunction;
  private final PatternFunction<DataDerivType> dataDerivTypeFunction;

  private final Map<Pattern, Pattern> choiceMap = new HashMap<Pattern, Pattern>();
  private final PatternFunction<Pattern> removeChoicesFunction = new RemoveChoicesFunction();
  private final PatternFunction<VoidValue> noteChoicesFunction = new NoteChoicesFunction();
  private final PatternFunction<Set<Name>> requiredElementsFunction = new RequiredElementsFunction();
  private final PatternFunction<Set<Name>> requiredAttributesFunction = new RequiredAttributesFunction();
  private final PossibleNamesFunction possibleStartTagNamesFunction = new PossibleStartTagNamesFunction();
  private final PossibleNamesFunction possibleAttributeNamesFunction = new PossibleAttributeNamesFunction();

  private class NoteChoicesFunction extends AbstractPatternFunction<VoidValue> {
    public VoidValue caseOther(Pattern p) {
      choiceMap.put(p, p);
      return VoidValue.VOID;
    }

    public VoidValue caseChoice(ChoicePattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return VoidValue.VOID;
    }
  }

  private class RemoveChoicesFunction extends AbstractPatternFunction<Pattern> {
    public Pattern caseOther(Pattern p) {
      if (choiceMap.get(p) != null)
        return notAllowed;
      return p;
    }

    public Pattern caseChoice(ChoicePattern p) {
      Pattern p1 = p.getOperand1().apply(this);
      Pattern p2 = p.getOperand2().apply(this);
      if (p1 == p.getOperand1() && p2 == p.getOperand2())
        return p;
      if (p1 == notAllowed)
        return p2;
      if (p2 == notAllowed)
        return p1;
      Pattern p3 = new ChoicePattern(p1, p2);
      return interner.intern(p3);
    }
  }

  public ValidatorPatternBuilder(PatternBuilder builder) {
    super(builder);
    endAttributesFunction = new EndAttributesFunction(this);
    ignoreMissingAttributesFunction = new IgnoreMissingAttributesFunction(this);
    endTagDerivFunction = new EndTagDerivFunction(this);
    mixedTextDerivFunction = new MixedTextDerivFunction(this);
    textOnlyFunction = new TextOnlyFunction(this);
    recoverAfterFunction = new RecoverAfterFunction(this);
    dataDerivTypeFunction = new DataDerivTypeFunction(this);
  }

  PatternMemo getPatternMemo(Pattern p) {
    PatternMemo memo = patternMemoMap.get(p);
    if (memo == null) {
      memo = new PatternMemo(p, this);
      patternMemoMap.put(p, memo);
    }
    return memo;
  }

  PatternFunction<Pattern> getEndAttributesFunction() {
    return endAttributesFunction;
  }

  PatternFunction<Pattern> getIgnoreMissingAttributesFunction() {
    return ignoreMissingAttributesFunction;
  }

  PatternFunction<Set<Name>> getRequiredElementsFunction() {
    return requiredElementsFunction;
  }

  PatternFunction<Set<Name>> getRequiredAttributesFunction() {
    return requiredAttributesFunction;
  }

  PossibleNamesFunction getPossibleStartTagNamesFunction() {
    return possibleStartTagNamesFunction;
  }

  PossibleNamesFunction getPossibleAttributeNamesFunction() {
    return possibleAttributeNamesFunction;
  }

  PatternFunction<Pattern> getEndTagDerivFunction() {
    return endTagDerivFunction;
  }

  PatternFunction<Pattern> getMixedTextDerivFunction() {
    return mixedTextDerivFunction;
  }

  PatternFunction<Pattern> getTextOnlyFunction() {
    return textOnlyFunction;
  }

  PatternFunction<Pattern> getRecoverAfterFunction() {
    return recoverAfterFunction;
  }

  PatternFunction<DataDerivType> getDataDerivTypeFunction() {
    return dataDerivTypeFunction;
  }

  Pattern makeAfter(Pattern p1, Pattern p2) {
    Pattern p = new AfterPattern(p1, p2);
    return interner.intern(p);
  }

  Pattern makeChoice(Pattern p1, Pattern p2) {
    if (p1 == p2)
      return p1;
    if (p1 == notAllowed)
      return p2;
    if (p2 == notAllowed)
      return p1;
    if (!(p1 instanceof ChoicePattern)) {
      if (p2.containsChoice(p1))
        return p2;
    }
    else if (!(p2 instanceof ChoicePattern)) {
      if (p1.containsChoice(p2))
        return p1;
    }
    else {
      p1.apply(noteChoicesFunction);
      p2 = p2.apply(removeChoicesFunction);
      if (choiceMap.size() > 0)
        choiceMap.clear();
    }
    if (p1 instanceof AfterPattern && p2 instanceof AfterPattern) {
      AfterPattern ap1 = (AfterPattern)p1;
      AfterPattern ap2 = (AfterPattern)p2;
      if (ap1.getOperand1() == ap2.getOperand1())
        return makeAfter(ap1.getOperand1(), makeChoice(ap1.getOperand2(), ap2.getOperand2()));
      if (ap1.getOperand1() == notAllowed)
        return ap2;
      if (ap2.getOperand1() == notAllowed)
        return ap1;
      if (ap1.getOperand2() == ap2.getOperand2())
        return makeAfter(makeChoice(ap1.getOperand1(), ap2.getOperand1()), ap1.getOperand2());
    }
    return super.makeChoice(p1, p2);
  }
}
