package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.relaxng.match.MatchContext;
import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.util.Equal;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.ValidationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private boolean ignoreNextEndTagOrAttributeValue;
  private String errorMessage;
  private final Shared shared;

  public PatternMatcher(Pattern start, ValidatorPatternBuilder builder) {
    shared = new Shared(start, builder);
    memo = builder.getPatternMemo(start);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof PatternMatcher))
      return false;
    PatternMatcher other = (PatternMatcher)obj;
    // don't need to test equality of shared, because the memos can only be ==
    // if the shareds are ==.
    return (memo == other.memo
            && hadError == other.hadError
            && Equal.equal(errorMessage, other.errorMessage)
            && ignoreNextEndTagOrAttributeValue == other.ignoreNextEndTagOrAttributeValue
            && textTyped == other.textTyped);
  }

  public int hashCode() {
    return memo.hashCode();
  }

  public final Object clone() {
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

  public boolean matchStartTagOpen(Name name, String qName, MatchContext context) {
    if (setMemo(memo.startTagOpenDeriv(name)))
      return true;
    PatternMemo next = memo.startTagOpenRecoverDeriv(name);
    if (!next.isNotAllowed()) {
      boolean ok = ignoreError();
      if (!ok) {
        Set missing = requiredElementNames();
        if (!missing.isEmpty())
          error(missing.size() == 1
                ? "unexpected_element_required_element_missing"
                : "unexpected_element_required_elements_missing",
                errorArgQName(qName, name, context, false),
                formatNames(missing, FORMAT_NAMES_ELEMENT|FORMAT_NAMES_AND, context));
        else
          error("unexpected_element_required_elements_missing_no_info",
                errorArgQName(qName, name, context, false));
      }
      memo = next;
      return ok;
    }
    ValidatorPatternBuilder builder = shared.builder;
    next = builder.getPatternMemo(builder.makeAfter(shared.findElement(name), memo.getPattern()));
    boolean ok = error(next.isNotAllowed() ? "unknown_element" : "out_of_context_element",
                       errorArgQName(qName, name, context, false));
    memo = next;
    return ok;
  }


  public boolean matchAttributeName(Name name, String qName, MatchContext context) {
    if (setMemo(memo.startAttributeDeriv(name)))
      return true;
    ignoreNextEndTagOrAttributeValue = true;
    return error("impossible_attribute_ignored", errorArgQName(qName, name, context, true));
  }

  public boolean matchAttributeValue(Name name, String qName, MatchContext context, String value) {
    if (ignoreNextEndTagOrAttributeValue) {
      ignoreNextEndTagOrAttributeValue = false;
      return true;
    }
    if (setMemo(memo.dataDeriv(value, context)))
      return true;
    boolean ok = error("bad_attribute_value", errorArgQName(qName, name, context, true));
    memo = memo.recoverAfter();
    return ok;
  }

  public boolean matchStartTagClose(MatchContext context) {
    boolean ok;
    if (setMemo(memo.endAttributes()))
      ok = true;
    else {
      ok = ignoreError();
      if (!ok) {
        Set missing = requiredAttributeNames();
        if (missing.isEmpty())
          // XXX Can we do better here? This is probably not very common in practice.
          error("required_attributes_missing_no_info");
        else
          error(missing.size() == 1 ? "required_attribute_missing" : "required_attributes_missing",
                formatNames(missing, FORMAT_NAMES_ATTRIBUTE|FORMAT_NAMES_AND, context));
      }
      memo = memo.ignoreMissingAttributes();
    }
    textTyped = memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE;
    return ok;
  }

  public boolean matchTextBeforeEndTag(String string, MatchContext context) {
    if (textTyped) {
      ignoreNextEndTagOrAttributeValue = true;
      return setDataDeriv(string, context);
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
      ignoreNextEndTagOrAttributeValue = true;
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

  public boolean matchEndTag(MatchContext context) {
    if (ignoreNextEndTagOrAttributeValue) {
      ignoreNextEndTagOrAttributeValue = false;
      return true;
    }
    if (textTyped)
      return setDataDeriv("", context);
    if (setMemo(memo.endTagDeriv()))
      return true;
    boolean ok = ignoreError();
    PatternMemo next = memo.recoverAfter();
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (!ok && (!next.isNotAllowed()
                || shared.fixAfter(memo).endTagDeriv().isNotAllowed())) {
      Set missing = requiredElementNames();
      if (!missing.isEmpty())
        error(missing.size() == 1
              ? "incomplete_element_required_element_missing"
              : "incomplete_element_required_elements_missing",
              formatNames(missing, FORMAT_NAMES_ELEMENT|FORMAT_NAMES_AND, context));
      else
        // XXX  Need to do better here. More common than the corresponding attributes case.
        error("incomplete_element_required_elements_missing_no_info");
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

  public com.thaiopensource.relaxng.match.NameClass possibleStartTagNames() {
    return memo.possibleStartTagNames();
  }

  public com.thaiopensource.relaxng.match.NameClass possibleAttributeNames() {
    return memo.possibleAttributeNames();
  }

  public Set requiredElementNames() {
    return (Set)memo.getPattern().apply(shared.builder.getRequiredElementsFunction());
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
    return error(key, new String[] { });
  }

  private boolean error(String key, String arg) {
    return error(key, new String[] { arg });
  }

  private boolean error(String key, String arg1, String arg2) {
    return error(key, new String[] { arg1, arg2 });
  }

  private boolean error(String key, String arg1, String arg2, String arg3) {
    return error(key, new String[] { arg1, arg2, arg3 });
  }

  private boolean error(String key, String[] args) {
    if (ignoreError())
      return true;
    hadError = true;
    errorMessage = SchemaBuilderImpl.localizer.message(key, args);
    return false;
  }
   
  private String errorArgQName(String qName, Name name, MatchContext context, boolean isAttribute) {
    if (ignoreError())
      return null;
    if (qName == null || qName.length() == 0) {
      final String ns = name.getNamespaceUri();
      final String localName = name.getLocalName();
      if (ns.length() == 0 || (!isAttribute && ns.equals(context.resolveNamespacePrefix(""))))
        qName = localName;
      else {
        String prefix = context.getPrefix(ns);
        if (prefix != null)
          qName = prefix + ":" + localName;
        // this shouldn't happen unless the parser isn't supplying prefixes properly
        else
          qName = "{" + ns + "}" + localName;
      }
    }
    return quoteQName(qName);
  }

  static private final String GENERATED_PREFIX = "ns";

  // Values for flags parameter of formatNames
  static private final int FORMAT_NAMES_ELEMENT = 0x0;
  static private final int FORMAT_NAMES_ATTRIBUTE = 0x1;
  static private final int FORMAT_NAMES_AND = 0x0;
  static private final int FORMAT_NAMES_OR = 0x2;

  private static String formatNames(Set names, int flags, MatchContext context) {
    if (names.isEmpty())
      return "";
    Map nsDecls = new HashMap();
    String defaultNamespace;
    if ((flags & FORMAT_NAMES_ATTRIBUTE) != 0)
      defaultNamespace = "";
    else {
      defaultNamespace = context.resolveNamespacePrefix("");
      if (defaultNamespace == null)
       defaultNamespace = "";
      else {
        for (Iterator iter = names.iterator(); iter.hasNext();) {
          if (((Name)iter.next()).getNamespaceUri().length() == 0) {
            defaultNamespace = "";
            nsDecls.put("", "");
            break;
          }
        }
      }
    }
    List list = new ArrayList();
    int genPrefixIndex = 0;
    for (Iterator iter = names.iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      String ns = name.getNamespaceUri();
      String prefix;
      if (ns.equals(defaultNamespace))
        prefix = "";
      else {
        prefix = context.getPrefix(ns);
        if (prefix == null) {
          prefix = (String)nsDecls.get(ns);
          if (prefix == null) {
            do {
              if (genPrefixIndex == 0)
                prefix = GENERATED_PREFIX;
              else
                prefix = GENERATED_PREFIX + genPrefixIndex;
              ++genPrefixIndex;
            } while (context.resolveNamespacePrefix(prefix) != null);
            nsDecls.put(ns, prefix);
          }
        }
      }
      list.add(makeQName(prefix, name.getLocalName()));
    }
    Collections.sort(list);
    int len = list.size();
    for (int i = 0; i < len; i++)
      list.set(i, quoteQName((String)list.get(i)));
    String result;
    final String conjunction = (flags & FORMAT_NAMES_OR) != 0 ? "or" : "and";
    switch (list.size()) {
    case 1:
      result = (String)list.get(0);
      break;
    case 2:
      result = SchemaBuilderImpl.localizer.message(conjunction + "_list_pair",
                                                   list.get(0),
                                                   list.get(1));
      break;
    default:
      result = SchemaBuilderImpl.localizer.message(conjunction + "_list_many_first", list.get(0));
      for (int i = 1; i < len - 1; i++)
        result = SchemaBuilderImpl.localizer.message(conjunction + "_list_many_middle", result, list.get(i));
      result = SchemaBuilderImpl.localizer.message(conjunction + "_list_many_last", result, list.get(len - 1));
      break;
    }
    if (nsDecls.size() != 0)
      result = SchemaBuilderImpl.localizer.message("qnames_nsdecls", result, formatNamespaceDecls(nsDecls));
    return result;
  }

  // nsDecls maps namespaces to prefixes
  private static String formatNamespaceDecls(Map nsDecls) {
    List list = new ArrayList();
    for (Iterator iter = nsDecls.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      StringBuffer buf = new StringBuffer();
      String prefix = (String)entry.getValue();
      if (prefix.length() == 0)
        buf.append("xmlns");
      else
        buf.append("xmlns:").append(prefix);
      buf.append("=\"");
      String ns = (String)entry.getKey();
      for (int i = 0; i < ns.length(); i++) {
        char c = ns.charAt(i);
        switch (c) {
        case '<':
          buf.append("&lt;");
          break;
        case '"':
          buf.append("&quot;");
          break;
        default:
          buf.append(c);
          break;
        }
      }
      buf.append('"');
      list.add(buf.toString());
    }
    Collections.sort(list);
    StringBuffer buf = new StringBuffer();
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      if (buf.length() != 0)
        buf.append(" ");
      buf.append((String)iter.next());
    }
    return buf.toString();
  }

  private static String makeQName(String prefix, String localName) {
    if (prefix.length() == 0)
      return localName;
    return prefix + ":" + localName;
  }

  static private String quoteQName(String qName) {
    return SchemaBuilderImpl.localizer.message("qname", qName);
  }
}
