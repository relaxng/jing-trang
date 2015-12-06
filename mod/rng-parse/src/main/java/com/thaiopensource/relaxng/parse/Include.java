package com.thaiopensource.relaxng.parse;

public interface Include<P, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends GrammarSection<P, L, EA, CL, A> {
  void endInclude(String href, String base, String ns, L loc, A anno)
          throws BuildException, IllegalSchemaException;
}
