package com.thaiopensource.relaxng.parse;

import org.relaxng.datatype.ValidationContext;

public interface SchemaBuilder {
  ParsedPattern makeChoice(ParsedPattern p1, ParsedPattern p2, Location loc) throws BuildException;
  ParsedPattern makeInterleave(ParsedPattern p1, ParsedPattern p2, Location loc) throws BuildException;
  ParsedPattern makeGroup(ParsedPattern p1, ParsedPattern p2, Location loc) throws BuildException;
  ParsedPattern makeOneOrMore(ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  ParsedPattern makeZeroOrMore(ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  ParsedPattern makeOptional(ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  ParsedPattern makeList(ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  ParsedPattern makeMixed(ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  ParsedPattern makeEmpty(Location loc, Annotations anno);
  ParsedPattern makeNotAllowed(Location loc, Annotations anno);
  ParsedPattern makeText(Location loc, Annotations anno);
  ParsedPattern makeAttribute(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  ParsedPattern makeElement(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno) throws BuildException;
  DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location loc) throws BuildException;
  ParsedPattern makeValue(String datatypeLibrary, String type, String value, ValidationContext vc,
                          Location loc, Annotations anno) throws BuildException;
  Grammar makeGrammar(Scope parent);
  ParsedPattern annotateMulti(ParsedPattern p, Annotations a) throws BuildException;
  ParsedNameClass annotateMulti(ParsedNameClass p, Annotations a) throws BuildException;
  ParsedPattern annotateAfter(ParsedPattern p, ElementAnnotation e) throws BuildException;
  ParsedNameClass annotateAfter(ParsedNameClass nc, ElementAnnotation e) throws BuildException;
  ParsedPattern makeExternalRef(String uri, String ns, Scope scope,
                                Location loc, Annotations anno) throws BuildException, IllegalSchemaException;
  ParsedNameClass makeChoice(ParsedNameClass nc1, ParsedNameClass nc2, Location loc);

  static final String INHERIT_NS = new String("#inherit");
  ParsedNameClass makeName(String ns, String localName, String prefix, Location loc, Annotations anno);
  ParsedNameClass makeNsName(String ns, Location loc, Annotations anno);
  /**
   * Caller must enforce constraints on except.
   */
  ParsedNameClass makeNsName(String ns, ParsedNameClass except, Location loc, Annotations anno);
  ParsedNameClass makeAnyName(Location loc, Annotations anno);
   /**
   * Caller must enforce constraints on except.
   */
  ParsedNameClass makeAnyName(ParsedNameClass except, Location loc, Annotations anno);
  Location makeLocation(String systemId, int lineNumber, int columnNumber);
  Annotations makeAnnotations();
  ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix, Location loc);
  ParsedPattern makeErrorPattern();
  ParsedNameClass makeErrorNameClass();
}
