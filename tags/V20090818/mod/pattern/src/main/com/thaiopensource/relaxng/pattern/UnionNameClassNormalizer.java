package com.thaiopensource.relaxng.pattern;

/**
 * Normalizes the union of zero or more name classes.
 */
public class UnionNameClassNormalizer extends NameClassNormalizer {
  public UnionNameClassNormalizer() {
    super(new NullNameClass());
  }

  public void add(NameClass nameClass) {
    setNameClass(new ChoiceNameClass(getNameClass(), nameClass));
  }
}
