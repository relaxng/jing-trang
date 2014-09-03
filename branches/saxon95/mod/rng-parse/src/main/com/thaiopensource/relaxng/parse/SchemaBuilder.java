package com.thaiopensource.relaxng.parse;

import java.util.List;

public interface SchemaBuilder<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>> {
  P makeChoice(List<P> patterns, L loc, A anno) throws BuildException;
  P makeInterleave(List<P> patterns, L loc, A anno) throws BuildException;
  P makeGroup(List<P> patterns, L loc, A anno) throws BuildException;
  P makeOneOrMore(P p, L loc, A anno) throws BuildException;
  P makeZeroOrMore(P p, L loc, A anno) throws BuildException;
  P makeOptional(P p, L loc, A anno) throws BuildException;
  P makeList(P p, L loc, A anno) throws BuildException;
  P makeMixed(P p, L loc, A anno) throws BuildException;
  P makeEmpty(L loc, A anno);
  P makeNotAllowed(L loc, A anno);
  P makeText(L loc, A anno);
  P makeAttribute(NC nc, P p, L loc, A anno) throws BuildException;
  P makeElement(NC nc, P p, L loc, A anno) throws BuildException;
  DataPatternBuilder<P, L, EA, CL, A> makeDataPatternBuilder(String datatypeLibrary, String type, L loc) throws BuildException;
  P makeValue(String datatypeLibrary, String type, String value, Context c, String ns,
              L loc, A anno) throws BuildException;
  Grammar<P, L, EA, CL, A> makeGrammar(Scope<P, L, EA, CL, A> parent);
  P annotatePattern(P p, A anno) throws BuildException;
  NC annotateNameClass(NC nc, A anno) throws BuildException;
  P annotateAfterPattern(P p, EA e) throws BuildException;
  NC annotateAfterNameClass(NC nc, EA e) throws BuildException;
  P commentAfterPattern(P p, CL comments) throws BuildException;
  NC commentAfterNameClass(NC nc, CL comments) throws BuildException;
  P makeExternalRef(String href, String base, String ns, Scope<P, L, EA, CL, A> scope,
                    L loc, A anno) throws BuildException, IllegalSchemaException;
  NC makeNameClassChoice(List<NC> nameClasses, L loc, A anno);

  // Compare against INHERIT_NS with == not equals.
  // Doing new String() ensures it is not == if the user specifies #inherit explicitly in the schema.
  static final String INHERIT_NS = new String("#inherit");
  NC makeName(String ns, String localName, String prefix, L loc, A anno);
  NC makeNsName(String ns, L loc, A anno);
  /*
   * Caller must enforce constraints on except.
   */
  NC makeNsName(String ns, NC except, L loc, A anno);
  NC makeAnyName(L loc, A anno);
  /*
   * Caller must enforce constraints on except.
   */
  NC makeAnyName(NC except, L loc, A anno);
  L makeLocation(String systemId, int lineNumber, int columnNumber);
  A makeAnnotations(CL comments, Context context);
  ElementAnnotationBuilder<L, EA, CL> makeElementAnnotationBuilder(String ns, String localName, String prefix,
                                                                   L loc, CL comments, Context context);
  CL makeCommentList();
  P makeErrorPattern();
  NC makeErrorNameClass();
  boolean usesComments();
}
