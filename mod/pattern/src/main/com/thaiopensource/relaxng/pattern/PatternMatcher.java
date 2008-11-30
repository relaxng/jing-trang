package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.util.Equal;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.ValidationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

public class PatternMatcher implements Cloneable, Matcher {

  static private class Shared {
    private final Pattern start;
    private final ValidatorPatternBuilder builder;
    private Map recoverPatternTable;
    Shared(Pattern start, ValidatorPatternBuilder builder) {
      this.start = start;
      this.builder = builder;
    }

    Pattern findElement(Name name) {
      if (recoverPatternTable == null)
        recoverPatternTable = new HashMap();
      Pattern p = (Pattern)recoverPatternTable.get(name);
      if (p == null) {
        p = FindElementFunction.findElement(builder, name, start);
        recoverPatternTable.put(name, p);
      }
      return p;
    }

    PatternMemo fixAfter(PatternMemo p) {
      return builder.getPatternMemo(p.getPattern().applyForPattern(new ApplyAfterFunction(builder) {
        Pattern apply(Pattern p) {
          return builder.makeEmpty();
        }
      }));
    }
  }

  private PatternMemo memo;
  private boolean textTyped;
  private boolean hadError;
  private boolean ignoreNextEndTag;
  private String errorMessage;
  private final Shared shared;

  public PatternMatcher(Pattern start, ValidatorPatternBuilder builder) {
    shared = new Shared(start, builder);
    memo = builder.getPatternMemo(start);
  }

  public boolean equals(Object obj) {
    PatternMatcher other = (PatternMatcher)obj;
    if (other == null)
      return false;
    // don't need to test equality of shared, because the memos can only be ==
    // if the shareds are ==.
    return (memo == other.memo
            && hadError == other.hadError
            && Equal.equal(errorMessage, other.errorMessage)
            && ignoreNextEndTag == other.ignoreNextEndTag
            && textTyped == other.textTyped);
  }

