package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.impl.XmlBaseHandler;
import com.thaiopensource.relaxng.impl.Localizer;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.Location;
import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.util.Uri;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/*
Deal with annotations
*/
class SchemaParser {

  static final String relaxngURIPrefix = "http://relaxng.org/ns/structure/";
  static final String relaxng10URI = relaxngURIPrefix + "1.0";
  static final String xmlURI = "http://www.w3.org/XML/1998/namespace";
  static final String xsdURI = "http://www.w3.org/2001/XMLSchema-datatypes";

  String relaxngURI;
  XMLReader xr;
  ErrorHandler eh;
  SchemaBuilder schemaBuilder;
  ParsedPattern startPattern;
  Locator locator;
  PrefixMapping prefixMapping;
  XmlBaseHandler xmlBaseHandler = new XmlBaseHandler();

  boolean hadError = false;

  Hashtable patternTable;
  Hashtable nameClassTable;
  Datatype ncNameDatatype;

  static class PrefixMapping {
    final String prefix;
    final String uri;
    final PrefixMapping next;

    PrefixMapping(String prefix, String uri, PrefixMapping next) {
      this.prefix = prefix;
      this.uri = uri;
      this.next = next;
    }
  }

  abstract class State implements ContentHandler, ValidationContext {
    State parent;
    String nsInherit;
    String ns;
    String datatypeLibrary;
    Scope scope;
    Location startLocation;

    void set() {
      xr.setContentHandler(this);
    }

    abstract State create();
    abstract State createChildState(String localName) throws SAXException;

    public void setDocumentLocator(Locator loc) {
      locator = loc;
      xmlBaseHandler.setLocator(loc);
    }

    void setParent(State parent) {
      this.parent = parent;
      this.nsInherit = parent.getNs();
      this.datatypeLibrary = parent.datatypeLibrary;
      this.scope = parent.scope;
      this.startLocation = makeLocation();
    }

    String getNs() {
      return ns == null ? nsInherit : ns;
    }

    boolean isRelaxNGElement(String uri) throws SAXException {
      return uri.equals(relaxngURI);
    }

    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      xmlBaseHandler.startElement();
      if (isRelaxNGElement(namespaceURI)) {
	State state = createChildState(localName);
	if (state == null) {
	  xr.setContentHandler(new Skipper(this));
	  return;
	}
	state.setParent(this);
	state.set();
	state.attributes(atts);
      }
      else {
	checkForeignElement();
	xr.setContentHandler(new Skipper(this));
      }
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      xmlBaseHandler.endElement();
      parent.set();
      end();
    }

    void setName(String name) throws SAXException {
      error("illegal_name_attribute");
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      error("illegal_attribute_ignored", name);
    }

    void endAttributes() throws SAXException {
    }

    void checkForeignElement() throws SAXException {
    }

    void attributes(Attributes atts) throws SAXException {
      int len = atts.getLength();
      for (int i = 0; i < len; i++) {
	String uri = atts.getURI(i);
	if (uri.length() == 0) {
	  String name = atts.getLocalName(i);
	  if (name.equals("name"))
	    setName(atts.getValue(i).trim());
	  else if (name.equals("ns"))
	    ns = atts.getValue(i);
	  else if (name.equals("datatypeLibrary")) {
	    datatypeLibrary = atts.getValue(i);
	    checkUri(datatypeLibrary);
	    if (!datatypeLibrary.equals("")
		&& !Uri.isAbsolute(datatypeLibrary))
	      error("relative_datatype_library");
	    if (Uri.hasFragmentId(datatypeLibrary))
	      error("fragment_identifier_datatype_library");
	    datatypeLibrary = Uri.escapeDisallowedChars(datatypeLibrary);
	  }
	  else
	    setOtherAttribute(name, atts.getValue(i));
	}
	else if (uri.equals(relaxngURI))
	  error("qualified_attribute", atts.getLocalName(i));
	else if (uri.equals(xmlURI)
		 && atts.getLocalName(i).equals("base"))
	  xmlBaseHandler.xmlBaseAttribute(atts.getValue(i));
      }
      endAttributes();
    }

    abstract void end() throws SAXException;

    void endChild(ParsedPattern pattern) {
      // XXX cannot happen; throw exception
    }

    void endChild(ParsedNameClass nc) {
      // XXX cannot happen; throw exception
    }

