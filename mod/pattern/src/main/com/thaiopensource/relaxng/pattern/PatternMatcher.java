package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.relaxng.match.MatchContext;
import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.util.Equal;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.util.Name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

  private PatternMatcher(PatternMemo memo, Shared shared) {
    this.memo = memo;
    this.shared = shared;
  }

  public Matcher start() {
    return new PatternMatcher(shared.builder.getPatternMemo(shared.start), shared);
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
          error("element_not_allowed_yet",
                errorArgQName(qName, name, context, false),
                expectedContent(context));
      }
      memo = next;
      return ok;
    }
    ValidatorPatternBuilder builder = shared.builder;
    next = builder.getPatternMemo(builder.makeAfter(shared.findElement(name), memo.getPattern()));
    boolean ok = error(next.isNotAllowed() ? "unknown_element" : "out_of_context_element",
                       errorArgQName(qName, name, context, false),
                       expectedContent(context));
    memo = next;
    return ok;
  }

  public boolean matchAttributeName(Name name, String qName, MatchContext context) {
    if (setMemo(memo.startAttributeDeriv(name)))
      return true;
    ignoreNextEndTagOrAttributeValue = true;
    boolean ok = ignoreError();
    if (ok)
      return true;
    qName = errorArgQName(qName, name, context, true);
    NormalizedNameClass nnc = memo.possibleAttributeNames();
    if (nnc.isEmpty())
      error("no_attributes_allowed", qName);
    else
      error("invalid_attribute_name", qName, expectedAttributes(context));
    return false;
  }

  public boolean matchAttributeValue(String value, Name name, String qName, MatchContext context) {
    if (ignoreNextEndTagOrAttributeValue) {
      ignoreNextEndTagOrAttributeValue = false;
      return true;
    }
    if (setMemo(memo.dataDeriv(value, context)))
      return true;
    boolean ok = error("invalid_attribute_value", errorArgQName(qName, name, context, true));
    memo = memo.recoverAfter();
    return ok;
  }

  public boolean matchStartTagClose(Name name, String qName, MatchContext context) {
    boolean ok;
    if (setMemo(memo.endAttributes()))
      ok = true;
    else {
      ok = ignoreError();
      if (!ok) {
        Set missing = requiredAttributeNames();
        if (missing.isEmpty())
          error("required_attributes_missing_expected",
                errorArgQName(qName, name, context, false),
                expectedAttributes(context));
        else
          error(missing.size() == 1 ? "required_attribute_missing" : "required_attributes_missing",
                errorArgQName(qName, name, context, false),
                formatNames(missing, FORMAT_NAMES_ATTRIBUTE|FORMAT_NAMES_AND, context));
      }
      memo = memo.ignoreMissingAttributes();
    }
    textTyped = memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE;
    return ok;
  }

  public boolean matchTextBeforeEndTag(String string, Name name, String qName, MatchContext context) {
    if (textTyped) {
      ignoreNextEndTagOrAttributeValue = true;
      return setDataDeriv(string, name, qName, context);
    }
    else
      return matchUntypedText(string, context);
  }

  public boolean matchTextBeforeStartTag(String string, MatchContext context) {
    return matchUntypedText(string, context);
  }

  private boolean matchUntypedText(String string, MatchContext context) {
    if (DataDerivFunction.isBlank(string))
      return true;
    return matchUntypedText(context);
  }

  public boolean matchUntypedText(MatchContext context) {
    if (setMemo(memo.mixedTextDeriv()))
      return true;
    return error("text_not_allowed", expectedContent(context));
  }

  public boolean isTextTyped() {
    return textTyped;
  }

  private boolean setDataDeriv(String string, Name name, String qName, MatchContext context) {
    textTyped = false;
    PatternMemo textOnlyMemo = memo.textOnly();
    if (setMemo(textOnlyMemo.dataDeriv(string, context)))
      return true;
    PatternMemo next = memo.recoverAfter();
    boolean ok = ignoreError();
    if (!ok && (!next.isNotAllowed()
                || shared.fixAfter(textOnlyMemo).dataDeriv(string, context).isNotAllowed())) {
      NormalizedNameClass nnc = memo.possibleStartTagNames();
      if (!nnc.isEmpty() && DataDerivFunction.isBlank(string))
        error("blank_not_allowed",
              errorArgQName(qName, name, context, false),
              expectedContent(context));
      else
        error("invalid_element_value", errorArgQName(qName, name, context, false));
    }
    memo = next;
    return ok;
  }

  public boolean matchEndTag(Name name, String qName, MatchContext context) {
    if (ignoreNextEndTagOrAttributeValue) {
      ignoreNextEndTagOrAttributeValue = false;
      return true;
    }
    if (textTyped)
      return setDataDeriv("", name, qName, context);
    if (setMemo(memo.endTagDeriv()))
      return true;
    boolean ok = ignoreError();
    PatternMemo next = memo.recoverAfter();
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (!ok && (!next.isNotAllowed()
                // Retry computing the deriv on a pattern where the after is OK (not notAllowed)
                || shared.fixAfter(memo).endTagDeriv().isNotAllowed())) {
      Set missing = requiredElementNames();
      if (!missing.isEmpty())
        error(missing.size() == 1
              ? "incomplete_element_required_element_missing"
              : "incomplete_element_required_elements_missing",
              errorArgQName(qName, name, context, false),
              formatNames(missing, FORMAT_NAMES_ELEMENT|FORMAT_NAMES_AND, context));
      else
        // XXX  Could do better here and describe what is required instead of what is possible
        error("incomplete_element_required_elements_missing_expected",
              errorArgQName(qName, name, context, false),
              expectedContent(context));
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
    errorMessage = localizer().message(key, args);
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

  private String expectedAttributes(MatchContext context) {
    if (ignoreError())
      return null;
    NormalizedNameClass nnc = memo.possibleAttributeNames();
    if (nnc.isEmpty())
      return "";
    Set expectedNames = nnc.getIncludedNames();
    if (!expectedNames.isEmpty())
      return localizer().message(nnc.isAnyNameIncluded() || !nnc.getIncludedNamespaces().isEmpty()
                                 ? "expected_attribute_or_other_ns"
                                 : "expected_attribute",
                                 formatNames(expectedNames,
                                             FORMAT_NAMES_ATTRIBUTE|FORMAT_NAMES_OR, context));
    return "";
  }

  private String expectedContent(MatchContext context) {
    if (ignoreError())
      return null;
    // this shouldn't happen, but just in case
    if (memo.isNotAllowed())
      return "";
    int contentType = memo.getPattern().getContentType();
    if (contentType == Pattern.EMPTY_CONTENT_TYPE)
      return localizer().message("expected_end_element");
    NormalizedNameClass nnc = memo.possibleStartTagNames();
    if (nnc.isEmpty()) {
      switch (contentType) {
      case Pattern.MIXED_CONTENT_TYPE:
        return localizer().message("expected_text");
      case Pattern.DATA_CONTENT_TYPE:
        return localizer().message("expected_data");
      default:
        // this shouldn't happen, but just in case
        return "";
      }
    }
    String key;
    switch (contentType) {
    case Pattern.MIXED_CONTENT_TYPE:
      // A pattern such as (element foo { empty }, text) has a MIXED_CONTENT_TYPE
      // but text is not allowed everywhere.
      if (!memo.mixedTextDeriv().isNotAllowed()) {
        key = "expected_text_or_element";
        break;
      }
      // fall through
    case Pattern.ELEMENT_CONTENT_TYPE:
      key = "expected_element";
      break;
    case Pattern.DATA_CONTENT_TYPE:
      key = "expected_data_or_element";
      break;
    default:
      return "";
    }
    Set expectedNames = nnc.getIncludedNames();
    // XXX say something about wildcards
    if (!expectedNames.isEmpty()) {
      if (nnc.isAnyNameIncluded() || !nnc.getIncludedNamespaces().isEmpty())
        key += "_or_other_ns";
      return localizer().message(key, formatNames(expectedNames,
                                                  FORMAT_NAMES_ELEMENT|FORMAT_NAMES_OR,
                                                  context));
    }
    // give up for now
    return "";
  }

  static final String GENERATED_PREFIXES[] = { "ns", "ns-", "ns_", "NS", "NS-", "NS_"};

  // Values for flags parameter of formatNames
  static private final int FORMAT_NAMES_ELEMENT = 0x0;
  static private final int FORMAT_NAMES_ATTRIBUTE = 0x1;
  static private final int FORMAT_NAMES_AND = 0x0;
  static private final int FORMAT_NAMES_OR = 0x2;

  private static String formatNames(Set names, int flags, MatchContext context) {
    if (names.isEmpty())
      return "";
    Map nsDecls = new HashMap();
    List qNames = generateQNames(names, flags, context, nsDecls);
    Collections.sort(qNames);
    int len = qNames.size();
    for (int i = 0; i < len; i++)
      qNames.set(i, quoteQName((String)qNames.get(i)));
    String result = formatList(qNames, (flags & FORMAT_NAMES_OR) != 0 ? "or" : "and");
    if (nsDecls.size() != 0)
      result = localizer().message("qnames_nsdecls", result, formatNamespaceDecls(nsDecls));
    return result;
  }

  private static List generateQNames(Set names, int flags, MatchContext context, Map nsDecls) {
    String defaultNamespace;
    if ((flags & FORMAT_NAMES_ATTRIBUTE) != 0)
      defaultNamespace = "";
    else {
      defaultNamespace = context.resolveNamespacePrefix("");
      for (Iterator iter = names.iterator(); iter.hasNext();) {
        if (((Name)iter.next()).getNamespaceUri().length() == 0) {
          if (defaultNamespace != null)
            nsDecls.put("", "");
          defaultNamespace = "";
          break;
        }
      }
    }
    List qNames = new ArrayList();
    Set undeclaredNamespaces = new HashSet();
    List namesWithUndeclaredNamespaces = new ArrayList();
    for (Iterator iter = names.iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      String ns = name.getNamespaceUri();
      String prefix;
      if (ns.equals(defaultNamespace))
        prefix = "";
      else
        prefix = context.getPrefix(ns);
      if (prefix == null) {
        undeclaredNamespaces.add(ns);
        namesWithUndeclaredNamespaces.add(name);
      }
      else
        qNames.add(makeQName(prefix, name.getLocalName()));
    }
    if (namesWithUndeclaredNamespaces.isEmpty())
      return qNames;
    if (undeclaredNamespaces.size() == 1 && defaultNamespace == null)
      nsDecls.put(undeclaredNamespaces.iterator().next(), "");
    else
      choosePrefixes(undeclaredNamespaces, context, nsDecls);
    // now nsDecls has a prefix for each namespace
    for (Iterator iter = namesWithUndeclaredNamespaces.iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      qNames.add(makeQName((String)nsDecls.get(name.getNamespaceUri()), name.getLocalName()));
    }
    return qNames;
  }

  private static void choosePrefixes(Set nsSet, MatchContext context, Map nsDecls) {
    List nsList = new ArrayList(nsSet);
    Collections.sort(nsList);
    int len = nsList.size();
    String prefix;
    int tryIndex = 0;
    do {
      if (tryIndex < GENERATED_PREFIXES.length)
        prefix = GENERATED_PREFIXES[tryIndex];
      else {
        // default is just to stick as many underscores as necessary at the beginning
        prefix = "_" + GENERATED_PREFIXES[0];
        for (int i = GENERATED_PREFIXES.length; i < tryIndex; i++)
          prefix += "_" + prefix;
      }
      for (int i = 0; i < len; i++) {
        if (context.resolveNamespacePrefix(len == 1 ? prefix : prefix + (i + 1)) != null) {
          prefix = null;
          break;
        }
      }
      ++tryIndex;
    } while (prefix == null);
    for (int i = 0; i < len; i++) {
      String ns = (String)nsList.get(i);
      nsDecls.put(ns, len == 1 ? prefix : prefix + (i + 1));
    }
  }

  private static String formatList(List list, String conjunction) {
    int len = list.size();
    switch (len) {
    case 0:
      return "";
    case 1:
      return (String)list.get(0);
    case 2:
      return localizer().message(conjunction + "_list_pair", list.get(0), list.get(1));
    }
    String s = localizer().message(conjunction + "_list_many_first", list.get(0));
    for (int i = 1; i < len - 1; i++)
      s = localizer().message(conjunction + "_list_many_middle", s, list.get(i));
    return localizer().message(conjunction + "_list_many_last", s, list.get(len - 1));
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
    return localizer().message("qname", qName);
  }

  static private Localizer localizer() {
    return SchemaBuilderImpl.localizer;
  }
}
