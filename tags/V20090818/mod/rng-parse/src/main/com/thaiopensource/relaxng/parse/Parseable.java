package com.thaiopensource.relaxng.parse;

public interface Parseable<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends SubParser<P, NC, L, EA, CL, A> {
  P parse(SchemaBuilder<P, NC, L, EA, CL, A> f, Scope<P, L, EA, CL, A> scope) throws BuildException, IllegalSchemaException;
}
