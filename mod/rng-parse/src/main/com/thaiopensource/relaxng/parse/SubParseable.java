package com.thaiopensource.relaxng.parse;

public interface SubParseable<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends Parseable<P, NC, L, EA, CL, A> {
  P parseAsInclude(SchemaBuilder<P, NC, L, EA, CL, A> f, IncludedGrammar<P, L, EA, CL, A> g)
          throws BuildException, IllegalSchemaException;
  /* The returned URI will have disallowed characters escaped. May return null for top-level schema. */
  String getUri();
}
