package com.thaiopensource.relaxng.edit;

public class AbstractPatternVisitor implements PatternVisitor {
  public Object visitElement(ElementPattern p) {
    return visitNameClassed(p);
  }

  public Object visitAttribute(AttributePattern p) {
    return visitNameClassed(p);
  }

  public Object visitOneOrMore(OneOrMorePattern p) {
    return visitUnary(p);
  }

  public Object visitZeroOrMore(ZeroOrMorePattern p) {
    return visitUnary(p);
  }

  public Object visitOptional(OptionalPattern p) {
    return visitUnary(p);
  }

  public Object visitInterleave(InterleavePattern p) {
    return visitComposite(p);
  }

  public Object visitGroup(GroupPattern p) {
    return visitComposite(p);
  }

  public Object visitChoice(ChoicePattern p) {
    return visitComposite(p);
  }

  public Object visitGrammar(GrammarPattern p) {
    return visitPattern(p);
  }

  public Object visitExternalRef(ExternalRefPattern p) {
    return visitPattern(p);
  }

  public Object visitRef(RefPattern p) {
    return visitRef(p);
  }

  public Object visitParentRef(ParentRefPattern p) {
    return visitParentRef(p);
  }

  public Object visitValue(ValuePattern p) {
    return visitValue(p);
  }

  public Object visitData(DataPattern p) {
    return visitData(p);
  }

  public Object visitMixed(MixedPattern p) {
    return visitMixed(p);
  }

  public Object visitList(ListPattern p) {
    return visitList(p);
  }

  public Object visitText(TextPattern p) {
    return visitText(p);
  }

  public Object visitEmpty(EmptyPattern p) {
    return visitEmpty(p);
  }

  public Object visitNotAllowed(NotAllowedPattern p) {
    return visitNotAllowed(p);
  }

  public Object visitNameClassed(NameClassedPattern p) {
    return visitUnary(p);
  }

  public Object visitUnary(UnaryPattern p) {
    return visitPattern(p);
  }

  public Object visitComposite(CompositePattern p) {
    return visitPattern(p);
  }

  public Object visitPattern(Pattern p) {
    return null;
  }
}
