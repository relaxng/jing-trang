package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Div;
import com.thaiopensource.relaxng.parse.ElementAnnotationBuilder;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedPatternFuture;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.SubParseable;
import com.thaiopensource.relaxng.parse.SubParser;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaBuilderImpl extends AnnotationsImpl implements
        ElementAnnotationBuilder<Locator, VoidValue, CommentListImpl>,
        SchemaBuilder<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> {
  private final SchemaBuilderImpl parent;
  private boolean hadError = false;
  private final SubParser<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> subParser;
  private final SchemaPatternBuilder pb;
  private final DatatypeLibraryFactory datatypeLibraryFactory;
  private final String inheritNs;
  private final ErrorHandler eh;
  private final OpenIncludes openIncludes;
  private final AttributeNameClassChecker attributeNameClassChecker = new AttributeNameClassChecker();
  static final Localizer localizer = new Localizer(SchemaBuilderImpl.class);

  static class OpenIncludes {
    final String uri;
    final OpenIncludes parent;

    OpenIncludes(String uri, OpenIncludes parent) {
      this.uri = uri;
      this.parent = parent;
    }
  }

  static public Pattern parse(Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> parseable,
                              ErrorHandler eh,
                              DatatypeLibraryFactory datatypeLibraryFactory,
                              SchemaPatternBuilder pb,
                              boolean isAttributesPattern)
          throws IllegalSchemaException, IOException, SAXException {
    try {
      SchemaBuilderImpl sb = new SchemaBuilderImpl(parseable,
                                                   eh,
                                                   new BuiltinDatatypeLibraryFactory(datatypeLibraryFactory),
                                                   pb);
      Pattern pattern = parseable.parse(sb, new RootScope(sb));
      if (isAttributesPattern)
        pattern = sb.wrapAttributesPattern(pattern);
      return sb.expandPattern(pattern);
    }
    catch (BuildException e) {
      throw unwrapBuildException(e);
    }
  }


  static public PatternFuture installHandlers(ParseReceiver<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> parser,
                                              XMLReader xr,
                                              ErrorHandler eh,
                                              DatatypeLibraryFactory dlf,
                                              SchemaPatternBuilder pb)
          throws SAXException {
    final SchemaBuilderImpl sb = new SchemaBuilderImpl(parser,
                                                       eh,
                                                       new BuiltinDatatypeLibraryFactory(dlf),
                                                       pb);
    final ParsedPatternFuture<Pattern> pf = parser.installHandlers(xr, sb, new RootScope(sb));
    return new PatternFuture() {
      public Pattern getPattern(boolean isAttributesPattern) throws IllegalSchemaException, SAXException, IOException {
        try {
          Pattern pattern = pf.getParsedPattern();
          if (isAttributesPattern)
            pattern = sb.wrapAttributesPattern(pattern);
          return sb.expandPattern(pattern);
        }
        catch (BuildException e) {
          throw unwrapBuildException(e);
        }
      }
    };
  }

  static public RuntimeException unwrapBuildException(BuildException e) throws SAXException, IllegalSchemaException, IOException {
    Throwable t = e.getCause();
    if (t instanceof IOException)
      throw (IOException)t;
    if (t instanceof RuntimeException)
      return (RuntimeException)t;
    if (t instanceof IllegalSchemaException)
      throw new IllegalSchemaException();
    if (t instanceof SAXException)
      throw (SAXException)t;
    if (t instanceof Exception)
      throw new SAXException((Exception)t);
    throw new SAXException(t.getClass().getName() + " thrown");
  }

  private Pattern wrapAttributesPattern(Pattern pattern) {
    // XXX where can we get a locator from?
    return makeElement(makeAnyName(null, null), pattern, null, null);
  }

  private Pattern expandPattern(Pattern pattern) throws IllegalSchemaException, BuildException {
    if (!hadError) {
      try {
        pattern.checkRecursion(0);
        pattern = pattern.expand(pb);
        pattern.checkRestrictions(Pattern.START_CONTEXT, null, null);
        if (!hadError)
          return pattern;
      }
      catch (SAXParseException e) {
        error(e);
      }
      catch (SAXException e) {
        throw new BuildException(e);
      }
      catch (RestrictionViolationException e) {
        if (e.getName() != null)
          error(e.getMessageId(), NameFormatter.format(e.getName()), e.getLocator());
        else if (e.getNamespaceUri() != null)
          error(e.getMessageId(), e.getNamespaceUri(), e.getLocator());
        else
          error(e.getMessageId(), e.getLocator());
      }
    }
    throw new IllegalSchemaException();
  }

  private SchemaBuilderImpl(SubParser<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> subParser,
                            ErrorHandler eh,
                            DatatypeLibraryFactory datatypeLibraryFactory,
                            SchemaPatternBuilder pb) {
    this.parent = null;
    this.subParser = subParser;
    this.eh = eh;
    this.datatypeLibraryFactory = datatypeLibraryFactory;
    this.pb = pb;
    this.inheritNs = "";
    this.openIncludes = null;
  }

  private SchemaBuilderImpl(String inheritNs,
                            String uri,
                            SchemaBuilderImpl parent) {
    this.parent = parent;
    this.subParser = parent.subParser;
    this.eh = parent.eh;
    this.datatypeLibraryFactory = parent.datatypeLibraryFactory;
    this.pb = parent.pb;
    this.inheritNs = parent.resolveInherit(inheritNs);
    this.openIncludes = new OpenIncludes(uri, parent.openIncludes);
  }

  public Pattern makeChoice(List<Pattern> patterns, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    int nPatterns = patterns.size();
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    Pattern result = patterns.get(0);
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeChoice(result, patterns.get(i));
    return result;
  }

  public Pattern makeInterleave(List<Pattern> patterns, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    int nPatterns = patterns.size();
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    Pattern result = patterns.get(0);
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeInterleave(result, patterns.get(i));
    return result;
  }

  public Pattern makeGroup(List<Pattern> patterns, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    int nPatterns = patterns.size();
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    Pattern result = patterns.get(0);
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeGroup(result, patterns.get(i));
    return result;
  }

  public Pattern makeOneOrMore(Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    return pb.makeOneOrMore(p);
  }

  public Pattern makeZeroOrMore(Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    return pb.makeZeroOrMore(p);
  }

  public Pattern makeOptional(Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    return pb.makeOptional(p);
  }

  public Pattern makeList(Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    return pb.makeList(p, loc);
  }

  public Pattern makeMixed(Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    return pb.makeMixed(p);
  }

  public Pattern makeEmpty(Locator loc, AnnotationsImpl anno) {
    return pb.makeEmpty();
  }

  public Pattern makeNotAllowed(Locator loc, AnnotationsImpl anno) {
    return pb.makeUnexpandedNotAllowed();
  }

  public Pattern makeText(Locator loc, AnnotationsImpl anno) {
    return pb.makeText();
  }

  public Pattern makeErrorPattern() {
    return pb.makeError();
  }

  public NameClass makeErrorNameClass() {
    return new ErrorNameClass();
  }

  public Pattern makeAttribute(NameClass nc, Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    String messageId = attributeNameClassChecker.checkNameClass(nc);
    if (messageId != null)
      error(messageId, loc);
    return pb.makeAttribute(nc, p, loc);
  }

  public Pattern makeElement(NameClass nc, Pattern p, Locator loc, AnnotationsImpl anno)
          throws BuildException {
    return pb.makeElement(nc, p, loc);
  }

  private class DummyDataPatternBuilder implements DataPatternBuilder<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> {
    public void addParam(String name, String value, Context context, String ns, Locator loc, AnnotationsImpl anno)
            throws BuildException {
    }

    public void annotation(VoidValue ea)
            throws BuildException {
    }

    public Pattern makePattern(Locator loc, AnnotationsImpl anno)
            throws BuildException {
      return pb.makeError();
    }

    public Pattern makePattern(Pattern except, Locator loc, AnnotationsImpl anno)
            throws BuildException {
      return pb.makeError();
    }
  }

  private class ValidationContextImpl implements ValidationContext {
    private final ValidationContext vc;
    private final String ns;

    ValidationContextImpl(ValidationContext vc, String ns) {
      this.vc = vc;
      this.ns = ns.length() == 0 ? null : ns;
    }

    public String resolveNamespacePrefix(String prefix) {
      String result = prefix.length() == 0 ? ns : vc.resolveNamespacePrefix(prefix);
      if (result == INHERIT_NS) {
        if (inheritNs.length() == 0)
          return null;
        return inheritNs;
      }
      return result;
    }

    public String getBaseUri() {
      return vc.getBaseUri();
    }

    public boolean isUnparsedEntity(String entityName) {
      return vc.isUnparsedEntity(entityName);
    }

    public boolean isNotation(String notationName) {
      return vc.isNotation(notationName);
    }
  }

  private class DataPatternBuilderImpl implements DataPatternBuilder<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> {
    private final DatatypeBuilder dtb;
    private final Name dtName;
    private final List<String> params = new ArrayList<String>();
    DataPatternBuilderImpl(DatatypeBuilder dtb, Name dtName) {
      this.dtb = dtb;
      this.dtName = dtName;
    }

    public void addParam(String name, String value, Context context, String ns, Locator loc, AnnotationsImpl anno)
            throws BuildException {
      try {
        dtb.addParameter(name, value, new ValidationContextImpl(context, ns));
        params.add(name);
        params.add(value);
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
        int pos = e.getIndex();
        String displayedParam;
        if (pos == DatatypeException.UNKNOWN)
          displayedParam = null;
        else
          displayedParam = displayParam(value, pos);
        if (displayedParam != null) {
          if (detail != null)
            error("invalid_param_detail_display", detail, displayedParam, loc);
          else
            error("invalid_param_display", displayedParam, loc);
        }
	else if (detail != null)
	  error("invalid_param_detail", detail, loc);
	else
	  error("invalid_param", loc);
      }
    }

    public void annotation(VoidValue ea)
            throws BuildException {
    }

    String displayParam(String value, int pos) {
      if (pos < 0)
        pos = 0;
      else if (pos > value.length())
        pos = value.length();
      return localizer.message("display_param", value.substring(0, pos), value.substring(pos));
    }

    public Pattern makePattern(Locator loc, AnnotationsImpl anno)
            throws BuildException {
      try {
        return pb.makeData(dtb.createDatatype(), dtName, params);
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_params_detail", detail, loc);
	else
	  error("invalid_params", loc);
        return pb.makeError();
      }
    }

    public Pattern makePattern(Pattern except, Locator loc, AnnotationsImpl anno)
            throws BuildException {
      try {
        return pb.makeDataExcept(dtb.createDatatype(), dtName, params, except, loc);
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_params_detail", detail, loc);
	else
	  error("invalid_params", loc);
        return pb.makeError();
      }
    }
  }

  public DataPatternBuilder<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> makeDataPatternBuilder(String datatypeLibrary, String type, Locator loc)
          throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, loc);
    else {
      try {
        return new DataPatternBuilderImpl(dl.createDatatypeBuilder(type), new Name(datatypeLibrary, type));
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("unsupported_datatype_detail", datatypeLibrary, type, detail, loc);
	else
	  error("unrecognized_datatype", datatypeLibrary, type, loc);
      }
    }
    return new DummyDataPatternBuilder();
  }

  public Pattern makeValue(String datatypeLibrary, String type, String value, Context context, String ns,
                                 Locator loc, AnnotationsImpl anno) throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, loc);
    else {
      try {
        DatatypeBuilder dtb = dl.createDatatypeBuilder(type);
        try {
          Datatype dt = dtb.createDatatype();
          Object obj = dt.createValue(value, new ValidationContextImpl(context, ns));
          if (obj != null)
            return pb.makeValue(dt, new Name(datatypeLibrary, type), obj, value);
          error("invalid_value", value, loc);
        }
        catch (DatatypeException e) {
          String detail = e.getMessage();
          if (detail != null)
            error("datatype_requires_param_detail", detail, loc);
          else
            error("datatype_requires_param", loc);
        }
      }
      catch (DatatypeException e) {
        error("unrecognized_datatype", datatypeLibrary, type, loc);
      }
    }
    return pb.makeError();
  }

  static class GrammarImpl implements Grammar<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl>, Div<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl>, IncludedGrammar<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> {
    private final SchemaBuilderImpl sb;
    private final Map<String, RefPattern> defines;
    private final RefPattern startRef;
    private final Scope<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> parent;

    private GrammarImpl(SchemaBuilderImpl sb, Scope<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> parent) {
      this.sb = sb;
      this.parent = parent;
      this.defines = new HashMap<String, RefPattern>();
      this.startRef = new RefPattern(null);
    }

    protected GrammarImpl(SchemaBuilderImpl sb, GrammarImpl g) {
      this.sb = sb;
      parent = g.parent;
      startRef = g.startRef;
      defines = g.defines;
    }

    public Pattern endGrammar(Locator loc, AnnotationsImpl anno) throws BuildException {
      for (String name : defines.keySet()) {
        RefPattern rp = defines.get(name);
        if (rp.getPattern() == null) {
          sb.error("reference_to_undefined", name, rp.getRefLocator());
          rp.setPattern(sb.pb.makeError());
        }
      }
      Pattern start = startRef.getPattern();
      if (start == null) {
        sb.error("missing_start_element", loc);
        start = sb.pb.makeError();
      }
      return start;
    }

    public void endDiv(Locator loc, AnnotationsImpl anno) throws BuildException {
      // nothing to do
    }

    public Pattern endIncludedGrammar(Locator loc, AnnotationsImpl anno) throws BuildException {
      return null;
    }

    public void define(String name, GrammarSection.Combine combine, Pattern pattern, Locator loc, AnnotationsImpl anno)
            throws BuildException {
      define(lookup(name), combine, pattern, loc);
    }

    private void define(RefPattern rp, GrammarSection.Combine combine, Pattern pattern, Locator loc)
            throws BuildException {
      switch (rp.getReplacementStatus()) {
      case RefPattern.REPLACEMENT_KEEP:
        if (combine == null) {
          if (rp.isCombineImplicit()) {
            if (rp.getName() == null)
              sb.error("duplicate_start", loc);
            else
              sb.error("duplicate_define", rp.getName(), loc);
          }
          else
            rp.setCombineImplicit();
        }
        else {
          byte combineType = (combine == COMBINE_CHOICE ? RefPattern.COMBINE_CHOICE : RefPattern.COMBINE_INTERLEAVE);
          if (rp.getCombineType() != RefPattern.COMBINE_NONE
              && rp.getCombineType() != combineType) {
            if (rp.getName() == null)
              sb.error("conflict_combine_start", loc);
            else
              sb.error("conflict_combine_define", rp.getName(), loc);
          }
          rp.setCombineType(combineType);
        }
        if (rp.getPattern() == null)
          rp.setPattern(pattern);
        else if (rp.getCombineType() == RefPattern.COMBINE_INTERLEAVE)
          rp.setPattern(sb.pb.makeInterleave(rp.getPattern(), pattern));
        else
          rp.setPattern(sb.pb.makeChoice(rp.getPattern(), pattern));
        break;
      case RefPattern.REPLACEMENT_REQUIRE:
        rp.setReplacementStatus(RefPattern.REPLACEMENT_IGNORE);
        break;
      case RefPattern.REPLACEMENT_IGNORE:
        break;
      }
    }

    public void topLevelAnnotation(VoidValue ea) throws BuildException {
    }

    public void topLevelComment(CommentListImpl comments) throws BuildException {
    }

    private RefPattern lookup(String name) {
      if (name == START)
        return startRef;
      return lookup1(name);
    }

    private RefPattern lookup1(String name) {
      RefPattern p = defines.get(name);
      if (p == null) {
        p = new RefPattern(name);
        defines.put(name, p);
      }
      return p;
    }

    public Pattern makeRef(String name, Locator loc, AnnotationsImpl anno) throws BuildException {
      RefPattern p = lookup1(name);
      if (p.getRefLocator() == null && loc != null)
        p.setRefLocator(loc);
      return p;
    }

    public Pattern makeParentRef(String name, Locator loc, AnnotationsImpl anno) throws BuildException {
      if (parent == null) {
        sb.error("parent_ref_outside_grammar", loc);
        return sb.makeErrorPattern();
      }
      return parent.makeRef(name, loc, anno);
    }

    public Div<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> makeDiv() {
      return this;
    }

    public Include<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> makeInclude() {
      return new IncludeImpl(sb, this);
    }

  }

  static class RootScope implements Scope<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> {
    private final SchemaBuilderImpl sb;
    RootScope(SchemaBuilderImpl sb) {
      this.sb = sb;
    }

    public Pattern makeParentRef(String name, Locator loc, AnnotationsImpl anno) throws BuildException {
      sb.error("parent_ref_outside_grammar", loc);
      return sb.makeErrorPattern();
    }
    public Pattern makeRef(String name, Locator loc, AnnotationsImpl anno) throws BuildException {
      sb.error("ref_outside_grammar", loc);
      return sb.makeErrorPattern();
    }

  }

  static class Override {
    Override(RefPattern prp, Override next) {
      this.prp = prp;
      this.next = next;
    }

    final RefPattern prp;
    final Override next;
    byte replacementStatus;
  }


  private static class IncludeImpl implements Include<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl>, Div<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> {
    private final SchemaBuilderImpl sb;
    private Override overrides;
    private final GrammarImpl grammar;

    private IncludeImpl(SchemaBuilderImpl sb, GrammarImpl grammar) {
      this.sb = sb;
      this.grammar = grammar;
    }

    public void define(String name, GrammarSection.Combine combine, Pattern pattern, Locator loc, AnnotationsImpl anno)
            throws BuildException {
      RefPattern rp = grammar.lookup(name);
      overrides = new Override(rp, overrides);
      grammar.define(rp, combine, pattern, loc);
    }

    public void endDiv(Locator loc, AnnotationsImpl anno) throws BuildException {
      // nothing to do
    }

    public void topLevelAnnotation(VoidValue ea) throws BuildException {
      // nothing to do
    }

    public void topLevelComment(CommentListImpl comments) throws BuildException {
    }

    public Div<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> makeDiv() {
      return this;
    }

    public void endInclude(String href, String base, String ns,
                           Locator loc, AnnotationsImpl anno) throws BuildException {
      SubParseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> subParseable
              = sb.subParser.createSubParseable(href, base);
      String uri = subParseable.getUri();
      for (OpenIncludes inc = sb.openIncludes;
           inc != null;
           inc = inc.parent) {
        if (inc.uri.equals(uri)) {
          sb.error("recursive_include", uri, loc);
          return;
        }
      }

      for (Override o = overrides; o != null; o = o.next) {
        o.replacementStatus = o.prp.getReplacementStatus();
        o.prp.setReplacementStatus(RefPattern.REPLACEMENT_REQUIRE);
      }
      try {
        SchemaBuilderImpl isb = new SchemaBuilderImpl(ns, uri, sb);
        subParseable.parseAsInclude(isb, new GrammarImpl(isb, grammar));
        for (Override o = overrides; o != null; o = o.next) {
          if (o.prp.getReplacementStatus() == RefPattern.REPLACEMENT_REQUIRE) {
            if (o.prp.getName() == null)
              sb.error("missing_start_replacement", loc);
            else
              sb.error("missing_define_replacement", o.prp.getName(), loc);
          }
        }
      }
      catch (IllegalSchemaException e) {
        sb.noteError();
      }
      finally {
        for (Override o = overrides; o != null; o = o.next)
          o.prp.setReplacementStatus(o.replacementStatus);
      }
    }

    public Include<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> makeInclude() {
      return null;
    }
  }

  public Grammar<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> makeGrammar(Scope<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> parent) {
    return new GrammarImpl(this, parent);
  }

  public Pattern makeExternalRef(String href, String base, String ns, Scope<Pattern, Locator, VoidValue, CommentListImpl, AnnotationsImpl> scope,
                                 Locator loc, AnnotationsImpl anno)
          throws BuildException {
    SubParseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> subParseable
            = subParser.createSubParseable(href, base);
    String uri = subParseable.getUri();
    for (OpenIncludes inc = openIncludes;
         inc != null;
         inc = inc.parent) {
      if (inc.uri.equals(uri)) {
        error("recursive_include", uri, loc);
        return pb.makeError();
      }
    }
    try {
      return subParseable.parse(new SchemaBuilderImpl(ns, uri, this), scope);
    }
    catch (IllegalSchemaException e) {
      noteError();
      return pb.makeError();
    }
  }

  public NameClass makeNameClassChoice(List<NameClass> nameClasses, Locator loc, AnnotationsImpl anno) {
    int nNameClasses = nameClasses.size();
    if (nNameClasses <= 0)
      throw new IllegalArgumentException();
    NameClass result = nameClasses.get(0);
    for (int i = 1; i < nNameClasses; i++)
      result = new ChoiceNameClass(result, nameClasses.get(i));
    return result;
  }

  public NameClass makeName(String ns, String localName, String prefix, Locator loc, AnnotationsImpl anno) {
    return new SimpleNameClass(new Name(resolveInherit(ns), localName));
  }

  public NameClass makeNsName(String ns, Locator loc, AnnotationsImpl anno) {
    return new NsNameClass(resolveInherit(ns));
  }

  public NameClass makeNsName(String ns, NameClass except, Locator loc, AnnotationsImpl anno) {
    return new NsNameExceptNameClass(resolveInherit(ns), except);
  }

  public NameClass makeAnyName(Locator loc, AnnotationsImpl anno) {
    return new AnyNameClass();
  }

  public NameClass makeAnyName(NameClass except, Locator loc, AnnotationsImpl anno) {
    return new AnyNameExceptNameClass(except);
  }

  public AnnotationsImpl makeAnnotations(CommentListImpl comments, Context context) {
    return this;
  }

  public VoidValue makeElementAnnotation() throws BuildException {
     return VoidValue.VOID;
  }

  public void addText(String value, Locator loc, CommentListImpl comments) throws BuildException {
  }

  public ElementAnnotationBuilder<Locator, VoidValue, CommentListImpl>
  makeElementAnnotationBuilder(String ns, String localName, String prefix,
                               Locator loc, CommentListImpl comments, Context context) {
    return this;
  }

  public CommentListImpl makeCommentList() {
    return this;
  }

  public boolean usesComments() {
    return false;
  }

  public Pattern annotatePattern(Pattern p, AnnotationsImpl anno) throws BuildException {
    return p;
  }

  public NameClass annotateNameClass(NameClass nc, AnnotationsImpl anno) throws BuildException {
    return nc;
  }

  public Pattern annotateAfterPattern(Pattern p, VoidValue e) throws BuildException {
    return p;
  }

  public NameClass annotateAfterNameClass(NameClass nc, VoidValue e) throws BuildException {
    return nc;
  }

  public Pattern commentAfterPattern(Pattern p, CommentListImpl comments) throws BuildException {
    return p;
  }

  public NameClass commentAfterNameClass(NameClass nc, CommentListImpl comments) throws BuildException {
    return nc;
  }

  private String resolveInherit(String ns) {
    if (ns == INHERIT_NS)
      return inheritNs;
    return ns;
  }

  private class LocatorImpl implements Locator {
    private final String systemId;
    private final int lineNumber;
    private final int columnNumber;

    private LocatorImpl(String systemId, int lineNumber, int columnNumber) {
      this.systemId = systemId;
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
    }

    public String getPublicId() {
      return null;
    }

    public String getSystemId() {
      return systemId;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public int getColumnNumber() {
      return columnNumber;
    }
  }

  public Locator makeLocation(String systemId, int lineNumber, int columnNumber) {
    return new LocatorImpl(systemId, lineNumber, columnNumber);
  }

  private void error(SAXParseException message) throws BuildException {
    noteError();
    try {
      if (eh != null)
        eh.error(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }

  /*
  private void warning(SAXParseException message) throws BuildException {
    try {
      if (eh != null)
        eh.warning(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }
  */

  private void error(String key, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key), loc));
  }

  private void error(String key, String arg, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg), loc));
  }

  private void error(String key, String arg1, String arg2, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }

  private void error(String key, String arg1, String arg2, String arg3, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, new Object[]{arg1, arg2, arg3}), loc));
  }
  private void noteError() {
    if (!hadError && parent != null)
      parent.noteError();
    hadError = true;
  }
}