  public int hashCode() {
    return memo.hashCode();
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new Error("unexpected CloneNotSupportedException");
    }
  }

  public Matcher copy() {
    return (Matcher)clone();
  }

  public boolean matchStartDocument() {
    if (memo.isNotAllowed())
      return error("schema_allows_nothing");
    return true;
  }

  public boolean matchEndDocument() {
    // XXX maybe check that memo.isNullable if !hadError
    return true;
  }

  public boolean matchStartTagOpen(Name name) {
    if (setMemo(memo.startTagOpenDeriv(name)))
      return true;
    PatternMemo next = memo.startTagOpenRecoverDeriv(name);
    if (!next.isNotAllowed()) {
      boolean ok = error("required_elements_missing");
      memo = next;
      return ok;
    }
    ValidatorPatternBuilder builder = shared.builder;
    next = builder.getPatternMemo(builder.makeAfter(shared.findElement(name), memo.getPattern()));
    boolean ok = error(next.isNotAllowed() ? "unknown_element" : "out_of_context_element", name);
    memo = next;
    return ok;
  }

  public boolean matchAttributeName(Name name) {
    if (setMemo(memo.startAttributeDeriv(name)))
      return true;
    ignoreNextEndTag = true;
    return error("impossible_attribute_ignored", name);
  }

  public boolean matchAttributeValue(Name name, String value, ValidationContext vc) {
    if (ignoreNextEndTag) {
      ignoreNextEndTag = false;
      return true;
    }
    if (setMemo(memo.dataDeriv(value, vc)))
      return true;
    boolean ok = error("bad_attribute_value", name);
    memo = memo.recoverAfter();
    return ok;
  }

  public boolean matchStartTagClose() {
    boolean ok;
    if (setMemo(memo.endAttributes()))
      ok = true;
    else {
      ok = ignoreError();
      if (!ok)
        reportMissingAttributes();
      memo = memo.ignoreMissingAttributes();
    }
    textTyped = memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE;
    return ok;
  }

  public void reportMissingAttributes() {
    List missing = new ArrayList(requiredAttributeNames());
    if (missing.isEmpty())
      // XXX Can we do better here? This is probably not very common in practice.
      error("required_attributes_missing");
    else if (missing.size() == 1)
      error("required_attribute_missing", NameFormatter.format((Name)missing.get(0)));
    else {
      Collections.sort(missing, new Comparator() {
        public int compare(Object o1, Object o2) {
          return Name.compare((Name)o1, (Name)o2);
        }
      });
      StringBuffer buf = new StringBuffer();
      for (Iterator iter = missing.iterator(); iter.hasNext();) {
        // XXX internationalize this better
        if (buf.length() > 0)
          buf.append(", ");
        buf.append(NameFormatter.format((Name)iter.next()));
      }
      error("required_attributes_missing_detail", buf.toString());
    }
  }

  public boolean matchTextBeforeEndTag(String string, ValidationContext vc) {
    if (textTyped) {
      ignoreNextEndTag = true;
      return setDataDeriv(string, vc);
    }
    else
      return matchUntypedText(string);
  }

  public boolean matchTextBeforeStartTag(String string) {
    return matchUntypedText(string);
  }

  private boolean matchUntypedText(String string) {
    if (DataDerivFunction.isBlank(string))
      return true;
    return matchUntypedText();
  }

  public boolean matchUntypedText() {
    if (setMemo(memo.mixedTextDeriv()))
      return true;
    return error("text_not_allowed");
  }

  public boolean isTextTyped() {
    return textTyped;
  }

  private boolean setDataDeriv(String string, ValidationContext vc) {
    textTyped = false;
    if (!setMemo(memo.textOnly())) {
      boolean ok = error("only_text_not_allowed");
      memo = memo.recoverAfter();
      return ok;
    }
    if (setMemo(memo.dataDeriv(string, vc))) {
      ignoreNextEndTag = true;
      return true;
    }
    PatternMemo next = memo.recoverAfter();
    boolean ok = true;
    if (!memo.isNotAllowed()) {
      if (!next.isNotAllowed()
          || shared.fixAfter(memo).dataDeriv(string, vc).isNotAllowed())
        ok = error("string_not_allowed");
    }
    memo = next;
    return ok;
  }

  public boolean matchEndTag(ValidationContext vc) {
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (ignoreNextEndTag) {
      ignoreNextEndTag = false;
      return true;
    }
    if (textTyped) {
      textTyped = false;
      return setDataDeriv("", vc);
    }
    if (setMemo(memo.endTagDeriv()))
      return true;
    boolean ok = true;
    PatternMemo next = memo.recoverAfter();
    if (!memo.isNotAllowed()) {
      if (!next.isNotAllowed()
          || shared.fixAfter(memo).endTagDeriv().isNotAllowed())
        ok = error("unfinished_element");
    }
    memo = next;
    return ok;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public boolean isValidSoFar() {
    return !hadError;
  }

  static abstract class PossibleNamesFunction extends AbstractPatternFunction {
    private UnionNameClassNormalizer normalizer = new UnionNameClassNormalizer();

    NormalizedNameClass applyTo(Pattern p) {
      p.apply(this);
      return normalizer.normalize();
    }

    void add(NameClass nc) {
      normalizer.add(nc);
    }

    public Object caseAfter(AfterPattern p) {
      return p.getOperand1().apply(this);
    }

    public Object caseBinary(BinaryPattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return null;
    }

    public Object caseChoice(ChoicePattern p) {
      return caseBinary(p);
    }

    public Object caseInterleave(InterleavePattern p) {
      return caseBinary(p);
    }

    public Object caseOneOrMore(OneOrMorePattern p) {
      return p.getOperand().apply(this);
    }

    public Object caseOther(Pattern p) {
      return null;
    } 
  }

  static class PossibleStartTagNamesFunction extends PossibleNamesFunction {
    public Object caseElement(ElementPattern p) {
      add(p.getNameClass());
      return null;
    }

    public Object caseGroup(GroupPattern p) {
      p.getOperand1().apply(this);
      if (p.getOperand1().isNullable())
        p.getOperand2().apply(this);
      return null;
    }
  }

  static class PossibleAttributeNamesFunction extends PossibleNamesFunction {
    public Object caseAttribute(AttributePattern p) {
      add(p.getNameClass());
      return null;
    }

    public Object caseGroup(GroupPattern p) {
      return caseBinary(p);
    }
  }

  public com.thaiopensource.relaxng.match.NameClass possibleStartTagNames() {
    return new PossibleStartTagNamesFunction().applyTo(memo.getPattern());
  }

  public com.thaiopensource.relaxng.match.NameClass possibleAttributeNames() {
    return new PossibleAttributeNamesFunction().applyTo(memo.getPattern());
  }

  public Set requiredAttributeNames() {
    return (Set)memo.getPattern().apply(shared.builder.getRequiredAttributesFunction());
  }

  private boolean setMemo(PatternMemo m) {
    if (m.isNotAllowed())
      return false;
    else {
      memo = m;
      return true;
    }
  }

  private boolean ignoreError() {
    return hadError && memo.isNotAllowed();
  }

  /**
   * Return true if the error was ignored, false otherwise.
   */
  private boolean error(String key) {
    if (ignoreError())
      return true;
    hadError = true;
    errorMessage = SchemaBuilderImpl.localizer.message(key);
    return false;
  }

  private boolean error(String key, Name arg) {
    return error(key, NameFormatter.format(arg));
  }

  private boolean error(String key, String arg) {
    if (hadError && memo.isNotAllowed())
      return true;
    hadError = true;
    errorMessage = SchemaBuilderImpl.localizer.message(key, arg);
    return false;
  }

}
