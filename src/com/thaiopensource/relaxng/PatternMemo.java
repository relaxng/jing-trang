package com.thaiopensource.relaxng;

import java.util.Hashtable;

import org.relaxng.datatype.ValidationContext;

/* XXX When caching fails look for an After pattern with same first operand. */

final class PatternMemo {
  private final Pattern pattern;
  private final PatternBuilder builder;
  private final boolean notAllowed;
  private PatternMemo memoEndAttributes;
  private PatternMemo memoTextOnly;
  private PatternMemo memoEndTagDeriv;
  private PatternMemo memoMixedTextDeriv;
  private PatternMemo memoIgnoreMissingAttributes;
  private Hashtable startTagOpenDerivMap;
  private Hashtable startAttributeDerivMap;

  PatternMemo(Pattern pattern, PatternBuilder builder) {
    this.pattern = pattern;
    this.builder = builder;
    this.notAllowed = pattern.isNotAllowed();
  }

  Pattern getPattern() {
    return pattern;
  }

  PatternBuilder getPatternBuilder() {
    return builder;
  }

  boolean isNotAllowed() {
    return notAllowed;
  }

  PatternMemo endAttributes() {
    if (memoEndAttributes == null)
      memoEndAttributes = apply(builder.getEndAttributesFunction());
    return memoEndAttributes;
  }

  PatternMemo endAttributes(PatternFunction f) {
    if (memoEndAttributes == null)
      memoEndAttributes = apply(f);
    return memoEndAttributes;
  }

  PatternMemo ignoreMissingAttributes() {
    if (memoIgnoreMissingAttributes == null)
      memoIgnoreMissingAttributes
	= apply(builder.getIgnoreMissingAttributesFunction());
    return memoIgnoreMissingAttributes;
  }

  PatternMemo ignoreMissingAttributes(PatternFunction f) {
    if (memoIgnoreMissingAttributes == null)
      memoIgnoreMissingAttributes = apply(f);
    return memoIgnoreMissingAttributes;
  }


  PatternMemo textOnly() {
    if (memoTextOnly == null)
      memoTextOnly = apply(builder.getTextOnlyFunction());
    return memoTextOnly;
  }

  PatternMemo textOnly(PatternFunction f) {
    if (memoTextOnly == null)
      memoTextOnly = apply(f);
    return memoTextOnly;
  }

  PatternMemo endTagDeriv() {
    if (memoEndTagDeriv == null)
      memoEndTagDeriv = apply(builder.getEndTagDerivFunction());
    return memoEndTagDeriv;
  }

  PatternMemo endTagDeriv(PatternFunction f) {
    if (memoEndTagDeriv == null)
      memoEndTagDeriv = apply(f);
    return memoEndTagDeriv;
  }


  PatternMemo mixedTextDeriv() {
    if (memoMixedTextDeriv == null)
      memoMixedTextDeriv = apply(builder.getMixedTextDerivFunction());
    return memoMixedTextDeriv;
  }

  PatternMemo mixedTextDeriv(PatternFunction f) {
    if (memoMixedTextDeriv == null)
      memoMixedTextDeriv = apply(f);
    return memoMixedTextDeriv;
  }

  PatternMemo startTagOpenDeriv(Name name) {
    return startTagOpenDeriv(name, null);
  }

  PatternMemo startTagOpenDeriv(StartTagOpenDerivFunction f) {
    return startTagOpenDeriv(f.getName(), f);
  }

  private PatternMemo startTagOpenDeriv(Name name, StartTagOpenDerivFunction f) {
    PatternMemo tem;
    if (startTagOpenDerivMap == null)
      startTagOpenDerivMap = new Hashtable();
    else {
      tem = (PatternMemo)startTagOpenDerivMap.get(name);
      if (tem != null)
	return tem;
    }
    if (f == null)
      f = new StartTagOpenDerivFunction(name, builder);
    tem = apply(f);
    startTagOpenDerivMap.put(name, tem);
    return tem;
  }

  PatternMemo startAttributeDeriv(Name name) {
    return startAttributeDeriv(name, null);
  }

  PatternMemo startAttributeDeriv(StartAttributeDerivFunction f) {
    return startAttributeDeriv(f.getName(), f);
  }

  private PatternMemo startAttributeDeriv(Name name, StartAttributeDerivFunction f) {
    PatternMemo tem;
    if (startAttributeDerivMap == null)
      startAttributeDerivMap = new Hashtable();
    else {
      tem = (PatternMemo)startAttributeDerivMap.get(name);
      if (tem != null)
	return tem;
    }
    if (f == null)
      f = new StartAttributeDerivFunction(name, builder);
    tem = apply(f);
    startAttributeDerivMap.put(name, tem);
    return tem;
  }

  PatternMemo dataDeriv(String str, ValidationContext vc) {
    // XXX cache it (at least if it doesn't use the string value)
    return apply(new DataDerivFunction(str, vc, builder));
  }

  PatternMemo recoverAfter() {
    // XXX memoize
    return apply(builder.getRecoverAfterFunction());
  }

  private PatternMemo apply(PatternFunction f) {
    return builder.getPatternMemo(pattern.apply(f));
  }
}
