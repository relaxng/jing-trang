package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Div;
import com.thaiopensource.relaxng.parse.ParsedElementAnnotation;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.Location;
import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.ElementAnnotationBuilder;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.parse.nonxml.NonXmlParseable;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.InputSource;

import java.util.List;
import java.util.Vector;
import java.util.Map;

/* Need to change handling of parentRef */
/* Need to deal with inheritNs */
public class SchemaBuilderImpl implements SchemaBuilder {
  private final DatatypeLibraryFactory dlf;

  private SchemaBuilderImpl(DatatypeLibraryFactory dlf) {
    this.dlf = dlf;
  }

  public ParsedPattern makeChoice(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException  {
    return makeComposite(new ChoicePattern(), patterns, nPatterns, loc, anno);
  }

  private ParsedPattern makeComposite(CompositePattern p, ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException {
    List children = p.getChildren();
    for (int i = 0; i < nPatterns; i++)
      children.add(patterns[i]);
    return finishPattern(p, loc, anno);
  }

  public ParsedPattern makeGroup(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException {
    return makeComposite(new GroupPattern(), patterns, nPatterns, loc, anno);
  }

  public ParsedPattern makeInterleave(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException {
    return makeComposite(new InterleavePattern(), patterns, nPatterns, loc, anno);
  }

  public ParsedPattern makeOneOrMore(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new OneOrMorePattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeZeroOrMore(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new ZeroOrMorePattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeOptional(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new OptionalPattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeList(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new ListPattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeMixed(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new MixedPattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeEmpty(Location loc, Annotations anno) {
    return finishPattern(new EmptyPattern(), loc, anno);
  }

  public ParsedPattern makeNotAllowed(Location loc, Annotations anno) {
    return finishPattern(new NotAllowedPattern(), loc, anno);
  }

  public ParsedPattern makeText(Location loc, Annotations anno) {
    return finishPattern(new TextPattern(), loc, anno);
  }

  public ParsedPattern makeAttribute(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new AttributePattern((NameClass)nc, (Pattern)p), loc, anno);
  }

  public ParsedPattern makeElement(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new ElementPattern((NameClass)nc, (Pattern)p), loc, anno);
  }

  private static class TraceValidationContext implements ValidationContext {
    private final Map map;
    private final ValidationContext vc;
    TraceValidationContext(Map map, ValidationContext vc) {
      this.map = map;
      this.vc = vc;
    }

    public String resolveNamespacePrefix(String prefix) {
      String ns = vc.resolveNamespacePrefix(prefix);
      if (ns != null)
        map.put(prefix, ns);
      return ns;
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

  public ParsedPattern makeValue(String datatypeLibrary, String type, String value, ValidationContext vc,
                                 Location loc, Annotations anno) throws BuildException {
    ValuePattern p = new ValuePattern(datatypeLibrary, type, value);
    DatatypeLibrary dl = dlf.createDatatypeLibrary(datatypeLibrary);
    if (dl != null) {
      try {
        Datatype dt = dl.createDatatype(type);
        if (dt.isContextDependent())
          dt.checkValid(value, new TraceValidationContext(p.getPrefixMap(), vc));
      }
      catch (DatatypeException e) {
      }
    }
    return finishPattern(p, loc, anno);
  }

  public ParsedPattern makeExternalRef(String uri, String ns, Scope scope,
                                       Location loc, Annotations anno) throws BuildException, IllegalSchemaException {
    ExternalRefPattern p = new ExternalRefPattern(uri);
    p.setNs(mapInheritNs(ns));
    return finishPattern(p, loc, anno);
  }

  static private ParsedPattern finishPattern(Pattern p, Location loc, Annotations anno) {
    finishAnnotated(p, loc, anno);
    return p;
  }

  public ParsedNameClass makeChoice(ParsedNameClass[] nameClasses, int nNameClasses, Location loc, Annotations anno) {
    ChoiceNameClass nc = new ChoiceNameClass();
    List children = nc.getChildren();
    for (int i = 0; i < nNameClasses; i++)
      children.add(nameClasses[i]);
    return finishNameClass(nc, loc, anno);
  }

  public ParsedNameClass makeName(String ns, String localName, String prefix, Location loc, Annotations anno) {
    NameNameClass nc = new NameNameClass(mapInheritNs(ns), localName);
    nc.setPrefix(prefix);
    return finishNameClass(nc, loc, anno);
  }

  public ParsedNameClass makeNsName(String ns, Location loc, Annotations anno) {
    return finishNameClass(new NsNameNameClass(mapInheritNs(ns)), loc, anno);
  }

  public ParsedNameClass makeNsName(String ns, ParsedNameClass except, Location loc, Annotations anno) {
    return finishNameClass(new NsNameNameClass(mapInheritNs(ns), (NameClass)except), loc, anno);
  }

  public ParsedNameClass makeAnyName(Location loc, Annotations anno) {
    return finishNameClass(new AnyNameNameClass(), loc, anno);
  }

  public ParsedNameClass makeAnyName(ParsedNameClass except, Location loc, Annotations anno) {
    return finishNameClass(new AnyNameNameClass((NameClass)except), loc, anno);
  }

  private static class ParentScope implements Scope {
    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
      return finishPattern(new ParentRefPattern(name), loc, anno);
    }
    public Scope getParent() {
      return null;
    }
  }

  private static class ScopeImpl implements Scope {
    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
      return finishPattern(new RefPattern(name), loc, anno);
    }

    public Scope getParent() {
      return new ParentScope();
    }
  }

  private static class GrammarSectionImpl extends ScopeImpl implements Grammar, Div, Include {
    private Annotated subject;
    private List components;
    Component lastComponent;

    private GrammarSectionImpl(Annotated subject, Container container) {
      this.subject = subject;
      this.components = container.getComponents();
    }

    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
            throws BuildException {
      if (name == GrammarSection.START)
        name = DefineComponent.START;
      DefineComponent dc = new DefineComponent(name, (Pattern)pattern);
      if (combine != null)
        dc.setCombine(mapCombine(combine));
      finishAnnotated(dc, loc, anno);
      add(dc);
    }

    public Div makeDiv() {
      DivComponent dc = new DivComponent();
      add(dc);
      return new GrammarSectionImpl(dc, dc);
    }

    public Include makeInclude() {
      IncludeComponent ic = new IncludeComponent();
      add(ic);
      return new GrammarSectionImpl(ic, ic);
    }

    public void topLevelAnnotation(ParsedElementAnnotation ea) throws BuildException {
      addAfterAnnotation(lastComponent, ea);
    }

    private void add(Component c) {
      components.add(c);
      lastComponent = c;
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
      finishAnnotated(subject, loc, anno);
    }

    public void endInclude(String uri, String ns,
                           Location loc, Annotations anno) throws BuildException, IllegalSchemaException {
      IncludeComponent ic = (IncludeComponent)subject;
      ic.setHref(uri);
      ic.setNs(mapInheritNs(ns));
      finishAnnotated(ic, loc, anno);
    }

    public ParsedPattern endGrammar(Location loc, Annotations anno) throws BuildException {
      finishAnnotated(subject, loc, anno);
      return (ParsedPattern)subject;
    }
  }

  public Grammar makeGrammar(Scope parent) {
    GrammarPattern g = new GrammarPattern();
    return new GrammarSectionImpl(g, g);
  }

  private static ParsedNameClass finishNameClass(NameClass nc, Location loc, Annotations anno) {
    finishAnnotated(nc, loc, anno);
    return nc;
  }

  private static void finishAnnotated(Annotated a, Location loc, Annotations anno) {
    a.setSourceLocation((SourceLocation)loc);
    if (anno != null)
      ((AnnotationsImpl)anno).apply(a);
  }

  public ParsedPattern annotateAfter(ParsedPattern p, ParsedElementAnnotation e) throws BuildException {
    addAfterAnnotation((Pattern)p, e);
    return p;
  }

  public ParsedNameClass annotateAfter(ParsedNameClass nc, ParsedElementAnnotation e) throws BuildException {
    addAfterAnnotation((NameClass)nc, e);
    return nc;
  }

  static private void addAfterAnnotation(Annotated a, ParsedElementAnnotation e) {
    a.getFollowingElementAnnotations().add(e);
  }

  public Location makeLocation(String systemId, int lineNumber, int columnNumber) {
    return new SourceLocation(systemId, lineNumber, columnNumber);
  }

  private static class DataPatternBuilderImpl implements DataPatternBuilder {
    private DataPattern p;

    DataPatternBuilderImpl(DataPattern p) {
      this.p = p;
    }

    public void addParam(String name, String value, ValidationContext vc, Location loc, Annotations anno)
            throws BuildException {
      Param param = new Param(name, value);
      finishAnnotated(param, loc, anno);
      p.getParams().add(param);
    }

    public ParsedPattern makePattern(Location loc, Annotations anno)
            throws BuildException {
      return finishPattern(p, loc, anno);
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
            throws BuildException {
      p.setExcept((Pattern)except);
      return finishPattern(p, loc, anno);
    }
  }

  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location loc) throws BuildException {
    return new DataPatternBuilderImpl(new DataPattern(datatypeLibrary, type));
  }

  public ParsedPattern makeErrorPattern() {
    return null;
  }

  public ParsedNameClass makeErrorNameClass() {
    return null;
  }

  private static class AnnotationsImpl implements Annotations {
    private List attributes = new Vector();
    private List elements = new Vector();
    public void addAttribute(String ns, String localName, String prefix, String value, Location loc)
            throws BuildException {
      AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
      att.setPrefix(prefix);
      att.setSourceLocation((SourceLocation)loc);
      attributes.add(att);
    }

    public void addElement(ParsedElementAnnotation ea) throws BuildException {
      elements.add(ea);
    }

    void apply(Annotated subject) {
      subject.getAttributeAnnotations().addAll(attributes);
      subject.getChildElementAnnotations().addAll(elements);
    }
  }

  public Annotations makeAnnotations() {
    return new AnnotationsImpl();
  }

  private static class ElementAnnotationBuilderImpl implements ElementAnnotationBuilder {
    private final ElementAnnotation element;

    ElementAnnotationBuilderImpl(ElementAnnotation element) {
      this.element = element;
    }

    public void addText(String value, Location loc) throws BuildException {
      TextAnnotation t = new TextAnnotation(value);
      t.setSourceLocation((SourceLocation)loc);
      element.getChildren().add(t);
    }

    public void addAttribute(String ns, String localName, String prefix, String value, Location loc)
            throws BuildException {
      AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
      att.setPrefix(prefix);
      att.setSourceLocation((SourceLocation)loc);
      element.getAttributes().add(att);
    }

    public ParsedElementAnnotation makeElementAnnotation() throws BuildException {
      return element;
    }

    public void addElement(ParsedElementAnnotation ea) throws BuildException {
      element.getChildren().add(ea);
    }
  }

  public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix, Location loc) {
    ElementAnnotation element = new ElementAnnotation(ns, localName);
    element.setPrefix(prefix);
    element.setSourceLocation((SourceLocation)loc);
    return new ElementAnnotationBuilderImpl(element);
  }

  private static Combine mapCombine(GrammarSection.Combine combine) {
    if (combine == null)
      return null;
    return combine == GrammarSection.COMBINE_CHOICE ? Combine.CHOICE : Combine.INTERLEAVE;
  }

  private static String mapInheritNs(String ns) {
    return ns;
  }

  static public Pattern parse(Parseable parseable, DatatypeLibraryFactory dlf) throws IllegalSchemaException {
    return (Pattern)parseable.parse(new SchemaBuilderImpl(dlf));
  }

  static public void main(String[] args) throws IllegalSchemaException {
    Pattern p = parse(new SAXParseable(new Jaxp11XMLReaderCreator(), new InputSource(args[0]), new DraconianErrorHandler()),
                      new DatatypeLibraryLoader());
    dump(p);
  }

  static public void dump(Pattern p) {
  }
}