    public void startDocument() { }
    public void endDocument() throws SAXException { }
    public void processingInstruction(String target, String date) { }
    public void skippedEntity(String name) { }
    public void ignorableWhitespace(char[] ch, int start, int len) { }

    public void characters(char[] ch, int start, int len) throws SAXException {
      for (int i = 0; i < len; i++) {
	switch(ch[start + i]) {
	case ' ':
	case '\r':
	case '\n':
	case '\t':
	  break;
	default:
	  error("illegal_characters_ignored");
	  break;
	}
      }
    }

    public void startPrefixMapping(String prefix, String uri) {
      prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
    }

    public void endPrefixMapping(String prefix) {
      prefixMapping = prefixMapping.next;
    }

    public String resolveNamespacePrefix(String prefix) {
      if (prefix.equals(""))
        return getNs();
      for (PrefixMapping p = prefixMapping; p != null; p = p.next)
        if (p.prefix.equals(prefix))
          return p.uri;
      return null;
    }

    public String getBaseUri() {
      return xmlBaseHandler.getBaseUri();
    }

    public boolean isUnparsedEntity(String name) {
      return false;
    }

    public boolean isNotation(String name) {
      return false;
    }

    boolean isPatternNamespaceURI(String s) {
      return s.equals(relaxngURI);
    }

  }

  class Skipper extends DefaultHandler {
    int level = 1;
    State nextState;

    Skipper(State nextState) {
      this.nextState = nextState;
    }

    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      ++level;
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      if (--level == 0)
	nextState.set();
    }

  }

  abstract class EmptyContentState extends State {

    State createChildState(String localName) throws SAXException {
      error("expected_empty", localName);
      return null;
    }

    abstract ParsedPattern makePattern() throws SAXException;

    void end() throws SAXException {
      parent.endChild(makePattern());
    }
  }

  abstract class PatternContainerState extends State {
    ParsedPattern containedPattern;

    State createChildState(String localName) throws SAXException {
      State state = (State)patternTable.get(localName);
      if (state == null) {
	error("expected_pattern", localName);
	return null;
      }
      return state.create();
    }

    ParsedPattern combinePattern(ParsedPattern p1, ParsedPattern p2) {
      return schemaBuilder.makeGroup(p1, p2, startLocation, null);
    }

    ParsedPattern wrapPattern(ParsedPattern p) throws SAXException {
      return p;
    }

    void endChild(ParsedPattern pattern) {
      if (containedPattern == null)
	containedPattern = pattern;
      else
	containedPattern = combinePattern(containedPattern, pattern);
    }

    void end() throws SAXException {
      if (containedPattern == null) {
	error("missing_children");
	containedPattern = schemaBuilder.makeErrorPattern();
      }
      sendPatternToParent(wrapPattern(containedPattern));
    }

    void sendPatternToParent(ParsedPattern p) {
      parent.endChild(p);
    }
  }

  class GroupState extends PatternContainerState {
    State create() {
      return new GroupState();
    }
  }

  class ZeroOrMoreState extends PatternContainerState {
    State create() {
      return new ZeroOrMoreState();
    }
    ParsedPattern wrapPattern(ParsedPattern p) {
      return schemaBuilder.makeZeroOrMore(p, startLocation, null);
    }
  }

  class OneOrMoreState extends PatternContainerState {
    State create() {
      return new OneOrMoreState();
    }
    ParsedPattern wrapPattern(ParsedPattern p) {
      return schemaBuilder.makeOneOrMore(p, startLocation, null);
    }
  }

  class OptionalState extends PatternContainerState {
    State create() {
      return new OptionalState();
    }
    ParsedPattern wrapPattern(ParsedPattern p) {
      return schemaBuilder.makeOptional(p, startLocation, null);
    }
  }

  class ListState extends PatternContainerState {
    State create() {
      return new ListState();
    }
    ParsedPattern wrapPattern(ParsedPattern p) {
      return schemaBuilder.makeList(p, startLocation, null);
    }
  }

  class ChoiceState extends PatternContainerState {
    State create() {
      return new ChoiceState();
    }
    ParsedPattern combinePattern(ParsedPattern p1, ParsedPattern p2) {
      return schemaBuilder.makeChoice(p1, p2, startLocation, null);
    }
  }

  class InterleaveState extends PatternContainerState {
    State create() {
      return new InterleaveState();
    }
    ParsedPattern combinePattern(ParsedPattern p1, ParsedPattern p2) {
      return schemaBuilder.makeInterleave(p1, p2, startLocation, null);
    }
  }

  class MixedState extends PatternContainerState {
    State create() {
      return new MixedState();
    }
    ParsedPattern wrapPattern(ParsedPattern p) {
      return schemaBuilder.makeMixed(p, startLocation, null);
    }
  }

  static interface NameClassRef {
    void setNameClass(ParsedNameClass nc);
  }

  class ElementState extends PatternContainerState implements NameClassRef {
    ParsedNameClass nameClass;
    String name;

    void setName(String name) {
      this.name = name;
    }

    public void setNameClass(ParsedNameClass nc) {
      nameClass = nc;
    }

    void endAttributes() throws SAXException {
      if (name != null)
	nameClass = expandName(name, getNs());
      else
	new NameClassChildState(this, this).set();
    }

    State create() {
      return new ElementState();
    }

    ParsedPattern wrapPattern(ParsedPattern p) {
      return schemaBuilder.makeElement(nameClass, p, startLocation, null);
    }
  }

  class RootState extends PatternContainerState {
    IncludedGrammar grammar;
    RootState() {
    }

    RootState(IncludedGrammar grammar, Scope scope, String ns) {
      this.grammar = grammar;
      this.scope = scope;
      this.nsInherit = ns;
      this.datatypeLibrary = "";
    }

    State create() {
      return new RootState();
    }

    State createChildState(String localName) throws SAXException {
      if (grammar == null)
	return super.createChildState(localName);
      if (localName.equals("grammar"))
	return new MergeGrammarState(grammar);
      error("expected_grammar", localName);
      return null;
    }

    void checkForeignElement() throws SAXException {
      error("root_bad_namespace_uri", relaxng10URI);
    }

    public void endDocument() throws SAXException {
      if (!hadError)
	startPattern = containedPattern;
    }

    boolean isRelaxNGElement(String uri) throws SAXException {
      if (!uri.startsWith(relaxngURIPrefix))
	return false;
      if (!uri.equals(relaxng10URI))
	warning("wrong_uri_version",
		relaxng10URI.substring(relaxngURIPrefix.length()),
		uri.substring(relaxngURIPrefix.length()));
      relaxngURI = uri;
      return true;
    }

  }

  class NotAllowedState extends EmptyContentState {
    State create() {
      return new NotAllowedState();
    }

    ParsedPattern makePattern() {
      return schemaBuilder.makeNotAllowed(startLocation, null);
    }
  }

  class EmptyState extends EmptyContentState {
    State create() {
      return new EmptyState();
    }

    ParsedPattern makePattern() {
      return schemaBuilder.makeEmpty(startLocation, null);
    }
  }

  class TextState extends EmptyContentState {
    State create() {
      return new TextState();
    }

    ParsedPattern makePattern() {
      return schemaBuilder.makeText(startLocation, null);
    }
  }

  class ValueState extends EmptyContentState {
    StringBuffer buf = new StringBuffer();
    String type;

    State create() {
      return new ValueState();
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("type"))
	type = checkNCName(value.trim());
      else
	super.setOtherAttribute(name, value);
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }

    void checkForeignElement() throws SAXException {
      error("value_contains_foreign_element");
    }

    ParsedPattern makePattern() throws SAXException {
      if (type == null)
        return makePattern("", "token");
      else
        return makePattern(datatypeLibrary, type);
    }

    ParsedPattern makePattern(String datatypeLibrary, String type) {
      return schemaBuilder.makeValue(datatypeLibrary,
                                     type,
                                     buf.toString(),
                                     this,
                                     startLocation,
                                     null);
    }

  }

  class DataState extends State {
    String type;
    ParsedPattern except = null;
    DataPatternBuilder dpb = null;

    State create() {
      return new DataState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("param")) {
	if (except != null)
	  error("param_after_except");
	return new ParamState(dpb);
      }
      if (localName.equals("except")) {
	if (except != null)
	  error("multiple_except");
	return new ChoiceState();
      }
      error("expected_param_except", localName);
      return null;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("type"))
	type = checkNCName(value.trim());
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (type == null)
	error("missing_type_attribute");
      else
	dpb = schemaBuilder.makeDataPatternBuilder(datatypeLibrary, type, startLocation);
    }

    void end() throws SAXException {
      ParsedPattern p;
      if (dpb != null) {
        if (except != null)
          p = dpb.makePattern(except, startLocation, null);
        else
          p = dpb.makePattern(startLocation, null);
      }
      else
        p = schemaBuilder.makeErrorPattern();
      parent.endChild(p);
    }

    void endChild(ParsedPattern pattern) {
      if (except == null)
	except = pattern;
      else
	except = schemaBuilder.makeChoice(except, pattern, startLocation, null);
    }

  }

  class ParamState extends State {
    private StringBuffer buf = new StringBuffer();
    private DataPatternBuilder dpb;
    private String name;

    ParamState(DataPatternBuilder dpb) {
      this.dpb = dpb;
    }

    State create() {
      return new ParamState(null);
    }

    void setName(String name) throws SAXException {
      this.name = checkNCName(name);
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
    }

    State createChildState(String localName) throws SAXException {
      error("expected_empty", localName);
      return null;
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }

    void checkForeignElement() throws SAXException {
      error("param_contains_foreign_element");
    }

    void end() throws SAXException {
      if (name == null)
	return;
      if (dpb != null)
	dpb.addParam(name, buf.toString(), this, startLocation, null);
    }
  }

  class AttributeState extends PatternContainerState implements NameClassRef {
    ParsedNameClass nameClass;
    String name;

    State create() {
      return new AttributeState();
    }

    void setName(String name) {
      this.name = name;
    }

    public void setNameClass(ParsedNameClass nc) {
      nameClass = nc;
    }

    void endAttributes() throws SAXException {
      if (name != null) {
	String nsUse;
	if (ns != null)
	  nsUse = ns;
	else
	  nsUse = "";
	nameClass = expandName(name, nsUse);
      }
      else
	new NameClassChildState(this, this).set();
    }

    void end() throws SAXException {
      if (containedPattern == null)
	containedPattern = schemaBuilder.makeText(startLocation, null);
      super.end();
    }

    ParsedPattern wrapPattern(ParsedPattern p) throws SAXException {
      return schemaBuilder.makeAttribute(nameClass, p, startLocation, null);
    }

    State createChildState(String localName) throws SAXException {
      State tem = super.createChildState(localName);
      if (tem != null && containedPattern != null)
	error("attribute_multi_pattern");
      return tem;
    }

  }

  abstract class SinglePatternContainerState extends PatternContainerState {
    State createChildState(String localName) throws SAXException {
      if (containedPattern == null)
	return super.createChildState(localName);
      error("too_many_children");
      return null;
    }
  }

  class DivState extends State {
    GrammarSection section;

    DivState() { }

    DivState(GrammarSection section) {
      this.section = section;
    }

    State create() {
      return new DivState(null);
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("define"))
	return new DefineState(section);
      if (localName.equals("start"))
	return new StartState(section);
      if (localName.equals("include")) {
	Include include = section.makeInclude();
	if (include != null)
	  return new IncludeState(include);
      }
      if (localName.equals("div"))
	return new DivState(section.makeDiv());
      error("expected_define", localName);
      // XXX better errors
      return null;
    }

    void end() throws SAXException {
    }
  }


  class IncludeState extends DivState {
    String href;
    Include include;

    IncludeState(Include include) {
      super(include);
      this.include = include;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("href")) {
	href = value;
	checkUri(href);
      }
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (href == null)
	error("missing_href_attribute");
      else
        href = resolve(href);
    }

    void end() throws SAXException {
      if (href != null) {
        try {
          include.endInclude(href, getNs(), startLocation, null);
        }
        catch (IllegalSchemaException e) {
        }
      }
    }
  }

  class MergeGrammarState extends DivState {
    IncludedGrammar grammar;
    MergeGrammarState(IncludedGrammar grammar) {
      super(grammar);
      this.grammar = grammar;
    }

    void end() throws SAXException {
      grammar.endIncludedGrammar(startLocation, null);
      // need a non-null pattern to avoid error
      // XXX This is a bit fishy
      parent.endChild(schemaBuilder.makeEmpty(null, null));
    }
  }

  class GrammarState extends DivState {
    Grammar grammar;

    void setParent(State parent) {
      super.setParent(parent);
      grammar = schemaBuilder.makeGrammar(scope);
      section = grammar;
      scope = grammar;
    }

    State create() {
      return new GrammarState();
    }

    void end() throws SAXException {
      parent.endChild(grammar.endGrammar(startLocation, null));
    }
  }

  class RefState extends EmptyContentState {
    String name;

    State create() {
      return new RefState();
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
      if (scope == null)
	error("ref_outside_grammar");
    }

    void setName(String name) throws SAXException {
      this.name = checkNCName(name);
    }

    ParsedPattern makePattern() {
      return makePattern(scope);
    }

    ParsedPattern makePattern(Scope scope) {
      return scope.makeRef(name, startLocation, null);
    }
  }

  class ParentRefState extends RefState {
    State create() {
      return new ParentRefState();
    }

    void endAttributes() throws SAXException {
      super.endAttributes();
      if (scope.getParent() == null)
	error("parent_ref_outside_grammar");
    }

    ParsedPattern makePattern() {
      return makePattern(scope == null ? null : scope.getParent());
    }
  }

  class ExternalRefState extends EmptyContentState {
    String href;
    ParsedPattern includedPattern;

    State create() {
      return new ExternalRefState();
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("href")) {
	href = value;
	checkUri(href);
      }
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (href == null)
	error("missing_href_attribute");
      else
        href = resolve(href);
    }

    ParsedPattern makePattern() {
      if (href != null) {
        try {
          return schemaBuilder.makeExternalRef(href,
                                               getNs(),
                                               scope,
                                               startLocation,
                                               null);
        }
        catch (IllegalSchemaException e) { }
      }
      return schemaBuilder.makeErrorPattern();
    }
  }

  abstract class DefinitionState extends PatternContainerState {
    GrammarSection.Combine combine = null;
    GrammarSection section;

    DefinitionState(GrammarSection section) {
      this.section = section;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("combine")) {
	value = value.trim();
	if (value.equals("choice"))
	  combine = GrammarSection.COMBINE_CHOICE;
	else if (value.equals("interleave"))
	  combine = GrammarSection.COMBINE_INTERLEAVE;
	else
	  error("combine_attribute_bad_value", value);
      }
      else
	super.setOtherAttribute(name, value);
    }
  }

  class DefineState extends DefinitionState {
    String name;

    DefineState(GrammarSection section) {
      super(section);
    }

    State create() {
      return new DefineState(null);
    }

    void setName(String name) throws SAXException {
      this.name = checkNCName(name);
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
    }

    void sendPatternToParent(ParsedPattern p) {
      if (name != null)
	section.define(name, combine, p, startLocation, null);
    }

  }

  class StartState extends DefinitionState {

    StartState(GrammarSection section) {
      super(section);
    }

    State create() {
      return new StartState(null);
    }

    void sendPatternToParent(ParsedPattern p) {
      section.define(GrammarSection.START, combine, p, startLocation, null);
    }

    State createChildState(String localName) throws SAXException {
      State tem = super.createChildState(localName);
      if (tem != null && containedPattern != null)
	error("start_multi_pattern");
      return tem;
    }

  }

  abstract class NameClassContainerState extends State {
    State createChildState(String localName) throws SAXException {
      State state = (State)nameClassTable.get(localName);
      if (state == null) {
	error("expected_name_class", localName);
	return null;
      }
      return state.create();
    }
  }

  class NameClassChildState extends NameClassContainerState {
    State prevState;
    NameClassRef nameClassRef;

    State create() {
      return null;
    }

    NameClassChildState(State prevState, NameClassRef nameClassRef) {
      this.prevState = prevState;
      this.nameClassRef = nameClassRef;
      setParent(prevState.parent);
      this.ns = prevState.ns;
    }

    void endChild(ParsedNameClass nameClass) {
      nameClassRef.setNameClass(nameClass);
      prevState.set();
    }

    void end() throws SAXException {
      nameClassRef.setNameClass(schemaBuilder.makeErrorNameClass());
      error("missing_name_class");
      prevState.set();
      prevState.end();
    }
  }

  abstract class NameClassBaseState extends State {

    abstract ParsedNameClass makeNameClass() throws SAXException;

    void end() throws SAXException {
      parent.endChild(makeNameClass());
    }
  }

  class NameState extends NameClassBaseState {
    StringBuffer buf = new StringBuffer();

    State createChildState(String localName) throws SAXException {
      error("expected_name", localName);
      return null;
    }

    State create() {
      return new NameState();
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }

    void checkForeignElement() throws SAXException {
      error("name_contains_foreign_element");
    }

    ParsedNameClass makeNameClass() throws SAXException {
      return expandName(buf.toString().trim(), getNs());
    }

  }

  private static final int PATTERN_CONTEXT = 0;
  private static final int ANY_NAME_CONTEXT = 1;
  private static final int NS_NAME_CONTEXT = 2;

  class AnyNameState extends NameClassBaseState {
    ParsedNameClass except = null;

    State create() {
      return new AnyNameState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("except")) {
	if (except != null)
	  error("multiple_except");
	return new NameClassChoiceState(getContext());
      }
      error("expected_except", localName);
      return null;
    }

    int getContext() {
      return ANY_NAME_CONTEXT;
    }

    ParsedNameClass makeNameClass() {
      if (except == null)
	return makeNameClassNoExcept();
      else
	return makeNameClassExcept(except);
    }

    ParsedNameClass makeNameClassNoExcept() {
      return schemaBuilder.makeAnyName(null, null);
    }

    ParsedNameClass makeNameClassExcept(ParsedNameClass except) {
      return schemaBuilder.makeAnyName(except, startLocation, null);
    }

    void endChild(ParsedNameClass nameClass) {
      if (except != null)
	except = schemaBuilder.makeChoice(except, nameClass, startLocation, null);
      else
	except = nameClass;
    }

  }

  class NsNameState extends AnyNameState {
    State create() {
      return new NsNameState();
    }

    ParsedNameClass makeNameClassNoExcept() {
      return schemaBuilder.makeNsName(getNs(), null, null);
    }

    ParsedNameClass makeNameClassExcept(ParsedNameClass except) {
      return schemaBuilder.makeNsName(getNs(), except, null, null);
    }

    int getContext() {
      return NS_NAME_CONTEXT;
    }

  }

  class NameClassChoiceState extends NameClassContainerState {
    private ParsedNameClass nameClass;
    private int context;

    NameClassChoiceState() {
      this.context = PATTERN_CONTEXT;
    }

    NameClassChoiceState(int context) {
      this.context = context;
    }

    void setParent(State parent) {
      super.setParent(parent);
      if (parent instanceof NameClassChoiceState)
	this.context = ((NameClassChoiceState)parent).context;
    }

    State create() {
      return new NameClassChoiceState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("anyName")) {
	if (context >= ANY_NAME_CONTEXT) {
	  error(context == ANY_NAME_CONTEXT
		? "any_name_except_contains_any_name"
		: "ns_name_except_contains_any_name");
	  return null;
	}
      }
      else if (localName.equals("nsName")) {
	if (context == NS_NAME_CONTEXT) {
	  error("ns_name_except_contains_ns_name");
	  return null;
	}
      }
      return super.createChildState(localName);
    }

    void endChild(ParsedNameClass nc) {
      if (nameClass == null)
	nameClass = nc;
      else
	nameClass = schemaBuilder.makeChoice(nameClass, nc, startLocation, null);
    }

    void end() throws SAXException {
      if (nameClass == null) {
	error("missing_name_class");
	parent.endChild(schemaBuilder.makeErrorNameClass());
	return;
      }
      parent.endChild(nameClass);
    }
  }

  private void initPatternTable() {
    patternTable = new Hashtable();
    patternTable.put("zeroOrMore", new ZeroOrMoreState());
    patternTable.put("oneOrMore", new OneOrMoreState());
    patternTable.put("optional", new OptionalState());
    patternTable.put("list", new ListState());
    patternTable.put("choice", new ChoiceState());
    patternTable.put("interleave", new InterleaveState());
    patternTable.put("group", new GroupState());
    patternTable.put("mixed", new MixedState());
    patternTable.put("element", new ElementState());
    patternTable.put("attribute", new AttributeState());
    patternTable.put("empty", new EmptyState());
    patternTable.put("text", new TextState());
    patternTable.put("value", new ValueState());
    patternTable.put("data", new DataState());
    patternTable.put("notAllowed", new NotAllowedState());
    patternTable.put("grammar", new GrammarState());
    patternTable.put("ref", new RefState());
    patternTable.put("parentRef", new ParentRefState());
    patternTable.put("externalRef", new ExternalRefState());
  }

  private void initNameClassTable() {
    nameClassTable = new Hashtable();
    nameClassTable.put("name", new NameState());
    nameClassTable.put("anyName", new AnyNameState());
    nameClassTable.put("nsName", new NsNameState());
    nameClassTable.put("choice", new NameClassChoiceState());
  }

  ParsedPattern getStartPattern() throws IllegalSchemaException {
    if (startPattern == null)
      throw new IllegalSchemaException();
    return startPattern;
  }

  void error(String key) throws SAXException {
    error(key, locator);
  }

  void error(String key, String arg) throws SAXException {
    error(key, arg, locator);
  }

  void error(String key, String arg1, String arg2) throws SAXException {
    error(key, arg1, arg2, locator);
  }

  void error(String key, Locator loc) throws SAXException {
    error(new SAXParseException(Localizer.message(key), loc));
  }

  void error(String key, String arg, Locator loc) throws SAXException {
    error(new SAXParseException(Localizer.message(key, arg), loc));
  }

  void error(String key, String arg1, String arg2, Locator loc)
    throws SAXException {
    error(new SAXParseException(Localizer.message(key, arg1, arg2), loc));
  }

  void error(SAXParseException e) throws SAXException {
    hadError = true;
    if (eh != null)
      eh.error(e);
  }

  void warning(String key) throws SAXException {
    warning(key, locator);
  }

  void warning(String key, String arg) throws SAXException {
    warning(key, arg, locator);
  }

  void warning(String key, String arg1, String arg2) throws SAXException {
    warning(key, arg1, arg2, locator);
  }

  void warning(String key, Locator loc) throws SAXException {
    warning(new SAXParseException(Localizer.message(key), loc));
  }

  void warning(String key, String arg, Locator loc) throws SAXException {
    warning(new SAXParseException(Localizer.message(key, arg), loc));
  }

  void warning(String key, String arg1, String arg2, Locator loc)
    throws SAXException {
    warning(new SAXParseException(Localizer.message(key, arg1, arg2), loc));
  }

  void warning(SAXParseException e) throws SAXException {
    if (eh != null)
      eh.warning(e);
  }

  SchemaParser(XMLReader xr,
               ErrorHandler eh,
               SchemaBuilder schemaBuilder,
               Datatype ncNameDatatype,
               IncludedGrammar grammar,
               Scope scope) {
    this.xr = xr;
    this.eh = eh;
    this.schemaBuilder = schemaBuilder;
    this.ncNameDatatype = ncNameDatatype;
    if (eh != null)
      xr.setErrorHandler(eh);
    initPatternTable();
    initNameClassTable();
    prefixMapping = new PrefixMapping("xml", xmlURI, null);
    new RootState(grammar, scope, SchemaBuilder.INHERIT_NS).set();
  }

  ParsedNameClass expandName(String name, String ns) throws SAXException {
    int ic = name.indexOf(':');
    if (ic == -1)
      return schemaBuilder.makeName(ns, checkNCName(name), null, null, null);
    String prefix = checkNCName(name.substring(0, ic));
    String localName = checkNCName(name.substring(ic + 1));
    for (PrefixMapping tem = prefixMapping; tem != null; tem = tem.next)
      if (tem.prefix.equals(prefix))
	return schemaBuilder.makeName(tem.uri, localName, prefix, null, null);
    error("undefined_prefix", prefix);
    return schemaBuilder.makeName("", localName, null, null, null);
  }

  String checkNCName(String str) throws SAXException {
    if (!ncNameDatatype.isValid(str, null))
      error("invalid_ncname", str);
    return str;
  }

  String resolve(String systemId) throws SAXException {
    if (Uri.hasFragmentId(systemId))
      error("href_fragment_id");
    systemId = Uri.escapeDisallowedChars(systemId);
    return Uri.resolve(xmlBaseHandler.getBaseUri(), systemId);
  }

  Location makeLocation() {
    if (locator == null)
      return null;
    return schemaBuilder.makeLocation(locator.getSystemId(),
				      locator.getLineNumber(),
				      locator.getColumnNumber());
  }

  void checkUri(String s) throws SAXException {
    if (!Uri.isValid(s))
      error("invalid_uri", s);
  }
}
